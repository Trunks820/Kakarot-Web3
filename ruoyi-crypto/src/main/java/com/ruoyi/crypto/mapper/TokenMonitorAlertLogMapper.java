package com.ruoyi.crypto.mapper;

import java.util.List;
import com.ruoyi.crypto.domain.TokenMonitorAlertLog;

/**
 * Token监控预警日志Mapper接口
 * 
 * @author ruoyi
 * @date 2025-01-24
 */
public interface TokenMonitorAlertLogMapper 
{
    /**
     * 查询Token监控预警日志
     * 
     * @param id Token监控预警日志主键
     * @return Token监控预警日志
     */
    public TokenMonitorAlertLog selectTokenMonitorAlertLogById(Long id);

    /**
     * 查询Token监控预警日志列表
     * 
     * @param tokenMonitorAlertLog Token监控预警日志
     * @return Token监控预警日志集合
     */
    public List<TokenMonitorAlertLog> selectTokenMonitorAlertLogList(TokenMonitorAlertLog tokenMonitorAlertLog);

    /**
     * 新增Token监控预警日志
     * 
     * @param tokenMonitorAlertLog Token监控预警日志
     * @return 结果
     */
    public int insertTokenMonitorAlertLog(TokenMonitorAlertLog tokenMonitorAlertLog);

    /**
     * 修改Token监控预警日志
     * 
     * @param tokenMonitorAlertLog Token监控预警日志
     * @return 结果
     */
    public int updateTokenMonitorAlertLog(TokenMonitorAlertLog tokenMonitorAlertLog);

    /**
     * 删除Token监控预警日志
     * 
     * @param id Token监控预警日志主键
     * @return 结果
     */
    public int deleteTokenMonitorAlertLogById(Long id);

    /**
     * 批量删除Token监控预警日志
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTokenMonitorAlertLogByIds(Long[] ids);
}

