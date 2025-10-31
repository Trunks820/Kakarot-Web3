package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.crypto.domain.QuickMonitorTemplate;
import com.ruoyi.crypto.mapper.QuickMonitorTemplateMapper;
import com.ruoyi.crypto.service.IQuickMonitorTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static final Logger log = LoggerFactory.getLogger(QuickMonitorTemplateServiceImpl.class);
    
    /**
     * Redis Key 前缀
     * 格式：quick_monitor:template:{chainType}
     */
    private static final String REDIS_KEY_PREFIX = "quick_monitor:template:";
    
    @Resource
    private QuickMonitorTemplateMapper quickMonitorTemplateMapper;
    
    @Autowired
    private RedisCache redisCache;

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
     * 根据链类型查询配置列表（优先从 Redis 读取）
     * 
     * @param chainType 链类型
     * @return 配置列表
     */
    @Override
    public List<QuickMonitorTemplate> selectQuickMonitorTemplateByChainType(String chainType)
    {
        // 生成 Redis Key
        String redisKey = REDIS_KEY_PREFIX + chainType.toLowerCase();
        
        // 1. 先从 Redis 读取
        List<QuickMonitorTemplate> cachedList = redisCache.getCacheObject(redisKey);
        if (cachedList != null) {
            log.debug("✅ 从 Redis 缓存读取智能监控配置: chainType={}, 配置数量={}", chainType, cachedList.size());
            return cachedList;
        }
        
        // 2. Redis 没有，从数据库读取
        log.debug("⚠️ Redis 缓存未命中，从数据库读取智能监控配置: chainType={}", chainType);
        List<QuickMonitorTemplate> templates = quickMonitorTemplateMapper.selectQuickMonitorTemplateByChainType(chainType);
        
        // 3. 写入 Redis（永不过期）
        if (templates != null && !templates.isEmpty()) {
            redisCache.setCacheObject(redisKey, templates);
            log.info("📝 智能监控配置已缓存到 Redis（永不过期）: chainType={}, 配置数量={}", chainType, templates.size());
        }
        
        return templates;
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
        // 默认设置为未删除
        if (quickMonitorTemplate.getDelFlag() == null) {
            quickMonitorTemplate.setDelFlag("0");
        }
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
     * 批量保存配置模板（先逻辑删除该链的所有配置，再批量插入新配置，同时刷新 Redis 缓存）
     * 
     * @param chainType 链类型
     * @param templates 配置列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchSaveQuickMonitorTemplate(String chainType, List<QuickMonitorTemplate> templates)
    {
        // 1. 逻辑删除该链的所有现有配置（del_flag设置为'2'）
        QuickMonitorTemplate query = new QuickMonitorTemplate();
        query.setChainType(chainType);
        List<QuickMonitorTemplate> existList = quickMonitorTemplateMapper.selectQuickMonitorTemplateList(query);
        
        if (!existList.isEmpty()) {
            Long[] ids = existList.stream().map(QuickMonitorTemplate::getId).toArray(Long[]::new);
            // 逻辑删除（实际上是update操作）
            quickMonitorTemplateMapper.deleteQuickMonitorTemplateByIds(ids);
        }
        
        // 2. 批量插入新配置（del_flag默认为'0'）
        int result = 0;
        if (templates != null && !templates.isEmpty()) {
            // 确保所有新配置的del_flag为'0'
            templates.forEach(template -> {
                if (template.getDelFlag() == null) {
                    template.setDelFlag("0");
                }
            });
            result = quickMonitorTemplateMapper.batchInsertQuickMonitorTemplate(templates);
        }
        
        // 3. 刷新 Redis 缓存
        refreshRedisCache(chainType);
        
        return result;
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
            item.put("hasTwitter", template.getHasTwitter());
            item.put("timeInterval", template.getTimeInterval());
            item.put("topHoldersThreshold", template.getTopHoldersThreshold());
            item.put("configName", template.getConfigName());
            item.put("eventsConfig", template.getEventsConfig());
            item.put("notifyMethods", template.getNotifyMethods());
            item.put("tokenCount", tokenCount);
            result.add(item);
        }
        
        return result;
    }

    /**
     * 预测配置的Token匹配数量（用于编辑时实时预测）
     * 
     * @param marketCapList 市值门槛列表
     * @return 预测结果列表
     */
    @Override
    public List<java.util.Map<String, Object>> predictTokenCounts(List<Long> marketCapList)
    {
        if (marketCapList == null || marketCapList.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 按市值从小到大排序
        List<Long> sortedList = new java.util.ArrayList<>(marketCapList);
        sortedList.sort(Long::compareTo);
        
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        
        for (int i = 0; i < sortedList.size(); i++) {
            Long minMarketCap = sortedList.get(i);
            Long maxMarketCap = null;
            
            // 如果不是最后一个（最高档），设置上限为下一个更高的市值
            if (i < sortedList.size() - 1) {
                maxMarketCap = sortedList.get(i + 1);
            }
            
            // 统计该区间的Token数量
            int tokenCount = quickMonitorTemplateMapper.countTokensInRange(minMarketCap, maxMarketCap);
            
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("minMarketCap", minMarketCap);
            item.put("maxMarketCap", maxMarketCap);
            item.put("tokenCount", tokenCount);
            result.add(item);
        }
        
        return result;
    }

    /**
     * 刷新 Redis 缓存
     * 
     * @param chainType 链类型
     */
    private void refreshRedisCache(String chainType)
    {
        if (chainType == null || chainType.isEmpty()) {
            log.warn("⚠️ 刷新 Redis 缓存失败：chainType 为空");
            return;
        }
        
        // 生成 Redis Key
        String redisKey = REDIS_KEY_PREFIX + chainType.toLowerCase();
        
        // 从数据库重新读取最新配置
        List<QuickMonitorTemplate> templates = quickMonitorTemplateMapper.selectQuickMonitorTemplateByChainType(chainType);
        
        // 更新 Redis 缓存（永不过期）
        if (templates != null && !templates.isEmpty()) {
            redisCache.setCacheObject(redisKey, templates);
            log.info("🔄 Redis 缓存已刷新: chainType={}, 配置数量={}, Redis Key={}", 
                    chainType, templates.size(), redisKey);
        } else {
            // 如果没有配置，删除缓存
            redisCache.deleteObject(redisKey);
            log.info("🗑️ Redis 缓存已清除（无配置）: chainType={}, Redis Key={}", chainType, redisKey);
        }
    }

    /**
     * 清除 Redis 缓存
     * 
     * @param chainType 链类型
     */
    private void clearRedisCache(String chainType)
    {
        if (chainType == null || chainType.isEmpty()) {
            log.warn("⚠️ 清除 Redis 缓存失败：chainType 为空");
            return;
        }
        
        // 生成 Redis Key
        String redisKey = REDIS_KEY_PREFIX + chainType.toLowerCase();
        
        // 删除缓存
        redisCache.deleteObject(redisKey);
        
        log.info("🗑️ Redis 缓存已清除: chainType={}, Redis Key={}", chainType, redisKey);
    }

}

