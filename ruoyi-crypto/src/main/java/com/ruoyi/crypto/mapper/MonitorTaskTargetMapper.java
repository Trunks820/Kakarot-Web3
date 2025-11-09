package com.ruoyi.crypto.mapper;

import org.apache.ibatis.annotations.Param;

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
     * 删除任务目标
     * 
     * @param taskId 任务ID
     * @return 结果
     */
    public int deleteTaskTargetByTaskId(@Param("taskId") Long taskId);
}

