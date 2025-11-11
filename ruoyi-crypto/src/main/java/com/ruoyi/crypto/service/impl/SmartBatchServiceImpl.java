package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.domain.MonitorBatch;
import com.ruoyi.crypto.domain.MonitorBatchItem;
import com.ruoyi.crypto.domain.MonitorTask;
import com.ruoyi.crypto.domain.MonitorTaskTarget;
import com.ruoyi.crypto.domain.TokenLaunchHistory;
import com.ruoyi.crypto.mapper.*;
import com.ruoyi.crypto.service.ISmartBatchService;
import com.ruoyi.crypto.util.ConsistentHashUtil;
import com.ruoyi.crypto.util.RedisLockUtil;
import com.ruoyi.crypto.websocket.MonitorWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能批次分配Service实现
 * 
 * 核心流程：
 * 1. 从token_launch_history筛选目标（根据智能条件）
 * 2. 与monitor_task_target_v2进行diff，计算新增/删除
 * 3. 使用Redis分布式锁保证并发安全
 * 4. Epoch版本控制实现零停机更新
 * 5. 一致性哈希分配到Consumer
 * 6. WebSocket通知Python端
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@Service
public class SmartBatchServiceImpl implements ISmartBatchService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartBatchServiceImpl.class);
    
    @Autowired
    private MonitorTaskMapper monitorTaskMapper;
    
    @Autowired
    private MonitorTaskTargetMapper monitorTaskTargetMapper;
    
    @Autowired
    private TokenLaunchHistoryMapper tokenLaunchHistoryMapper;
    
    @Autowired
    private MonitorBatchMapper monitorBatchMapper;
    
    @Autowired
    private MonitorBatchItemMapper monitorBatchItemMapper;
    
    @Autowired
    private ConsistentHashUtil consistentHashUtil;
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    @Autowired(required = false)
    private MonitorWebSocketHandler webSocketHandler;
    
    @Value("${monitor.batch.batch-size:99}")
    private int batchSize;
    
    @Value("${monitor.batch.max-targets:10000}")
    private int maxTargets;
    
    /**
     * 执行智能目标同步和批次分配
     * 
     * @param taskId 任务ID
     * @return 同步结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncTargetsAndAllocateBatches(Long taskId) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查询任务信息
            MonitorTask task = monitorTaskMapper.selectMonitorTaskById(taskId);
            if (task == null) {
                result.put("success", false);
                result.put("message", "任务不存在");
                return result;
            }
            
            if (task.getStatus() != 1) {
                result.put("success", false);
                result.put("message", "任务未启用");
                return result;
            }
            
            logger.info("开始智能目标同步：taskId={}, taskName={}", taskId, task.getTaskName());
            
            // 2. 从token_launch_history筛选目标
            List<TokenLaunchHistory> latestTokens = fetchLatestTokens(task);
            logger.info("从token_launch_history筛选出{}个目标", latestTokens.size());
            
            Set<String> latestCAs = latestTokens.stream()
                    .map(TokenLaunchHistory::getCa)
                    .collect(Collectors.toSet());
            
            // 3. 获取当前任务的目标
            List<MonitorTaskTarget> currentTargets = monitorTaskTargetMapper.selectByTaskId(taskId);
            Set<String> currentCAs = currentTargets.stream()
                    .map(MonitorTaskTarget::getCa)
                    .collect(Collectors.toSet());
            
            // 4. 计算Diff
            Set<String> toAdd = new HashSet<>(latestCAs);
            toAdd.removeAll(currentCAs);
            
            Set<String> toRemove = new HashSet<>(currentCAs);
            toRemove.removeAll(latestCAs);
            
            logger.info("目标Diff结果：新增={}, 删除={}", toAdd.size(), toRemove.size());
            
            // 5. 使用分布式锁保护批次分配
            String lockKey = "smart_batch:task:" + taskId;
            String requestId = UUID.randomUUID().toString();


            
            boolean locked = redisLockUtil.tryLockWithDynamicTimeout(lockKey, requestId, latestCAs.size());
            if (!locked) {
                result.put("success", false);
                result.put("message", "获取分布式锁失败，可能有其他同步任务正在执行");
                return result;
            }

            try {
                // 6. 更新monitor_task_target_v2
                int addedCount = 0;
                int removedCount = 0;

                // 建立CA到Token信息的映射
                Map<String, TokenLaunchHistory> caToTokenMap = new HashMap<>();
                for (TokenLaunchHistory token : latestTokens) {
                    caToTokenMap.put(token.getCa(), token);
                }

                if (!toAdd.isEmpty()) {
                    // 批量插入目标（每500条一批）
                    List<MonitorTaskTarget> targetsToInsert = new ArrayList<>();
                    Date now = new Date();

                    for (String ca : toAdd) {
                        TokenLaunchHistory tokenInfo = caToTokenMap.get(ca);

                        MonitorTaskTarget target = new MonitorTaskTarget();
                        target.setTaskId(taskId);
                        target.setCa(ca);
                        target.setChainType(task.getChainType());

                        // 从 TokenLaunchHistory 中获取 token 信息
                        if (tokenInfo != null) {
                            target.setTokenName(tokenInfo.getTokenName());
                            target.setTokenSymbol(tokenInfo.getTokenSymbol());
                            target.setMarketCap(BigDecimal.valueOf(tokenInfo.getHighestMarketCap()));
                        }

                        // status字段是tinyint类型：1=活跃, 0=已移除
                        // 注意：不设置status，让数据库使用默认值1
                        target.setCreateTime(now);

                        targetsToInsert.add(target);

                        // 每500条批量插入一次
                        if (targetsToInsert.size() >= 500) {
                            monitorTaskTargetMapper.insertBatchTargets(targetsToInsert);
                            addedCount += targetsToInsert.size();
                            targetsToInsert.clear();
                            logger.info("已批量插入{}个目标", addedCount);
                        }
                    }

                    // 插入剩余的目标
                    if (!targetsToInsert.isEmpty()) {
                        monitorTaskTargetMapper.insertBatchTargets(targetsToInsert);
                        addedCount += targetsToInsert.size();
                        logger.info("最终插入目标总数：{}", addedCount);
                    }
                }

                if (!toRemove.isEmpty()) {
                    for (String ca : toRemove) {
                        monitorTaskTargetMapper.deleteByTaskIdAndCa(taskId, ca);
                        removedCount++;
                    }
                }

                logger.info("目标更新完成：新增={}, 删除={}", addedCount, removedCount);

                // 7. 重新分配批次（Epoch版本递增）
                Integer newEpoch = (task.getCurrentEpoch() != null ? task.getCurrentEpoch() : 0) + 1;

                // 7.1 删除旧批次数据（避免唯一索引冲突）
                // 由于 uk_task_batch(task_id, batch_no) 唯一索引限制，需要先删除旧批次
                if (task.getCurrentEpoch() != null && task.getCurrentEpoch() > 0) {
                    logger.info("删除旧批次数据：taskId={}, currentEpoch={}", taskId, task.getCurrentEpoch());
                    // 先删除批次项
                    monitorBatchMapper.deleteBatchItemsByTaskId(taskId);
                    // 再删除批次
                    monitorBatchMapper.deleteBatchesByTaskId(taskId);
                }

                int allocatedCount = allocateBatches(taskId, newEpoch, new ArrayList<>(latestCAs));

                // 8. 更新任务的current_epoch
                MonitorTask updateTask = new MonitorTask();
                updateTask.setId(taskId);
                updateTask.setCurrentEpoch(newEpoch);
                updateTask.setUpdateTime(new Date());
                monitorTaskMapper.updateMonitorTask(updateTask);

                // 9. 通知Python端（通过WebSocket或Redis Pub/Sub）
                notifyPythonClient(taskId, newEpoch);

                // 11. 返回结果
                long duration = System.currentTimeMillis() - startTime;
                result.put("success", true);
                result.put("addedTargets", addedCount);
                result.put("removedTargets", removedCount);
                result.put("totalTargets", latestCAs.size());
                result.put("allocatedBatches", allocatedCount);
                result.put("newEpoch", newEpoch);
                result.put("duration", duration + "ms");

                logger.info("智能目标同步完成：taskId={}, epoch={}, 耗时={}ms", taskId, newEpoch, duration);

            } finally {
                // 释放锁
                redisLockUtil.releaseLock(lockKey, requestId);
            }
            
        } catch (Exception e) {
            logger.error("智能目标同步失败：taskId=" + taskId, e);
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 从token_launch_history筛选最新目标
     */
    private List<TokenLaunchHistory> fetchLatestTokens(MonitorTask task) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("chainType", task.getChainType());
        conditions.put("maxTargets", 20000);

        // 从任务的智能条件中解析参数（如果有）
        if(StringUtils.isNotNull(task.getMinMarketCap())) {
            conditions.put("minMarketCap", task.getMinMarketCap());
        }

        if(StringUtils.isNotNull(task.getMaxMarketCap())) {
            conditions.put("maxMarketCap", task.getMaxMarketCap());
        }

        if(StringUtils.isNotNull(task.getHasTwitter())) {
            conditions.put("requireTwitter", true);
        }

        return tokenLaunchHistoryMapper.selectBySmartConditions(conditions);
    }
    
    /**
     * 分配批次（使用一致性哈希）
     */
    private int allocateBatches(Long taskId, Integer epoch, List<String> caList) {
        logger.info("开始批次分配：taskId={}, epoch={}, targetCount={}", taskId, epoch, caList.size());
        
        // 1. 查询当前活跃的Consumer列表
        List<String> activeConsumers = getActiveConsumers(taskId);
        if (activeConsumers.isEmpty()) {
            logger.warn("没有活跃的Consumer，跳过批次分配");
            return 0;
        }
        
        // 2. 更新一致性哈希环
        consistentHashUtil.updateConsumers(activeConsumers);
        
        // 3. 查询所有targets并建立CA到ID的映射（优化性能）
        List<MonitorTaskTarget> allTargets = monitorTaskTargetMapper.selectByTaskId(taskId);
        Map<String, Long> caToTargetIdMap = new HashMap<>();
        for (MonitorTaskTarget target : allTargets) {
            caToTargetIdMap.put(target.getCa(), target.getId());
        }
        
        // 4. 按Consumer分组CA
        Map<String, List<String>> casByConsumer = consistentHashUtil.groupCAsByConsumer(caList);
        
        // 5. 为每个Consumer创建批次
        int totalBatchCount = 0;
        int globalBatchNo = 1; // 全局批次编号，从1开始
        
        for (Map.Entry<String, List<String>> entry : casByConsumer.entrySet()) {
            String consumerId = entry.getKey();
            List<String> consumerCAs = entry.getValue();
            
            // 按batchSize分批
            int batchCount = (int) Math.ceil((double) consumerCAs.size() / batchSize);
            for (int i = 0; i < batchCount; i++) {
                int fromIndex = i * batchSize;
                int toIndex = Math.min(fromIndex + batchSize, consumerCAs.size());
                List<String> batchCAs = consumerCAs.subList(fromIndex, toIndex);
                
                // 创建批次
                MonitorBatch batch = new MonitorBatch();
                batch.setTaskId(taskId);
                batch.setBatchNo(globalBatchNo++); // 设置批次编号并递增
                batch.setConsumerId(consumerId);
                batch.setEpoch(epoch);
                batch.setStatus("pending");
                batch.setTargetCount(batchCAs.size());
                batch.setItemCount(batchCAs.size()); // 设置item_count
                batch.setCreateTime(new Date());
                
                monitorBatchMapper.insertMonitorBatch(batch);
                
                // ⭐ 批量创建批次项（每500条一批）
                List<MonitorBatchItem> itemsToInsert = new ArrayList<>();
                int itemOrder = 0; // 批次内序号
                for (String ca : batchCAs) {
                    // 从映射中获取 target_id 和对应的 target 信息
                    Long targetId = caToTargetIdMap.get(ca);
                    MonitorTaskTarget target = null;
                    if (targetId != null) {
                        for (MonitorTaskTarget t : allTargets) {
                            if (t.getId().equals(targetId)) {
                                target = t;
                                break;
                            }
                        }
                    }
                    
                    MonitorBatchItem item = new MonitorBatchItem();
                    item.setBatchId(batch.getId());
                    item.setTaskId(taskId);
                    item.setBatchNo(batch.getBatchNo()); // 设置批次编号
                    item.setTargetId(targetId != null ? targetId : 0L); // 设置target_id
                    item.setItemOrder(itemOrder++); // 设置批次内序号
                    item.setCa(ca);
                    
                    // 从 target 中获取 token 信息（如果有）
                    if (target != null) {
                        item.setTokenName(target.getTokenName());
                        item.setTokenSymbol(target.getTokenSymbol());
                        item.setMarketCap(target.getMarketCap());
                    }
                    
                    // status字段不设置，使用数据库默认值
                    item.setCreateTime(new Date());
                    
                    itemsToInsert.add(item);
                    
                    // 每500条批量插入一次
                    if (itemsToInsert.size() >= 500) {
                        monitorBatchItemMapper.insertBatchItems(itemsToInsert);
                        itemsToInsert.clear();
                    }
                }
                
                // 插入剩余的批次项
                if (!itemsToInsert.isEmpty()) {
                    monitorBatchItemMapper.insertBatchItems(itemsToInsert);
                }
                
                totalBatchCount++;
            }
            
            logger.info("Consumer {}分配了{}个批次，共{}个目标", consumerId, batchCount, consumerCAs.size());
        }
        
        logger.info("批次分配完成：taskId={}, epoch={}, 总批次数={}", taskId, epoch, totalBatchCount);
        return totalBatchCount;
    }
    
    /**
     * 获取活跃的Consumer列表（可以从数据库或Redis读取）
     */
    private List<String> getActiveConsumers(Long taskId) {
        // TODO: 从数据库或Redis读取活跃的Consumer列表
        // 这里暂时返回模拟数据
        // return redisTemplate.opsForSet().members("monitor:consumers:" + taskId);
        
        // 临时实现：返回固定Consumer列表
        return Arrays.asList("consumer-1", "consumer-2");
    }
    
    /**
     * 归档旧epoch的批次数据
     */
    @Override
    public int archiveOldBatches(Long taskId, Integer currentEpoch) {
        logger.info("开始归档旧批次：taskId={}, currentEpoch={}", taskId, currentEpoch);
        
        // 标记旧epoch批次的archived_time
        int count = monitorBatchMapper.archiveOldEpochs(taskId, currentEpoch);
        
        logger.info("归档完成：taskId={}, 归档批次数={}", taskId, count);
        return count;
    }
    
    /**
     * 通知Python客户端（通过WebSocket或Redis Pub/Sub）
     */
    private void notifyPythonClient(Long taskId, Integer newEpoch) {
        try {
            if (webSocketHandler != null) {
                Map<String, Object> message = new HashMap<>();
                message.put("type", "batch_allocated");
                message.put("taskId", taskId);
                message.put("epoch", newEpoch);
                message.put("timestamp", System.currentTimeMillis());
                
                webSocketHandler.broadcastMessage(message);
                logger.info("WebSocket通知已发送：taskId={}, epoch={}", taskId, newEpoch);
            }
        } catch (Exception e) {
            logger.error("发送WebSocket通知失败", e);
        }
    }
}

