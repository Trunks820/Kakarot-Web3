package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.crypto.domain.GlobalMonitorConfig;
import com.ruoyi.crypto.mapper.GlobalMonitorConfigMapper;
import com.ruoyi.crypto.service.IGlobalMonitorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 链级全局监控配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-10-21
 */
@Service
public class GlobalMonitorConfigServiceImpl implements IGlobalMonitorConfigService 
{
    @Autowired
    private GlobalMonitorConfigMapper globalMonitorConfigMapper;

    /**
     * 查询链级全局监控配置
     * 
     * @param id 链级全局监控配置主键
     * @return 链级全局监控配置
     */
    @Override
    public GlobalMonitorConfig selectGlobalMonitorConfigById(Long id)
    {
        return globalMonitorConfigMapper.selectGlobalMonitorConfigById(id);
    }

    /**
     * 根据链类型查询配置
     * 
     * @param chainType 链类型
     * @return 链级全局监控配置
     */
    @Override
    public GlobalMonitorConfig selectGlobalMonitorConfigByChainType(String chainType)
    {
        return globalMonitorConfigMapper.selectGlobalMonitorConfigByChainType(chainType);
    }

    /**
     * 查询链级全局监控配置列表
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 链级全局监控配置
     */
    @Override
    public List<GlobalMonitorConfig> selectGlobalMonitorConfigList(GlobalMonitorConfig globalMonitorConfig)
    {
        return globalMonitorConfigMapper.selectGlobalMonitorConfigList(globalMonitorConfig);
    }

    /**
     * 新增链级全局监控配置
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 结果
     */
    @Override
    public int insertGlobalMonitorConfig(GlobalMonitorConfig globalMonitorConfig)
    {
        globalMonitorConfig.setCreateTime(DateUtils.getNowDate());
        return globalMonitorConfigMapper.insertGlobalMonitorConfig(globalMonitorConfig);
    }

    /**
     * 修改链级全局监控配置
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 结果
     */
    @Override
    public int updateGlobalMonitorConfig(GlobalMonitorConfig globalMonitorConfig)
    {
        globalMonitorConfig.setUpdateTime(DateUtils.getNowDate());
        return globalMonitorConfigMapper.updateGlobalMonitorConfig(globalMonitorConfig);
    }

    /**
     * 批量删除链级全局监控配置
     * 
     * @param ids 需要删除的链级全局监控配置主键
     * @return 结果
     */
    @Override
    public int deleteGlobalMonitorConfigByIds(Long[] ids)
    {
        return globalMonitorConfigMapper.deleteGlobalMonitorConfigByIds(ids);
    }

    /**
     * 删除链级全局监控配置信息
     * 
     * @param id 链级全局监控配置主键
     * @return 结果
     */
    @Override
    public int deleteGlobalMonitorConfigById(Long id)
    {
        return globalMonitorConfigMapper.deleteGlobalMonitorConfigById(id);
    }

    /**
     * 保存或更新全局监控配置
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 结果
     */
    @Override
    public int saveOrUpdateGlobalMonitorConfig(GlobalMonitorConfig globalMonitorConfig)
    {
        if (globalMonitorConfig.getId() != null)
        {
            // 更新
            return updateGlobalMonitorConfig(globalMonitorConfig);
        }
        else
        {
            // 检查是否已存在相同链类型的配置
            GlobalMonitorConfig existing = selectGlobalMonitorConfigByChainType(globalMonitorConfig.getChainType());
            if (existing != null)
            {
                // 更新现有配置
                globalMonitorConfig.setId(existing.getId());
                return updateGlobalMonitorConfig(globalMonitorConfig);
            }
            else
            {
                // 新增
                return insertGlobalMonitorConfig(globalMonitorConfig);
            }
        }
    }

    /**
     * 切换配置状态
     * 
     * @param id 配置ID
     * @param status 状态
     * @return 结果
     */
    @Override
    public int changeStatus(Long id, String status)
    {
        GlobalMonitorConfig config = new GlobalMonitorConfig();
        config.setId(id);
        config.setStatus(status);
        return updateGlobalMonitorConfig(config);
    }
}

