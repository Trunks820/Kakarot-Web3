package com.ruoyi.crypto.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.crypto.domain.MonitorBatch;
import com.ruoyi.crypto.domain.MonitorBatchItem;
import org.apache.ibatis.annotations.Param;

/**
 * 批次管理Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
public interface MonitorBatchMapper 
{
    /**
     * 查询批次总数
     * 
     * @return 批次总数
     */
    public int countTotal();

    /**
     * 根据状态查询批次数量
     * 
     * @param status 状态
     * @return 批次数量
     */
    public int countByStatus(@Param("status") String status);

    /**
     * 查询目标总数
     * 
     * @return 目标总数
     */
    public int countTotalTargets();

    /**
     * 查询心跳正常的批次数量（最近5分钟有心跳）
     * 
     * @return 心跳正常数量
     */
    public int countHeartbeatNormal();

    /**
     * 查询异常批次列表
     * 
     * @return 异常批次列表
     */
    public List<Map<String, Object>> selectErrorBatches();

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
    public List<MonitorBatchItem> selectBatchItems(@Param("batchId") Long batchId);

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
     * 删除批次
     * 
     * @param id 批次主键
     * @return 结果
     */
    public int deleteMonitorBatchById(Long id);

    /**
     * 批量删除批次
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMonitorBatchByIds(Long[] ids);
    
    /**
     * 获取批次统计数据
     * 
     * @return 统计数据Map
     */
    public java.util.Map<String, Object> getBatchStats();

    /**
     * 归档旧epoch的批次（标记archived_time）
     * 
     * @param taskId 任务ID
     * @param currentEpoch 当前epoch
     * @return 归档数量
     */
    public int archiveOldEpochs(@Param("taskId") Long taskId, @Param("currentEpoch") Integer currentEpoch);

    /**
     * 删除任务的所有批次项
     * 
     * @param taskId 任务ID
     * @return 删除数量
     */
    public int deleteBatchItemsByTaskId(@Param("param1") Long taskId, @Param("param2") Integer beforeEpoch);

    /**
     * 删除任务的旧epoch批次
     * 
     * @param taskId 任务ID
     * @param beforeEpoch 删除小于此epoch的批次
     * @return 删除数量
     */
    public int deleteBatchesByTaskId(@Param("param1") Long taskId, @Param("param2") Integer beforeEpoch);

    /**
     * 查询指定任务已存在的最大epoch（用于防止重复epoch导致唯一索引冲突）
     *
     * @param taskId 任务ID
     * @return 最大epoch，若不存在则返回null
     */
    public Integer selectMaxEpochByTaskId(@Param("taskId") Long taskId);
}

