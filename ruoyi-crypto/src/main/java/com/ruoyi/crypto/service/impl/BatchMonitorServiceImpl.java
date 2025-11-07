package com.ruoyi.crypto.service.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.crypto.domain.QuickMonitorTemplate;
import com.ruoyi.crypto.domain.SolWsBatchPool;
import com.ruoyi.crypto.mapper.QuickMonitorTemplateMapper;
import com.ruoyi.crypto.mapper.SolWsBatchPoolMapper;
import com.ruoyi.crypto.service.IBatchMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 批量监控Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
@Service
public class BatchMonitorServiceImpl implements IBatchMonitorService 
{
    @Autowired
    private SolWsBatchPoolMapper batchPoolMapper;

    @Autowired
    private QuickMonitorTemplateMapper templateMapper;

    /**
     * 智能批量添加监控（自动分配批次）
     */
    @Override
    @Transactional
    public Map<String, Object> smartBatchAdd(List<String> addresses, Map<String, Object> config)
    {
        // 1. 按链类型分组
        Map<String, List<String>> grouped = groupAddressesByChain(addresses);
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 2. 为每条链智能分配批次
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            String chainType = entry.getKey();
            List<String> chainAddresses = entry.getValue();
            
            if (chainAddresses.isEmpty()) {
                continue;
            }
            
            Map<String, Object> result = allocateAndSaveToBatch(chainType, chainAddresses, config);
            results.add(result);
        }
        
        // 3. 返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("results", results);
        response.put("totalCount", addresses.size());
        
        return response;
    }

    /**
     * 为指定链的地址智能分配批次
     */
    private Map<String, Object> allocateAndSaveToBatch(String chainType, List<String> addresses, Map<String, Object> config)
    {
        List<Map<String, Object>> allocations = new ArrayList<>();
        List<String> remaining = new ArrayList<>(addresses);
        
        // 准备配置参数
        String eventsConfig = JSON.toJSONString(config.get("events"));
        String triggerLogic = (String) config.get("triggerLogic");
        String timeInterval = (String) config.get("timeInterval");
        
        while (!remaining.isEmpty()) {
            // 1. 查找最后一个未满的批次
            Map<String, Object> params = new HashMap<>();
            params.put("sourceType", "batch");
            params.put("chainType", chainType);
            params.put("eventsConfig", eventsConfig);
            params.put("triggerLogic", triggerLogic);
            
            Map<String, Object> availableSlot = batchPoolMapper.findLastUnfilledBatch(params);
            
            Integer batchId;
            Long templateId;
            int currentCount;
            
            if (availableSlot == null) {
                // 没有未满批次，创建新批次
                templateId = createNewTemplate(chainType, config);
                batchId = findAvailableBatchId(chainType);
                currentCount = 0;
            } else {
                // 使用现有批次
                batchId = (Integer) availableSlot.get("batchId");
                templateId = ((Number) availableSlot.get("templateId")).longValue();
                currentCount = ((Number) availableSlot.get("currentCount")).intValue();
            }
            
            // 2. 计算可填充数量
            int spaceLeft = 99 - currentCount;
            int toFill = Math.min(spaceLeft, remaining.size());
            
            // 3. 取出要填充的地址
            List<String> toAdd = remaining.subList(0, toFill);
            remaining = new ArrayList<>(remaining.subList(toFill, remaining.size()));
            
            // 4. 批量插入
            batchInsertToPool(batchId, templateId, chainType, toAdd, config, currentCount);
            
            // 5. 记录分配信息
            Map<String, Object> allocation = new HashMap<>();
            allocation.put("batchId", batchId);
            allocation.put("addedCount", toFill);
            allocation.put("totalCount", currentCount + toFill);
            allocations.add(allocation);
        }
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("chainType", chainType);
        result.put("allocations", allocations);
        
        return result;
    }

    /**
     * 创建新的监控模板
     */
    private Long createNewTemplate(String chainType, Map<String, Object> config)
    {
        QuickMonitorTemplate template = new QuickMonitorTemplate();
        template.setSourceType("batch");
        template.setChainType(chainType);
        template.setMinMarketCap(null); // 批量监控不需要市值门槛
        
        // 生成配置名称：SOL批量监控 1、BSC批量监控 2...
        int nextNumber = getNextBatchNumber(chainType);
        String chainDisplayName = "SOL".equalsIgnoreCase(chainType) ? "SOL" : "BSC";
        template.setConfigName(chainDisplayName + "批量监控 " + nextNumber);
        
        template.setTimeInterval((String) config.get("timeInterval"));
        
        // 安全处理 topHoldersThreshold
        Object topHoldersThresholdObj = config.get("topHoldersThreshold");
        if (topHoldersThresholdObj != null) {
            template.setTopHoldersThreshold(new java.math.BigDecimal(topHoldersThresholdObj.toString()));
        } else {
            template.setTopHoldersThreshold(new java.math.BigDecimal("50")); // 默认值50
        }
        
        template.setEventsConfig(JSON.toJSONString(config.get("events")));
        template.setTriggerLogic((String) config.get("triggerLogic"));
        template.setNotifyMethods((String) config.get("notifyMethods"));
        template.setSortOrder(999);
        template.setStatus("1");
        template.setCreateTime(DateUtils.getNowDate());
        
        templateMapper.insertQuickMonitorTemplate(template);
        
        return template.getId();
    }

    /**
     * 查找可用的批次ID
     */
    private Integer findAvailableBatchId(String chainType)
    {
        List<Integer> usedIds = batchPoolMapper.selectUsedBatchIds(chainType, "batch");
        Set<Integer> usedSet = new HashSet<>(usedIds);
        
        // 查找第一个未使用的（1-20）
        for (int i = 1; i <= 20; i++) {
            if (!usedSet.contains(i)) {
                return i;
            }
        }
        
        throw new RuntimeException("批次已满（单链最多20个批次），请先删除不用的批次");
    }

    /**
     * 批量插入到批次表
     */
    private void batchInsertToPool(Integer batchId, Long templateId, String chainType,
                                   List<String> addresses, Map<String, Object> config, int startOrder)
    {
        // 获取模板名称
        QuickMonitorTemplate template = templateMapper.selectQuickMonitorTemplateById(templateId);
        String templateName = template != null ? template.getConfigName() : "批量监控";
        
        List<SolWsBatchPool> records = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            SolWsBatchPool record = new SolWsBatchPool();
            record.setBatchId(batchId);
            record.setSourceType("batch");
            record.setChainType(chainType);
            record.setCa(addresses.get(i));
            record.setTemplateId(templateId);
            record.setTemplateName(templateName);
            record.setTimeInterval((String) config.get("timeInterval"));
            record.setEventsConfig(JSON.toJSONString(config.get("events")));
            record.setTriggerLogic((String) config.get("triggerLogic"));
            record.setPriority(100);
            record.setSortOrder(startOrder + i + 1);
            record.setIsActive(1);
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            records.add(record);
        }
        
        batchPoolMapper.batchInsertSolWsBatchPool(records);
    }

    /**
     * 按链类型分组地址
     */
    private Map<String, List<String>> groupAddressesByChain(List<String> addresses)
    {
        Map<String, List<String>> grouped = new HashMap<>();
        grouped.put("sol", new ArrayList<>());
        grouped.put("bsc", new ArrayList<>());
        
        for (String addr : addresses) {
            if (addr == null || addr.trim().isEmpty()) {
                continue;
            }
            
            addr = addr.trim();
            
            // BSC: 0x + 40位十六进制
            if (addr.matches("^0x[a-fA-F0-9]{40}$")) {
                grouped.get("bsc").add(addr);
            }
            // SOL: Base58, 32-44位
            else if (addr.matches("^[1-9A-HJ-NP-Za-km-z]{32,44}$")) {
                grouped.get("sol").add(addr);
            }
        }
        
        return grouped;
    }

    /**
     * 查询批量监控列表（带统计信息）
     */
    @Override
    public List<Map<String, Object>> getBatchMonitorList(String sourceType, String chainType)
    {
        SolWsBatchPool query = new SolWsBatchPool();
        query.setSourceType(sourceType);
        query.setChainType(chainType);
        query.setIsActive(1);
        
        return batchPoolMapper.selectBatchListWithStats(query);
    }

    /**
     * 查询指定批次的Token列表
     */
    @Override
    public List<Map<String, Object>> getTokensInBatch(Integer batchId, String sourceType, String chainType)
    {
        SolWsBatchPool query = new SolWsBatchPool();
        query.setBatchId(batchId);
        query.setSourceType(sourceType);
        query.setChainType(chainType);
        query.setIsActive(1);
        
        List<SolWsBatchPool> list = batchPoolMapper.selectSolWsBatchPoolList(query);
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (SolWsBatchPool pool : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", pool.getId());
            item.put("ca", pool.getCa());
            item.put("tokenSymbol", pool.getTokenSymbol());
            item.put("tokenName", pool.getTokenName());
            item.put("marketCap", pool.getMarketCap());
            item.put("createTime", pool.getCreateTime());
            result.add(item);
        }
        
        return result;
    }

    /**
     * 删除批次
     */
    @Override
    @Transactional
    public int deleteBatch(Integer batchId, String sourceType, String chainType)
    {
        return batchPoolMapper.deleteSolWsBatchPoolByBatchId(batchId, sourceType, chainType);
    }

    /**
     * 启用/停用批次
     */
    @Override
    @Transactional
    public int toggleBatchStatus(Integer batchId, String sourceType, String chainType, Integer isActive)
    {
        SolWsBatchPool update = new SolWsBatchPool();
        update.setIsActive(isActive);
        
        // 需要按批次ID批量更新
        SolWsBatchPool query = new SolWsBatchPool();
        query.setBatchId(batchId);
        query.setSourceType(sourceType);
        query.setChainType(chainType);
        
        List<SolWsBatchPool> list = batchPoolMapper.selectSolWsBatchPoolList(query);
        int count = 0;
        for (SolWsBatchPool pool : list) {
            pool.setIsActive(isActive);
            pool.setUpdateTime(new Date());
            count += batchPoolMapper.updateSolWsBatchPool(pool);
        }
        
        return count;
    }

    /**
     * 获取该链类型下一个批量监控的序号
     */
    private int getNextBatchNumber(String chainType)
    {
        // 查询该链类型下所有 source_type='batch' 的模板
        QuickMonitorTemplate query = new QuickMonitorTemplate();
        query.setSourceType("batch");
        query.setChainType(chainType);
        
        List<QuickMonitorTemplate> existingTemplates = templateMapper.selectQuickMonitorTemplateList(query);
        
        // 返回下一个序号
        return existingTemplates.size() + 1;
    }
}

