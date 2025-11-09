package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.MonitorConfig;
import java.util.List;

/**
 * 监控配置Service接口
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
public interface IMonitorConfigService 
{
    /**
     * 查询监控配置
     * 
     * @param id 监控配置主键
     * @return 监控配置
     */
    public MonitorConfig selectMonitorConfigById(Long id);

    /**
     * 查询监控配置列表
     * 
     * @param monitorConfig 监控配置
     * @return 监控配置集合
     */
    public List<MonitorConfig> selectMonitorConfigList(MonitorConfig monitorConfig);

    /**
     * 新增监控配置
     * 
     * @param monitorConfig 监控配置
     * @return 结果
     */
    public int insertMonitorConfig(MonitorConfig monitorConfig);

    /**
     * 修改监控配置
     * 
     * @param monitorConfig 监控配置
     * @return 结果
     */
    public int updateMonitorConfig(MonitorConfig monitorConfig);

    /**
     * 批量删除监控配置
     * 
     * @param ids 需要删除的监控配置主键集合
     * @return 结果
     */
    public int deleteMonitorConfigByIds(Long[] ids);

    /**
     * 删除监控配置信息
     * 
     * @param id 监控配置主键
     * @return 结果
     */
    public int deleteMonitorConfigById(Long id);

    /**
     * 修改配置状态
     * 
     * @param id 配置ID
     * @param status 状态
     * @return 结果
     */
    public int changeStatus(Long id, Integer status);

    /**
     * 根据链类型查询配置列表
     * 
     * @param chainType 链类型
     * @return 监控配置集合
     */
    public List<MonitorConfig> selectMonitorConfigByChainType(String chainType);

    /**
     * 统计配置数量
     * 
     * @param chainType 链类型
     * @param status 状态
     * @return 配置数量
     */
    public int countMonitorConfig(String chainType, Integer status);
}

