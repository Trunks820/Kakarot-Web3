package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.QuickMonitorTemplate;
import java.util.List;

/**
 * Token智能监控配置模板Service接口
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
public interface IQuickMonitorTemplateService 
{
    /**
     * 查询配置模板
     * 
     * @param id 主键
     * @return 配置模板
     */
    public QuickMonitorTemplate selectQuickMonitorTemplateById(Long id);

    /**
     * 根据链类型查询配置列表
     * 
     * @param chainType 链类型
     * @return 配置列表
     */
    public List<QuickMonitorTemplate> selectQuickMonitorTemplateByChainType(String chainType);

    /**
     * 查询配置模板列表
     * 
     * @param quickMonitorTemplate 配置模板
     * @return 配置列表
     */
    public List<QuickMonitorTemplate> selectQuickMonitorTemplateList(QuickMonitorTemplate quickMonitorTemplate);

    /**
     * 新增配置模板
     * 
     * @param quickMonitorTemplate 配置模板
     * @return 结果
     */
    public int insertQuickMonitorTemplate(QuickMonitorTemplate quickMonitorTemplate);

    /**
     * 修改配置模板
     * 
     * @param quickMonitorTemplate 配置模板
     * @return 结果
     */
    public int updateQuickMonitorTemplate(QuickMonitorTemplate quickMonitorTemplate);

    /**
     * 批量删除配置模板
     * 
     * @param ids 主键数组
     * @return 结果
     */
    public int deleteQuickMonitorTemplateByIds(Long[] ids);

    /**
     * 删除配置模板
     * 
     * @param id 主键
     * @return 结果
     */
    public int deleteQuickMonitorTemplateById(Long id);

    /**
     * 批量保存配置模板（先删除后插入）
     * 
     * @param chainType 链类型
     * @param templates 配置列表
     * @return 结果
     */
    public int batchSaveQuickMonitorTemplate(String chainType, List<QuickMonitorTemplate> templates);

    /**
     * 获取配置统计（包含每个配置段匹配的Token数量）
     * 
     * @param chainType 链类型
     * @return 配置统计列表
     */
    public List<java.util.Map<String, Object>> getConfigStatsWithTokenCount(String chainType);
}

