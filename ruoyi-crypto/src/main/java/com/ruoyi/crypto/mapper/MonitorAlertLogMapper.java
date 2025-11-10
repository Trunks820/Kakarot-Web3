package com.ruoyi.crypto.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.crypto.domain.MonitorAlertLog;
import org.apache.ibatis.annotations.Param;

/**
 * 监控告警日志Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
public interface MonitorAlertLogMapper 
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
     * 删除监控告警日志
     * 
     * @param id 监控告警日志主键
     * @return 结果
     */
    public int deleteMonitorAlertLogById(Long id);

    /**
     * 批量删除监控告警日志
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMonitorAlertLogByIds(Long[] ids);

    /**
     * 标记告警为已读
     * 
     * @param id 告警ID
     * @return 结果
     */
    public int markAsRead(@Param("id") Long id);

    /**
     * 标记告警为已处理
     * 
     * @param id 告警ID
     * @param handleBy 处理人
     * @param handleRemark 处理备注
     * @return 结果
     */
    public int markAsHandled(@Param("id") Long id, @Param("handleBy") String handleBy, @Param("handleRemark") String handleRemark);

    /**
     * 批量标记告警为已处理
     * 
     * @param ids 告警ID数组
     * @param handleBy 处理人
     * @return 结果
     */
    public int batchMarkAsHandled(@Param("ids") Long[] ids, @Param("handleBy") String handleBy);

    /**
     * 查询告警统计
     * 
     * @return 统计数据
     */
    public Map<String, Object> selectAlertStats();
}

