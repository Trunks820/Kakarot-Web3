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
                if (toAdd.isEmpty() && toRemove.isEmpty() && latestCAs.size() == currentCAs.size()) {
                    logger.info("无目标变更，跳过批次重分配：taskId={}, epoch={}, targetCount={}", taskId, task.getCurrentEpoch(), latestCAs.size());
                    long duration = System.currentTimeMillis() - startTime;
                    result.put("success", true);
                    result.put("message", "无变更，跳过重分配");
                    return result;  // 直接返回，不再创建新批次
                }

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
                Integer oldEpoch = task.getCurrentEpoch() != null ? task.getCurrentEpoch() : 0;
                Integer existingMaxEpoch = monitorBatchMapper.selectMaxEpochByTaskId(taskId);
                int baseEpoch = Math.max(oldEpoch, existingMaxEpoch != null ? existingMaxEpoch : 0);
                Integer newEpoch = baseEpoch + 1;
                
                // ⭐ 防止epoch重复：使用 max(current_epoch, max(epoch in DB)) + 1
                logger.info("Epoch计算：taskId={}, currentEpoch={}, existingMaxEpoch={}, baseEpoch={}, newEpoch={}", 
                    taskId, oldEpoch, existingMaxEpoch, baseEpoch, newEpoch);

                // 7.1 先分配新批次（保证零停机）
                // ⭐ 先创建新epoch批次，确保成功后再删除旧批次，避免中间状态无批次可用
                int allocatedCount = allocateBatches(taskId, newEpoch, new ArrayList<>(latestCAs));
                
                if (allocatedCount == 0) {
                    logger.warn("批次分配失败，保留旧批次数据，不更新epoch");
                    result.put("success", false);
                    result.put("message", "批次分配失败：没有可用的Consumer或分配异常");
                    return result;
                }
                
                logger.info("新批次分配成功：newEpoch={}, batchCount={}", newEpoch, allocatedCount);

                // 7.2 更新任务的current_epoch（切换到新批次）
                MonitorTask updateTask = new MonitorTask();
                updateTask.setId(taskId);
                updateTask.setCurrentEpoch(newEpoch);
                updateTask.setUpdateTime(new Date());
                monitorTaskMapper.updateMonitorTask(updateTask);
                
                logger.info("任务epoch已更新：taskId={}, oldEpoch={}, newEpoch={}", taskId, oldEpoch, newEpoch);

                // 7.3 删除旧批次数据（在新批次创建成功后）
                // ⭐ 此时新批次已生效，删除旧批次不影响消费者读取
                // ⭐ 即使oldEpoch=0，也要删除旧批次（避免唯一索引冲突）
                logger.info("删除旧批次数据：taskId={}, oldEpoch={}, beforeEpoch={}", taskId, oldEpoch, newEpoch);
                // 先删除旧epoch的批次项（删除所有 epoch < newEpoch 的数据）
                int deletedItems = monitorBatchMapper.deleteBatchItemsByTaskId(taskId, newEpoch);
                // 再删除旧epoch的批次
                int deletedBatches = monitorBatchMapper.deleteBatchesByTaskId(taskId, newEpoch);
                logger.info("旧批次清理完成：deletedBatches={}, deletedItems={}", deletedBatches, deletedItems);

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
     * 批量同步所有智能任务（无事务，每个任务独立处理）
     */
    @Override
    public Map<String, Object> syncTargetsAndAllocateBatches() {
        long startTime = System.currentTimeMillis();
        
        // 1. 查询所有启用的智能任务
        MonitorTask query = new MonitorTask();
        query.setStatus(1);
        query.setTaskType("smart");
        List<MonitorTask> monitorTasks = monitorTaskMapper.selectMonitorTaskList(query);
        
        if (monitorTasks.isEmpty()) {
            return createResult(false, "没有需要同步的智能任务", 0, 0, 0);
        }
        
        logger.info("========================================");
        logger.info("开始批量同步所有智能任务，共{}个任务", monitorTasks.size());
        logger.info("========================================");
        
        // 2. 逐个任务执行同步（每个任务独立事务）
        List<Map<String, Object>> taskResults = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        
        for (MonitorTask task : monitorTasks) {
            try {
                logger.info("→ 开始同步任务：id={}, name={}", task.getId(), task.getTaskName());
                
                // ⭐ 调用单任务同步方法（独立事务）
                Map<String, Object> taskResult = syncTargetsAndAllocateBatches(task.getId());
                taskResults.add(taskResult);
                
                if (Boolean.TRUE.equals(taskResult.get("success"))) {
                    successCount++;
                    logger.info("✅ 任务同步成功：id={}, epoch={}", 
                        task.getId(), taskResult.get("newEpoch"));
                } else {
                    String message = (String) taskResult.get("message");
                    if (message != null && message.contains("分布式锁失败")) {
                        skipCount++;
                        logger.warn("⏭ 任务跳过（锁冲突）：id={}", task.getId());
                    } else {
                        failCount++;
                        logger.error("❌ 任务同步失败：id={}, error={}", 
                            task.getId(), message);
                    }
                }
                
            } catch (Exception e) {
                failCount++;
                logger.error("❌ 任务同步异常：id=" + task.getId(), e);
                
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("taskId", task.getId());
                errorResult.put("taskName", task.getTaskName());
                errorResult.put("success", false);
                errorResult.put("message", "异常: " + e.getMessage());
                taskResults.add(errorResult);
            }
        }
        
        // 3. 汇总结果
        long totalDuration = System.currentTimeMillis() - startTime;
        Map<String, Object> summaryResult = new HashMap<>();
        summaryResult.put("success", failCount == 0);
        summaryResult.put("totalTasks", monitorTasks.size());
        summaryResult.put("successCount", successCount);
        summaryResult.put("failCount", failCount);
        summaryResult.put("skipCount", skipCount);
        summaryResult.put("totalDuration", totalDuration + "ms");
        summaryResult.put("taskResults", taskResults);
        
        logger.info("========================================");
        logger.info("批量同步完成：总任务数={}, 成功={}, 失败={}, 跳过={}, 总耗时={}ms", 
            monitorTasks.size(), successCount, failCount, skipCount, totalDuration);
        logger.info("========================================");
        
        return summaryResult;
    }
    
    /**
     * 创建统一格式的结果对象
     */
    private Map<String, Object> createResult(boolean success, String message, 
                                             int addedTargets, int removedTargets, int totalTargets) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("addedTargets", addedTargets);
        result.put("removedTargets", removedTargets);
        result.put("totalTargets", totalTargets);
        return result;
    }
    
    /**
     * 从token_launch_history筛选最新目标
     */
    private List<TokenLaunchHistory> fetchLatestTokens(MonitorTask task) {
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("chainType", task.getChainType());
        // ⭐ 修复：使用@Value配置的maxTargets，而不是硬编码
        conditions.put("maxTargets", this.maxTargets);


        BigDecimal maxMarketCap = task.getMaxMarketCap();
        BigDecimal minMarketCap = task.getMinMarketCap();
        // 从任务的智能条件中解析参数（如果有）
        if(StringUtils.isNotNull(minMarketCap) && minMarketCap.compareTo(BigDecimal.ZERO) > 0) {
            conditions.put("minMarketCap", task.getMinMarketCap());
        }

        if(StringUtils.isNotNull(maxMarketCap) && maxMarketCap.compareTo(BigDecimal.ZERO) > 0) {
            conditions.put("maxMarketCap", task.getMaxMarketCap());
        }

        // ⭐ 修复：根据hasTwitter的值设置不同的筛选逻辑
        // null - 不限
        // "profile" - 推特主页（不包含/status/、/communities/、/search）
        // "tweet" - 推文（包含/status/）
        // "community" - 社区（包含/communities/）
        // "none" - 无推特
        if(StringUtils.isNotNull(task.getHasTwitter())) {
            conditions.put("hasTwitter", task.getHasTwitter());
        }

        return tokenLaunchHistoryMapper.selectBySmartConditions(conditions);
    }
    
    /**
     * 分配批次（使用一致性哈希）
     */
    private int allocateBatches(Long taskId, Integer epoch, List<String> caList) {
        logger.info("开始批次分配：taskId={}, epoch={}, targetCount={}", taskId, epoch, caList.size());
        
        // 0. 查询任务信息（用于获取chainType）
        MonitorTask task = monitorTaskMapper.selectMonitorTaskById(taskId);
        if (task == null) {
            logger.error("任务不存在：taskId={}", taskId);
            return 0;
        }
        
        // 1. 查询当前活跃的Consumer列表
        List<String> activeConsumers = getActiveConsumers(taskId);
        if (activeConsumers.isEmpty()) {
            logger.warn("没有活跃的Consumer，跳过批次分配");
            return 0;
        }
        
        // 2. 更新一致性哈希环
        consistentHashUtil.updateConsumers(activeConsumers);
        
        // 3. 查询所有targets并建立CA到target对象的映射（优化性能，避免O(n²)）
        // ⭐ 修复：直接建立 Map<String, MonitorTaskTarget>，避免后续遍历查找
        List<MonitorTaskTarget> allTargets = monitorTaskTargetMapper.selectByTaskId(taskId);
        Map<String, Long> caToTargetIdMap = new HashMap<>();
        Map<String, MonitorTaskTarget> caToTargetMap = new HashMap<>();
        for (MonitorTaskTarget target : allTargets) {
            caToTargetIdMap.put(target.getCa(), target.getId());
            caToTargetMap.put(target.getCa(), target); // ⭐ 新增：CA到完整target对象的映射
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
                batch.setChainType(task.getChainType()); // ⭐ 设置链类型
                batch.setStatus("pending");
                batch.setTargetCount(batchCAs.size());
                batch.setItemCount(batchCAs.size()); // 设置item_count
                batch.setCreateTime(new Date());
                
                monitorBatchMapper.insertMonitorBatch(batch);
                
                // ⭐ 批量创建批次项（每500条一批）
                List<MonitorBatchItem> itemsToInsert = new ArrayList<>();
                int itemOrder = 0; // 批次内序号
                for (String ca : batchCAs) {
                    // ⭐ 修复O(n²)性能问题：直接从Map获取，时间复杂度O(1)
                    Long targetId = caToTargetIdMap.get(ca);
                    MonitorTaskTarget target = caToTargetMap.get(ca); // ⭐ 直接从Map获取，不再遍历
                    
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

