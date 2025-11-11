package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.service.ISmartBatchService;
import com.ruoyi.crypto.util.ConsistentHashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能批次分配测试接口（临时测试用，上线后可删除）
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@RestController
@RequestMapping("/crypto/test/smart-batch")
public class SmartBatchTestController extends BaseController {
    
    @Autowired
    private ISmartBatchService smartBatchService;
    
    @Autowired
    private ConsistentHashUtil consistentHashUtil;
    
    /**
     * 测试智能目标同步和批次分配
     */
    @PostMapping("/task/{taskId}/sync")
    public AjaxResult testSyncTargetsAndAllocate(@PathVariable Long taskId) {
        try {
            logger.info("========================================");
            logger.info("开始测试智能批次分配：taskId={}", taskId);
            logger.info("========================================");
            
            // 执行同步
            Map<String, Object> result = smartBatchService.syncTargetsAndAllocateBatches(taskId);
            
            logger.info("========================================");
            logger.info("智能批次分配测试完成");
            logger.info("结果：{}", result);
            logger.info("========================================");
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("测试智能批次分配失败：taskId=" + taskId, e);
            return AjaxResult.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试一致性哈希功能
     */
    @GetMapping("/consistent-hash/test")
    public AjaxResult testConsistentHash(
            @RequestParam(defaultValue = "consumer-1,consumer-2,consumer-3") String consumers,
            @RequestParam(defaultValue = "0x123,0x456,0x789") String cas) {
        
        try {
            // 解析Consumer列表
            List<String> consumerList = Arrays.asList(consumers.split(","));
            
            // 更新一致性哈希环
            consistentHashUtil.updateConsumers(consumerList);
            
            // 解析CA列表
            List<String> caList = Arrays.asList(cas.split(","));
            
            // 测试CA分配
            Map<String, Object> result = new HashMap<>();
            result.put("consumers", consumerList);
            result.put("cas", caList);
            result.put("fixedBucketCount", consistentHashUtil.getFixedBucketCount());
            result.put("virtualNodes", consistentHashUtil.getVirtualNodes());
            
            // CA到Consumer的映射
            Map<String, String> caToConsumer = new HashMap<>();
            for (String ca : caList) {
                int bucketId = consistentHashUtil.getBucketForCA(ca);
                String consumerId = consistentHashUtil.getConsumerForBucket(bucketId);
                caToConsumer.put(ca, consumerId + " (bucket:" + bucketId + ")");
            }
            result.put("caToConsumer", caToConsumer);
            
            // 按Consumer分组
            Map<String, List<String>> groupedCAs = consistentHashUtil.groupCAsByConsumer(caList);
            result.put("groupedByConsumer", groupedCAs);
            
            // 所有槽位分配情况
            Map<String, List<Integer>> bucketAssignments = consistentHashUtil.getAllBucketAssignments();
            Map<String, Integer> bucketCounts = new HashMap<>();
            for (Map.Entry<String, List<Integer>> entry : bucketAssignments.entrySet()) {
                bucketCounts.put(entry.getKey(), entry.getValue().size());
            }
            result.put("bucketDistribution", bucketCounts);
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("测试一致性哈希失败", e);
            return AjaxResult.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看当前Consumer列表
     */
    @GetMapping("/consumers")
    public AjaxResult getCurrentConsumers() {
        List<String> consumers = consistentHashUtil.getCurrentConsumers();
        
        Map<String, Object> result = new HashMap<>();
        result.put("consumers", consumers);
        result.put("count", consumers.size());
        result.put("fixedBucketCount", consistentHashUtil.getFixedBucketCount());
        result.put("virtualNodes", consistentHashUtil.getVirtualNodes());
        
        return AjaxResult.success(result);
    }
    
    /**
     * 手动设置Consumer列表
     */
    @PostMapping("/consumers/set")
    public AjaxResult setConsumers(@RequestBody List<String> consumers) {
        try {
            logger.info("手动设置Consumer列表: {}", consumers);
            consistentHashUtil.updateConsumers(consumers);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Consumer列表更新成功");
            result.put("consumers", consumers);
            result.put("count", consumers.size());
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("设置Consumer列表失败", e);
            return AjaxResult.error("设置失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试归档功能
     */
    @PostMapping("/task/{taskId}/archive")
    public AjaxResult testArchive(
            @PathVariable Long taskId,
            @RequestParam Integer currentEpoch) {
        
        try {
            logger.info("开始测试归档功能：taskId={}, currentEpoch={}", taskId, currentEpoch);
            
            int archivedCount = smartBatchService.archiveOldBatches(taskId, currentEpoch);
            
            Map<String, Object> result = new HashMap<>();
            result.put("taskId", taskId);
            result.put("currentEpoch", currentEpoch);
            result.put("archivedCount", archivedCount);
            result.put("message", "归档完成");
            
            logger.info("归档测试完成：archivedCount={}", archivedCount);
            
            return AjaxResult.success(result);
            
        } catch (Exception e) {
            logger.error("测试归档功能失败", e);
            return AjaxResult.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public AjaxResult healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("consistentHashUtil", "initialized");
        health.put("smartBatchService", "initialized");
        health.put("currentConsumers", consistentHashUtil.getCurrentConsumers().size());
        health.put("timestamp", System.currentTimeMillis());
        
        return AjaxResult.success(health);
    }
}

