package com.ruoyi.crypto.service;

import java.util.List;
import java.util.Map;

import com.ruoyi.crypto.domain.MonitorAlertLog;

/**
 * 监控告警日志Service接口
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
public interface IMonitorAlertLogService 
{
    /**
     * 查询监控告警日志
     * 
     * @param id 监控告警日志主键
     * @return 监控告警日志
     */
    public MonitorAlertLog selectMonitorAlertLogById(Long id);

    /**
     * 查询监控告警日志列表
     * 
     * @param monitorAlertLog 监控告警日志
     * @return 监控告警日志集合
     */
    public List<MonitorAlertLog> selectMonitorAlertLogList(MonitorAlertLog monitorAlertLog);

    /**
     * 新增监控告警日志
     * 
     * @param monitorAlertLog 监控告警日志
     * @return 结果
     */
    public int insertMonitorAlertLog(MonitorAlertLog monitorAlertLog);

    /**
     * 修改监控告警日志
     * 
     * @param monitorAlertLog 监控告警日志
     * @return 结果
     */
    public int updateMonitorAlertLog(MonitorAlertLog monitorAlertLog);

    /**
     * 批量删除监控告警日志
     * 
     * @param ids 需要删除的监控告警日志主键集合
     * @return 结果
     */
    public int deleteMonitorAlertLogByIds(Long[] ids);

    /**
     * 删除监控告警日志信息
     * 
     * @param id 监控告警日志主键
     * @return 结果
     */
    public int deleteMonitorAlertLogById(Long id);

    /**
     * 标记告警为已读
     * 
     * @param id 告警ID
     * @return 结果
     */
    public int markAsRead(Long id);

    /**
     * 标记告警为已处理
     * 
     * @param id 告警ID
     * @param handleBy 处理人
     * @param handleRemark 处理备注
     * @return 结果
     */
    public int markAsHandled(Long id, String handleBy, String handleRemark);

    /**
     * 批量标记告警为已处理
     * 
     * @param ids 告警ID数组
     * @param handleBy 处理人
     * @return 结果
     */
    public int batchMarkAsHandled(Long[] ids, String handleBy);

    /**
     * 查询告警统计
     * 
     * @return 统计数据
     */
    public Map<String, Object> selectAlertStats();
}

