package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crypto.domain.MonitorConfig;
import com.ruoyi.crypto.mapper.MonitorConfigMapper;
import com.ruoyi.crypto.service.IMonitorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 监控配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
@Service
public class MonitorConfigServiceImpl implements IMonitorConfigService 
{
    @Autowired
    private MonitorConfigMapper monitorConfigMapper;

    /**
     * 查询监控配置
     * 
     * @param id 监控配置主键
     * @return 监控配置
     */
    @Override
    public MonitorConfig selectMonitorConfigById(Long id)
    {
        return monitorConfigMapper.selectMonitorConfigById(id);
    }

    /**
     * 查询监控配置列表
     * 
     * @param monitorConfig 监控配置
     * @return 监控配置
     */
    @Override
    public List<MonitorConfig> selectMonitorConfigList(MonitorConfig monitorConfig)
    {
        return monitorConfigMapper.selectMonitorConfigList(monitorConfig);
    }

    /**
     * 新增监控配置
     * 
     * @param monitorConfig 监控配置
     * @return 结果
     */
    @Override
    public int insertMonitorConfig(MonitorConfig monitorConfig)
    {
        // 设置创建信息
        monitorConfig.setCreateBy(SecurityUtils.getUsername());
        monitorConfig.setCreateTime(new Date());
        
        // 设置默认值
        if (monitorConfig.getVersion() == null) {
            monitorConfig.setVersion(1);
        }
        if (monitorConfig.getStatus() == null) {
            monitorConfig.setStatus(1); // 默认启用
        }
        
        return monitorConfigMapper.insertMonitorConfig(monitorConfig);
    }

    /**
     * 修改监控配置
     * 
     * @param monitorConfig 监控配置
     * @return 结果
     */
    @Override
    public int updateMonitorConfig(MonitorConfig monitorConfig)
    {
        monitorConfig.setUpdateBy(SecurityUtils.getUsername());
        monitorConfig.setUpdateTime(new Date());
        return monitorConfigMapper.updateMonitorConfig(monitorConfig);
    }

    /**
     * 批量删除监控配置
     * 
     * @param ids 需要删除的监控配置主键
     * @return 结果
     */
    @Override
    public int deleteMonitorConfigByIds(Long[] ids)
    {
        return monitorConfigMapper.deleteMonitorConfigByIds(ids);
    }

    /**
     * 删除监控配置信息
     * 
     * @param id 监控配置主键
     * @return 结果
     */
    @Override
    public int deleteMonitorConfigById(Long id)
    {
        return monitorConfigMapper.deleteMonitorConfigById(id);
    }

    /**
     * 修改配置状态
     * 
     * @param id 配置ID
     * @param status 状态
     * @return 结果
     */
    @Override
    public int changeStatus(Long id, Integer status)
    {
        MonitorConfig config = new MonitorConfig();
        config.setId(id);
        config.setStatus(status);
        config.setUpdateBy(SecurityUtils.getUsername());
        config.setUpdateTime(new Date());
        return monitorConfigMapper.updateMonitorConfig(config);
    }

    /**
     * 根据链类型查询配置列表
     * 
     * @param chainType 链类型
     * @return 监控配置集合
     */
    @Override
    public List<MonitorConfig> selectMonitorConfigByChainType(String chainType)
    {
        return monitorConfigMapper.selectMonitorConfigByChainType(chainType);
    }

    /**
     * 统计配置数量
     * 
     * @param chainType 链类型
     * @param status 状态
     * @return 配置数量
     */
    @Override
    public int countMonitorConfig(String chainType, Integer status)
    {
        MonitorConfig condition = new MonitorConfig();
        condition.setChainType(chainType);
        condition.setStatus(status);
        return monitorConfigMapper.countMonitorConfig(condition);
    }
}

