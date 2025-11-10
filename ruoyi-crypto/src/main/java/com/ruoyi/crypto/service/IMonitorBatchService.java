package com.ruoyi.crypto.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.crypto.domain.MonitorBatch;
import com.ruoyi.crypto.domain.MonitorBatchItem;

/**
 * 批次管理Service接口
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
public interface IMonitorBatchService 
{
    /**
     * 获取批次统计信息
     * 
     * @return 批次统计数据
     */
    public Map<String, Object> getBatchStats();

    /**
     * 查询批次
     * 
     * @param id 批次主键
     * @return 批次
     */
    public MonitorBatch selectMonitorBatchById(Long id);

    /**
     * 查询批次列表
     * 
     * @param monitorBatch 批次
     * @return 批次集合
     */
    public List<MonitorBatch> selectMonitorBatchList(MonitorBatch monitorBatch);

    /**
     * 查询批次项列表
     * 
     * @param batchId 批次ID
     * @return 批次项集合
     */
    public List<MonitorBatchItem> selectBatchItems(Long batchId);

    /**
     * 新增批次
     * 
     * @param monitorBatch 批次
     * @return 结果
     */
    public int insertMonitorBatch(MonitorBatch monitorBatch);

    /**
     * 修改批次
     * 
     * @param monitorBatch 批次
     * @return 结果
     */
    public int updateMonitorBatch(MonitorBatch monitorBatch);

    /**
     * 批量删除批次
     * 
     * @param ids 需要删除的批次主键集合
     * @return 结果
     */
    public int deleteMonitorBatchByIds(Long[] ids);

    /**
     * 删除批次信息
     * 
     * @param id 批次主键
     * @return 结果
     */
    public int deleteMonitorBatchById(Long id);

    /**
     * 重启批次
     * 
     * @param id 批次主键
     * @return 结果
     */
    public int restartBatch(Long id);

    /**
     * 获取批次心跳状态
     * 
     * @param id 批次主键
     * @return 心跳信息
     */
    public Map<String, Object> getBatchHeartbeat(Long id);

    /**
     * 更新批次心跳
     * 
     * @param id 批次主键
     * @param data 心跳数据
     * @return 结果
     */
    public int updateBatchHeartbeat(Long id, Map<String, Object> data);
}

