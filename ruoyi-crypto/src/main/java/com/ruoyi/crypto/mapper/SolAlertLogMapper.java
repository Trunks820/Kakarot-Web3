package com.ruoyi.crypto.mapper;

import java.util.List;
import com.ruoyi.crypto.domain.SolAlertLog;
import org.apache.ibatis.annotations.Param;

/**
 * SOL告警日志Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
public interface SolAlertLogMapper 
{
    /**
     * 查询SOL告警日志
     * 
     * @param id SOL告警日志主键
     * @return SOL告警日志
     */
    public SolAlertLog selectSolAlertLogById(Long id);

    /**
     * 查询SOL告警日志列表
     * 
     * @param solAlertLog SOL告警日志
     * @return SOL告警日志集合
     */
    public List<SolAlertLog> selectSolAlertLogList(SolAlertLog solAlertLog);

    /**
     * 查询最近的SOL告警记录
     * 
     * @param limit 查询数量
     * @return SOL告警日志集合
     */
    public List<SolAlertLog> selectRecentSolAlerts(@Param("limit") Integer limit);

    /**
     * 新增SOL告警日志
     * 
     * @param solAlertLog SOL告警日志
     * @return 结果
     */
    public int insertSolAlertLog(SolAlertLog solAlertLog);

    /**
     * 修改SOL告警日志
     * 
     * @param solAlertLog SOL告警日志
     * @return 结果
     */
    public int updateSolAlertLog(SolAlertLog solAlertLog);

    /**
     * 删除SOL告警日志
     * 
     * @param id SOL告警日志主键
     * @return 结果
     */
    public int deleteSolAlertLogById(Long id);

    /**
     * 批量删除SOL告警日志
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSolAlertLogByIds(Long[] ids);
}

