package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.GlobalMonitorConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 链级全局监控配置Mapper接口
 * 
 * @author ruoyi
 * @date 2025-10-21
 */
public interface GlobalMonitorConfigMapper 
{
    /**
     * 查询链级全局监控配置
     * 
     * @param id 链级全局监控配置主键
     * @return 链级全局监控配置
     */
    public GlobalMonitorConfig selectGlobalMonitorConfigById(Long id);

    /**
     * 根据链类型查询配置
     * 
     * @param chainType 链类型
     * @return 链级全局监控配置
     */
    public GlobalMonitorConfig selectGlobalMonitorConfigByChainType(String chainType);

    /**
     * 根据链类型和市场类型查询配置
     * 
     * @param chainType 链类型
     * @param marketType 市场类型
     * @return 链级全局监控配置
     */
    public GlobalMonitorConfig selectGlobalMonitorConfigByChainTypeAndMarket(@Param("chainType") String chainType, @Param("marketType") String marketType);

    /**
     * 查询链级全局监控配置列表
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 链级全局监控配置集合
     */
    public List<GlobalMonitorConfig> selectGlobalMonitorConfigList(GlobalMonitorConfig globalMonitorConfig);

    /**
     * 新增链级全局监控配置
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 结果
     */
    public int insertGlobalMonitorConfig(GlobalMonitorConfig globalMonitorConfig);

    /**
     * 修改链级全局监控配置
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 结果
     */
    public int updateGlobalMonitorConfig(GlobalMonitorConfig globalMonitorConfig);

    /**
     * 删除链级全局监控配置
     * 
     * @param id 链级全局监控配置主键
     * @return 结果
     */
    public int deleteGlobalMonitorConfigById(Long id);

    /**
     * 批量删除链级全局监控配置
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteGlobalMonitorConfigByIds(Long[] ids);
}

