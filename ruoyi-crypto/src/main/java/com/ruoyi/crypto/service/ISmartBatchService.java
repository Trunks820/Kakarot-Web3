package com.ruoyi.crypto.service;

import java.util.Map;

/**
 * 智能批次分配Service接口
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
public interface ISmartBatchService {
    
    /**
     * 执行智能目标同步和批次分配
     * 
     * @param taskId 任务ID
     * @return 同步结果
     */
    Map<String, Object> syncTargetsAndAllocateBatches(Long taskId);

    Map<String, Object> syncTargetsAndAllocateBatches();

    /**
     * 归档旧epoch的批次数据
     * 
     * @param taskId 任务ID
     * @param currentEpoch 当前epoch
     * @return 归档数量
     */
    int archiveOldBatches(Long taskId, Integer currentEpoch);

    /**
     * 为任务分配批次（通用方法，适用于智能/批量任务）
     *
     * @param taskId 任务ID
     * @return 分配结果
     */
    Map<String, Object> allocateBatchesForTask(Long taskId);
}

