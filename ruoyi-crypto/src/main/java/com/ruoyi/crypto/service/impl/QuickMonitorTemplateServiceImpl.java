package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.crypto.domain.QuickMonitorTemplate;
import com.ruoyi.crypto.mapper.QuickMonitorTemplateMapper;
import com.ruoyi.crypto.service.IQuickMonitorTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Token智能监控配置模板Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
@Service
public class QuickMonitorTemplateServiceImpl implements IQuickMonitorTemplateService 
{
    @Resource
    private QuickMonitorTemplateMapper quickMonitorTemplateMapper;

    /**
     * 查询配置模板
     * 
     * @param id 主键
     * @return 配置模板
     */
    @Override
    public QuickMonitorTemplate selectQuickMonitorTemplateById(Long id)
    {
        return quickMonitorTemplateMapper.selectQuickMonitorTemplateById(id);
    }

    /**
     * 根据链类型查询配置列表
     * 
     * @param chainType 链类型
     * @return 配置列表
     */
    @Override
    public List<QuickMonitorTemplate> selectQuickMonitorTemplateByChainType(String chainType)
    {
        return quickMonitorTemplateMapper.selectQuickMonitorTemplateByChainType(chainType);
    }

    /**
     * 查询配置模板列表
     * 
     * @param quickMonitorTemplate 配置模板
     * @return 配置列表
     */
    @Override
    public List<QuickMonitorTemplate> selectQuickMonitorTemplateList(QuickMonitorTemplate quickMonitorTemplate)
    {
        return quickMonitorTemplateMapper.selectQuickMonitorTemplateList(quickMonitorTemplate);
    }

    /**
     * 新增配置模板
     * 
     * @param quickMonitorTemplate 配置模板
     * @return 结果
     */
    @Override
    public int insertQuickMonitorTemplate(QuickMonitorTemplate quickMonitorTemplate)
    {
        quickMonitorTemplate.setCreateTime(DateUtils.getNowDate());
        return quickMonitorTemplateMapper.insertQuickMonitorTemplate(quickMonitorTemplate);
    }

    /**
     * 修改配置模板
     * 
     * @param quickMonitorTemplate 配置模板
     * @return 结果
     */
    @Override
    public int updateQuickMonitorTemplate(QuickMonitorTemplate quickMonitorTemplate)
    {
        quickMonitorTemplate.setUpdateTime(DateUtils.getNowDate());
        return quickMonitorTemplateMapper.updateQuickMonitorTemplate(quickMonitorTemplate);
    }

    /**
     * 批量删除配置模板
     * 
     * @param ids 主键数组
     * @return 结果
     */
    @Override
    public int deleteQuickMonitorTemplateByIds(Long[] ids)
    {
        return quickMonitorTemplateMapper.deleteQuickMonitorTemplateByIds(ids);
    }

    /**
     * 删除配置模板
     * 
     * @param id 主键
     * @return 结果
     */
    @Override
    public int deleteQuickMonitorTemplateById(Long id)
    {
        return quickMonitorTemplateMapper.deleteQuickMonitorTemplateById(id);
    }

    /**
     * 批量保存配置模板（先删除该链的所有配置，再批量插入）
     * 
     * @param chainType 链类型
     * @param templates 配置列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveQuickMonitorTemplate(String chainType, List<QuickMonitorTemplate> templates)
    {
        // 1. 删除该链的所有配置
        QuickMonitorTemplate query = new QuickMonitorTemplate();
        query.setChainType(chainType);
        List<QuickMonitorTemplate> existList = quickMonitorTemplateMapper.selectQuickMonitorTemplateList(query);
        
        if (!existList.isEmpty()) {
            Long[] ids = existList.stream().map(QuickMonitorTemplate::getId).toArray(Long[]::new);
            quickMonitorTemplateMapper.deleteQuickMonitorTemplateByIds(ids);
        }
        
        // 2. 批量插入新配置
        if (templates != null && !templates.isEmpty()) {
            return quickMonitorTemplateMapper.batchInsertQuickMonitorTemplate(templates);
        }
        
        return 0;
    }

    /**
     * 获取配置统计（包含每个配置段匹配的Token数量）
     * 
     * @param chainType 链类型
     * @return 配置统计列表
     */
    @Override
    public List<java.util.Map<String, Object>> getConfigStatsWithTokenCount(String chainType)
    {
        // 1. 获取所有配置，按市值从高到低排序
        List<QuickMonitorTemplate> templates = quickMonitorTemplateMapper.selectQuickMonitorTemplateByChainType(chainType);
        if (templates == null || templates.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 按 minMarketCap 升序排序（从小到大）
        templates.sort((a, b) -> a.getMinMarketCap().compareTo(b.getMinMarketCap()));
        
        // 2. 循环统计每个区间的Token数量
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 0; i < templates.size(); i++) {
            QuickMonitorTemplate template = templates.get(i);
            Long minMarketCap = template.getMinMarketCap().longValue();
            Long maxMarketCap = null;
            
            // 如果不是最后一个（最高档），设置上限为下一个更高的市值
            if (i < templates.size() - 1) {
                maxMarketCap = templates.get(i + 1).getMinMarketCap().longValue();
            }
            
            // 统计该区间的Token数量
            int tokenCount = quickMonitorTemplateMapper.countTokensInRange(minMarketCap, maxMarketCap);
            
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", template.getId());
            item.put("minMarketCap", template.getMinMarketCap());
            item.put("configName", template.getConfigName());
            item.put("eventsConfig", template.getEventsConfig());
            item.put("notifyMethods", template.getNotifyMethods());
            item.put("tokenCount", tokenCount);
            result.add(item);
        }
        
        return result;
    }
}

