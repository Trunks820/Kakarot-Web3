package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.QuickMonitorTemplate;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Token智能监控配置模板Mapper接口
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
public interface QuickMonitorTemplateMapper 
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
    public List<QuickMonitorTemplate> selectQuickMonitorTemplateByChainType(@Param("chainType") String chainType);

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
     * 删除配置模板
     * 
     * @param id 主键
     * @return 结果
     */
    public int deleteQuickMonitorTemplateById(Long id);

    /**
     * 批量删除配置模板
     * 
     * @param ids 主键数组
     * @return 结果
     */
    public int deleteQuickMonitorTemplateByIds(Long[] ids);

    /**
     * 批量保存配置模板
     * 
     * @param templates 配置列表
     * @return 结果
     */
    public int batchInsertQuickMonitorTemplate(@Param("templates") List<QuickMonitorTemplate> templates);

    /**
     * 统计指定市值区间的Token数量
     * 
     * @param minMarketCap 最小市值
     * @param maxMarketCap 最大市值（可为null表示无上限）
     * @return Token数量
     */
    public int countTokensInRange(@Param("minMarketCap") Long minMarketCap, @Param("maxMarketCap") Long maxMarketCap);
}

