package com.ruoyi.crypto.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.crypto.mapper.SolAlertLogMapper;
import com.ruoyi.crypto.domain.SolAlertLog;
import com.ruoyi.crypto.service.ISolAlertLogService;

import javax.annotation.Resource;

/**
 * SOL告警日志Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Service
public class SolAlertLogServiceImpl implements ISolAlertLogService 
{
    @Resource
    private SolAlertLogMapper solAlertLogMapper;

    /**
     * 查询SOL告警日志
     * 
     * @param id SOL告警日志主键
     * @return SOL告警日志
     */
    @Override
    public SolAlertLog selectSolAlertLogById(Long id)
    {
        return solAlertLogMapper.selectSolAlertLogById(id);
    }

    /**
     * 查询SOL告警日志列表
     * 
     * @param solAlertLog SOL告警日志
     * @return SOL告警日志
     */
    @Override
    public List<SolAlertLog> selectSolAlertLogList(SolAlertLog solAlertLog)
    {
        return solAlertLogMapper.selectSolAlertLogList(solAlertLog);
    }

    /**
     * 查询最近的SOL告警记录
     * 
     * @param limit 查询数量
     * @return SOL告警日志集合
     */
    @Override
    public List<SolAlertLog> selectRecentSolAlerts(Integer limit)
    {
        return solAlertLogMapper.selectRecentSolAlerts(limit);
    }

    /**
     * 新增SOL告警日志
     * 
     * @param solAlertLog SOL告警日志
     * @return 结果
     */
    @Override
    public int insertSolAlertLog(SolAlertLog solAlertLog)
    {
        return solAlertLogMapper.insertSolAlertLog(solAlertLog);
    }

    /**
     * 修改SOL告警日志
     * 
     * @param solAlertLog SOL告警日志
     * @return 结果
     */
    @Override
    public int updateSolAlertLog(SolAlertLog solAlertLog)
    {
        return solAlertLogMapper.updateSolAlertLog(solAlertLog);
    }

    /**
     * 批量删除SOL告警日志
     * 
     * @param ids 需要删除的数据主键
     * @return 结果
     */
    @Override
    public int deleteSolAlertLogByIds(Long[] ids)
    {
        return solAlertLogMapper.deleteSolAlertLogByIds(ids);
    }

    /**
     * 删除SOL告警日志信息
     * 
     * @param id SOL告警日志主键
     * @return 结果
     */
    @Override
    public int deleteSolAlertLogById(Long id)
    {
        return solAlertLogMapper.deleteSolAlertLogById(id);
    }
}

