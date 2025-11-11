package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.MonitorTaskTarget;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务目标Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
public interface MonitorTaskTargetMapper 
{
    /**
     * 插入任务目标
     * 
     * @param taskId 任务ID
     * @param chainType 链类型
     * @param targetAddress 目标地址
     * @return 结果
     */
    public int insertTaskTarget(@Param("taskId") Long taskId, @Param("chainType") String chainType, @Param("targetAddress") String targetAddress);

    /**
     * 新增任务目标（对象方式）
     * 
     * @param target 任务目标
     * @return 结果
     */
    public int insertMonitorTaskTarget(MonitorTaskTarget target);
    
    /**
     * 批量新增监控任务目标
     * 
     * @param targets 目标列表
     * @return 结果
     */
    public int insertBatchTargets(List<MonitorTaskTarget> targets);

    /**
     * 查询任务的所有目标
     * 
     * @param taskId 任务ID
     * @return 目标列表
     */
    public List<MonitorTaskTarget> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 删除任务目标
     * 
     * @param taskId 任务ID
     * @return 结果
     */
    public int deleteTaskTargetByTaskId(@Param("taskId") Long taskId);

    /**
     * 删除指定任务的指定目标
     * 
     * @param taskId 任务ID
     * @param ca 合约地址
     * @return 结果
     */
    public int deleteByTaskIdAndCa(@Param("taskId") Long taskId, @Param("ca") String ca);
}

