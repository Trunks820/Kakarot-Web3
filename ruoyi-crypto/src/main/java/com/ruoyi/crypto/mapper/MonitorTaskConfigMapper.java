package com.ruoyi.crypto.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 任务配置关联Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
public interface MonitorTaskConfigMapper 
{
    /**
     * 插入任务配置关联
     * 
     * @param taskId 任务ID
     * @param configId 配置ID
     * @return 结果
     */
    public int insertTaskConfig(@Param("taskId") Long taskId, @Param("configId") Long configId);

    /**
     * 删除任务配置关联
     * 
     * @param taskId 任务ID
     * @return 结果
     */
    public int deleteTaskConfigByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据配置ID删除关联
     * 
     * @param configId 配置ID
     * @return 结果
     */
    public int deleteTaskConfigByConfigId(@Param("configId") Long configId);
}

