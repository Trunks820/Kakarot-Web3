package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.crypto.domain.GlobalMonitorConfig;
import com.ruoyi.crypto.mapper.GlobalMonitorConfigMapper;
import com.ruoyi.crypto.service.IGlobalMonitorConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(GlobalMonitorConfigServiceImpl.class);
    
    /**
     * Redis Key 前缀
     * 格式: global_monitor:config:{chain_type}
     */
    private static final String REDIS_KEY_PREFIX = "global_monitor:config:";
    
    @Autowired
    private GlobalMonitorConfigMapper globalMonitorConfigMapper;
    
    @Autowired
    private RedisCache redisCache;

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
     * 根据链类型查询配置（优先从 Redis 读取）
     *
     * @param chainType 链类型
     * @return 链级全局监控配置
     */
    @Override
    public GlobalMonitorConfig selectGlobalMonitorConfigByChainType(String chainType)
    {
        String redisKey = REDIS_KEY_PREFIX + chainType.toLowerCase();
        
        // 1. 先从 Redis 读取
        GlobalMonitorConfig config = redisCache.getCacheObject(redisKey);
        
        if (config != null) {
            log.debug("✅ 从 Redis 缓存读取链级配置: chainType={}", chainType);
            return config;
        }
        
        // 2. Redis 没有，从数据库读取
        log.debug("⚠️ Redis 缓存未命中，从数据库读取链级配置: chainType={}", chainType);
        config = globalMonitorConfigMapper.selectGlobalMonitorConfigByChainType(chainType);
        
        // 3. 写入 Redis（永不过期）
        if (config != null) {
            redisCache.setCacheObject(redisKey, config);
            log.info("📝 链级配置已缓存到 Redis（永不过期）: chainType={}", chainType);
        }
        
        return config;
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
     * 新增链级全局监控配置（同时刷新 Redis 缓存）
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 结果
     */
    @Override
    public int insertGlobalMonitorConfig(GlobalMonitorConfig globalMonitorConfig)
    {
        globalMonitorConfig.setCreateTime(DateUtils.getNowDate());
        int result = globalMonitorConfigMapper.insertGlobalMonitorConfig(globalMonitorConfig);
        
        // 刷新 Redis 缓存
        if (result > 0) {
            refreshRedisCache(globalMonitorConfig);
        }
        
        return result;
    }

    /**
     * 修改链级全局监控配置（同时刷新 Redis 缓存）
     * 
     * @param globalMonitorConfig 链级全局监控配置
     * @return 结果
     */
    @Override
    public int updateGlobalMonitorConfig(GlobalMonitorConfig globalMonitorConfig)
    {
        globalMonitorConfig.setUpdateTime(DateUtils.getNowDate());
        int result = globalMonitorConfigMapper.updateGlobalMonitorConfig(globalMonitorConfig);
        
        // 刷新 Redis 缓存
        if (result > 0) {
            refreshRedisCache(globalMonitorConfig);
        }
        
        return result;
    }

    /**
     * 批量删除链级全局监控配置（同时清除 Redis 缓存）
     * 
     * @param ids 需要删除的链级全局监控配置主键
     * @return 结果
     */
    @Override
    public int deleteGlobalMonitorConfigByIds(Long[] ids)
    {
        // 先查询出要删除的配置，获取 chainType
        for (Long id : ids) {
            GlobalMonitorConfig config = globalMonitorConfigMapper.selectGlobalMonitorConfigById(id);
            if (config != null) {
                clearRedisCache(config.getChainType());
            }
        }
        
        return globalMonitorConfigMapper.deleteGlobalMonitorConfigByIds(ids);
    }

    /**
     * 删除链级全局监控配置信息（同时清除 Redis 缓存）
     * 
     * @param id 链级全局监控配置主键
     * @return 结果
     */
    @Override
    public int deleteGlobalMonitorConfigById(Long id)
    {
        // 先查询出要删除的配置，获取 chainType
        GlobalMonitorConfig config = globalMonitorConfigMapper.selectGlobalMonitorConfigById(id);
        if (config != null) {
            clearRedisCache(config.getChainType());
        }
        
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
     * 切换配置状态（同时刷新 Redis 缓存）
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
        int result = updateGlobalMonitorConfig(config);
        
        // 刷新 Redis 缓存（updateGlobalMonitorConfig 内部已经会刷新，这里无需重复）
        
        return result;
    }
    
    /**
     * 刷新 Redis 缓存
     * 
     * @param config 链级全局监控配置
     */
    private void refreshRedisCache(GlobalMonitorConfig config)
    {
        if (config == null || config.getChainType() == null) {
            return;
        }
        
        String redisKey = REDIS_KEY_PREFIX + config.getChainType().toLowerCase();
        
        // 先从数据库查询完整的配置信息（确保数据完整性）
        GlobalMonitorConfig fullConfig = globalMonitorConfigMapper.selectGlobalMonitorConfigById(config.getId());
        
        if (fullConfig != null) {
            // 写入 Redis（永不过期）
            redisCache.setCacheObject(redisKey, fullConfig);
            
            log.info("🔄 Redis 缓存已刷新: chainType={}, configId={}, status={}, configName={}", 
                     fullConfig.getChainType(), fullConfig.getId(), fullConfig.getStatus(), fullConfig.getConfigName());
        }
    }
    
    /**
     * 清除 Redis 缓存
     * 
     * @param chainType 链类型
     */
    private void clearRedisCache(String chainType)
    {
        if (chainType == null) {
            return;
        }
        
        String redisKey = REDIS_KEY_PREFIX + chainType.toLowerCase();
        redisCache.deleteObject(redisKey);
        
        log.info("🗑️ Redis 缓存已清除: chainType={}", chainType);
    }
}

