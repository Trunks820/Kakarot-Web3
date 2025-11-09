package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.MonitorTask;
import java.util.List;

/**
 * 监控任务Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
public interface MonitorTaskMapper 
{
    /**
     * 查询监控任务
     * 
     * @param id 监控任务主键
     * @return 监控任务
     */
    public MonitorTask selectMonitorTaskById(Long id);

    /**
     * 查询监控任务列表
     * 
     * @param monitorTask 监控任务
     * @return 监控任务集合
     */
    public List<MonitorTask> selectMonitorTaskList(MonitorTask monitorTask);

    /**
     * 新增监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    public int insertMonitorTask(MonitorTask monitorTask);

    /**
     * 修改监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    public int updateMonitorTask(MonitorTask monitorTask);

    /**
     * 删除监控任务
     * 
     * @param id 监控任务主键
     * @return 结果
     */
    public int deleteMonitorTaskById(Long id);

    /**
     * 批量删除监控任务
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMonitorTaskByIds(Long[] ids);

    /**
     * 根据链类型查询任务列表
     * 
     * @param chainType 链类型
     * @return 监控任务集合
     */
    public List<MonitorTask> selectMonitorTaskByChainType(String chainType);

    /**
     * 统计任务数量
     * 
     * @param monitorTask 查询条件
     * @return 任务数量
     */
    public int countMonitorTask(MonitorTask monitorTask);
}

