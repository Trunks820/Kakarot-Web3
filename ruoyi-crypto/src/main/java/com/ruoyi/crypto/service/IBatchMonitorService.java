package com.ruoyi.crypto.service;

import java.util.List;
import java.util.Map;

/**
 * 批量监控Service接口
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
public interface IBatchMonitorService 
{
    /**
     * 智能批量添加监控（自动分配批次并填充）
     * 
     * @param addresses Token地址列表
     * @param config 监控配置
     * @return 批量添加结果
     */
    Map<String, Object> smartBatchAdd(List<String> addresses, Map<String, Object> config);

    /**
     * 查询批量监控列表（带统计信息）
     * 
     * @param sourceType 来源类型
     * @param chainType 链类型
     * @return 批次列表
     */
    List<Map<String, Object>> getBatchMonitorList(String sourceType, String chainType);

    /**
     * 查询指定批次的Token列表
     * 
     * @param batchId 批次ID
     * @param sourceType 来源类型
     * @param chainType 链类型
     * @return Token列表
     */
    List<Map<String, Object>> getTokensInBatch(Integer batchId, String sourceType, String chainType);

    /**
     * 删除批次
     * 
     * @param batchId 批次ID
     * @param sourceType 来源类型
     * @param chainType 链类型
     * @return 结果
     */
    int deleteBatch(Integer batchId, String sourceType, String chainType);

    /**
     * 启用/停用批次
     * 
     * @param batchId 批次ID
     * @param sourceType 来源类型
     * @param chainType 链类型
     * @param isActive 是否激活
     * @return 结果
     */
    int toggleBatchStatus(Integer batchId, String sourceType, String chainType, Integer isActive);
}

