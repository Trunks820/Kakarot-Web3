package com.ruoyi.crypto.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.crypto.mapper.MonitorAlertLogMapper;
import com.ruoyi.crypto.domain.MonitorAlertLog;
import com.ruoyi.crypto.service.IMonitorAlertLogService;

/**
 * 监控告警日志Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
@Service
public class MonitorAlertLogServiceImpl implements IMonitorAlertLogService 
{
    @Autowired
    private MonitorAlertLogMapper monitorAlertLogMapper;

    /**
     * 查询监控告警日志
     * 
     * @param id 监控告警日志主键
     * @return 监控告警日志
     */
    @Override
    public MonitorAlertLog selectMonitorAlertLogById(Long id)
    {
        return monitorAlertLogMapper.selectMonitorAlertLogById(id);
    }

    /**
     * 查询监控告警日志列表
     * 
     * @param monitorAlertLog 监控告警日志
     * @return 监控告警日志
     */
    @Override
    public List<MonitorAlertLog> selectMonitorAlertLogList(MonitorAlertLog monitorAlertLog)
    {
        return monitorAlertLogMapper.selectMonitorAlertLogList(monitorAlertLog);
    }

    /**
     * 新增监控告警日志
     * 
     * @param monitorAlertLog 监控告警日志
     * @return 结果
     */
    @Override
    public int insertMonitorAlertLog(MonitorAlertLog monitorAlertLog)
    {
        return monitorAlertLogMapper.insertMonitorAlertLog(monitorAlertLog);
    }

    /**
     * 修改监控告警日志
     * 
     * @param monitorAlertLog 监控告警日志
     * @return 结果
     */
    @Override
    public int updateMonitorAlertLog(MonitorAlertLog monitorAlertLog)
    {
        return monitorAlertLogMapper.updateMonitorAlertLog(monitorAlertLog);
    }

    /**
     * 批量删除监控告警日志
     * 
     * @param ids 需要删除的监控告警日志主键
     * @return 结果
     */
    @Override
    public int deleteMonitorAlertLogByIds(Long[] ids)
    {
        return monitorAlertLogMapper.deleteMonitorAlertLogByIds(ids);
    }

    /**
     * 删除监控告警日志信息
     * 
     * @param id 监控告警日志主键
     * @return 结果
     */
    @Override
    public int deleteMonitorAlertLogById(Long id)
    {
        return monitorAlertLogMapper.deleteMonitorAlertLogById(id);
    }

    /**
     * 标记告警为已读
     * 
     * @param id 告警ID
     * @return 结果
     */
    @Override
    public int markAsRead(Long id)
    {
        return monitorAlertLogMapper.markAsRead(id);
    }

    /**
     * 标记告警为已处理
     * 
     * @param id 告警ID
     * @param handleBy 处理人
     * @param handleRemark 处理备注
     * @return 结果
     */
    @Override
    public int markAsHandled(Long id, String handleBy, String handleRemark)
    {
        return monitorAlertLogMapper.markAsHandled(id, handleBy, handleRemark);
    }

    /**
     * 批量标记告警为已处理
     * 
     * @param ids 告警ID数组
     * @param handleBy 处理人
     * @return 结果
     */
    @Override
    public int batchMarkAsHandled(Long[] ids, String handleBy)
    {
        return monitorAlertLogMapper.batchMarkAsHandled(ids, handleBy);
    }

    /**
     * 查询告警统计
     * 
     * @return 统计数据
     */
    @Override
    public Map<String, Object> selectAlertStats()
    {
        return monitorAlertLogMapper.selectAlertStats();
    }
}

