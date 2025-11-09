package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.MonitorTask;
import java.util.List;

/**
 * 监控任务Service接口
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
public interface IMonitorTaskService 
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
     * 新增智能监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    public int insertSmartTask(MonitorTask monitorTask);

    /**
     * 新增批量监控任务
     * 
     * @param monitorTask 监控任务
     * @param targetAddresses 目标地址列表
     * @return 结果
     */
    public int insertBatchTask(MonitorTask monitorTask, List<String> targetAddresses);

    /**
     * 新增区块监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    public int insertBlockTask(MonitorTask monitorTask);

    /**
     * 修改监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    public int updateMonitorTask(MonitorTask monitorTask);

    /**
     * 批量删除监控任务
     * 
     * @param ids 需要删除的监控任务主键集合
     * @return 结果
     */
    public int deleteMonitorTaskByIds(Long[] ids);

    /**
     * 删除监控任务信息
     * 
     * @param id 监控任务主键
     * @return 结果
     */
    public int deleteMonitorTaskById(Long id);

    /**
     * 启动任务
     * 
     * @param id 任务ID
     * @return 结果
     */
    public int startTask(Long id);

    /**
     * 停止任务
     * 
     * @param id 任务ID
     * @return 结果
     */
    public int stopTask(Long id);

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
     * @param taskType 任务类型
     * @param chainType 链类型
     * @param status 状态
     * @return 任务数量
     */
    public int countMonitorTask(String taskType, String chainType, Integer status);
}

