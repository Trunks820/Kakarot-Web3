package com.ruoyi.crypto.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.crypto.mapper.TokenMonitorAlertLogMapper;
import com.ruoyi.crypto.domain.TokenMonitorAlertLog;
import com.ruoyi.crypto.service.ITokenMonitorAlertLogService;

/**
 * Token监控预警日志Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-01-24
 */
@Service
public class TokenMonitorAlertLogServiceImpl implements ITokenMonitorAlertLogService 
{
    @Autowired
    private TokenMonitorAlertLogMapper tokenMonitorAlertLogMapper;

    /**
     * 查询Token监控预警日志
     * 
     * @param id Token监控预警日志主键
     * @return Token监控预警日志
     */
    @Override
    public TokenMonitorAlertLog selectTokenMonitorAlertLogById(Long id)
    {
        return tokenMonitorAlertLogMapper.selectTokenMonitorAlertLogById(id);
    }

    /**
     * 查询Token监控预警日志列表
     * 
     * @param tokenMonitorAlertLog Token监控预警日志
     * @return Token监控预警日志
     */
    @Override
    public List<TokenMonitorAlertLog> selectTokenMonitorAlertLogList(TokenMonitorAlertLog tokenMonitorAlertLog)
    {
        return tokenMonitorAlertLogMapper.selectTokenMonitorAlertLogList(tokenMonitorAlertLog);
    }

    /**
     * 新增Token监控预警日志
     * 
     * @param tokenMonitorAlertLog Token监控预警日志
     * @return 结果
     */
    @Override
    public int insertTokenMonitorAlertLog(TokenMonitorAlertLog tokenMonitorAlertLog)
    {
        return tokenMonitorAlertLogMapper.insertTokenMonitorAlertLog(tokenMonitorAlertLog);
    }

    /**
     * 修改Token监控预警日志
     * 
     * @param tokenMonitorAlertLog Token监控预警日志
     * @return 结果
     */
    @Override
    public int updateTokenMonitorAlertLog(TokenMonitorAlertLog tokenMonitorAlertLog)
    {
        return tokenMonitorAlertLogMapper.updateTokenMonitorAlertLog(tokenMonitorAlertLog);
    }

    /**
     * 批量删除Token监控预警日志
     * 
     * @param ids 需要删除的Token监控预警日志主键
     * @return 结果
     */
    @Override
    public int deleteTokenMonitorAlertLogByIds(Long[] ids)
    {
        return tokenMonitorAlertLogMapper.deleteTokenMonitorAlertLogByIds(ids);
    }

    /**
     * 删除Token监控预警日志信息
     * 
     * @param id Token监控预警日志主键
     * @return 结果
     */
    @Override
    public int deleteTokenMonitorAlertLogById(Long id)
    {
        return tokenMonitorAlertLogMapper.deleteTokenMonitorAlertLogById(id);
    }
}

