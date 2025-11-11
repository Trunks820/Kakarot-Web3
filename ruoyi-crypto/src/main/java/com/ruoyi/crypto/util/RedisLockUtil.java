package com.ruoyi.crypto.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 * 
 * 功能：
 * 1. 基于Redis的分布式锁
 * 2. 支持动态超时（根据目标数量计算）
 * 3. 安全释放（Lua脚本校验requestId）
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@Component
public class RedisLockUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(RedisLockUtil.class);
    
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    
    /**
     * 尝试获取锁（固定超时）
     * 
     * @param lockKey 锁的key
     * @param requestId 请求ID（用于释放时校验）
     * @param expireMs 过期时间（毫秒）
     * @return true=获取成功，false=获取失败
     */
    public boolean tryLock(String lockKey, String requestId, long expireMs) {
        try {
            Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, requestId, expireMs, TimeUnit.MILLISECONDS);
            
            if (Boolean.TRUE.equals(result)) {
                logger.info("获取锁成功: key={}, requestId={}, expire={}ms", 
                            lockKey, requestId, expireMs);
                return true;
            } else {
                logger.warn("获取锁失败: key={}, requestId={}", lockKey, requestId);
                return false;
            }
        } catch (Exception e) {
            logger.error("获取锁异常: key={}, requestId={}", lockKey, requestId, e);
            return false;
        }
    }
    
    /**
     * 尝试获取锁（动态超时）⭐ 新增方法
     * 
     * 动态计算超时时间 = 基础5分钟 + 目标数×单个目标处理时间
     * 适用场景：目标数量不固定，需要根据实际情况动态调整锁超时时间
     * 
     * @param lockKey 锁的key
     * @param requestId 请求ID
     * @param targetCount 目标数量
     * @return true=获取成功，false=获取失败
     */
    public boolean tryLockWithDynamicTimeout(String lockKey, String requestId, int targetCount) {
        // 动态计算：基础5分钟(300秒) + 每个目标20ms
        // 示例：6000个目标 = 300秒 + 120秒 = 7分钟
        int perItemMs = 20;  // 每个目标预估处理时间（含插入+分配）
        long timeoutMs = 300000L + (long) targetCount * perItemMs;  // ⭐ 修复：改为相加，而非取最大值
        
        logger.info("尝试获取锁（动态超时）: key={}, targetCount={}, timeout={}s", 
                    lockKey, targetCount, timeoutMs / 1000);
        
        return tryLock(lockKey, requestId, timeoutMs);
    }
    
    /**
     * 释放锁（安全释放，Lua脚本校验requestId）
     * 
     * @param lockKey 锁的key
     * @param requestId 请求ID（必须匹配才能释放）
     * @return true=释放成功，false=释放失败
     */
    public boolean releaseLock(String lockKey, String requestId) {
        try {
            // Lua脚本：只有当值匹配时才删除
            String luaScript = 
                "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "   return redis.call('del', KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";
            
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(luaScript);
            redisScript.setResultType(Long.class);
            
            Long result = redisTemplate.execute(
                redisScript,
                Collections.singletonList(lockKey),
                requestId
            );
            
            if (result != null && result > 0) {
                logger.info("释放锁成功: key={}, requestId={}", lockKey, requestId);
                return true;
            } else {
                logger.warn("释放锁失败（key不存在或值不匹配）: key={}, requestId={}", lockKey, requestId);
                return false;
            }
        } catch (Exception e) {
            logger.error("释放锁异常: key={}, requestId={}", lockKey, requestId, e);
            return false;
        }
    }
    
    /**
     * 检查锁是否存在
     * 
     * @param lockKey 锁的key
     * @return true=锁存在，false=锁不存在
     */
    public boolean isLocked(String lockKey) {
        try {
            Boolean hasKey = redisTemplate.hasKey(lockKey);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            logger.error("检查锁异常: key={}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 获取锁的剩余过期时间
     * 
     * @param lockKey 锁的key
     * @return 剩余时间（毫秒），-1表示key不存在，-2表示key永久有效
     */
    public long getLockTTL(String lockKey) {
        try {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MILLISECONDS);
            return ttl != null ? ttl : -1;
        } catch (Exception e) {
            logger.error("获取锁TTL异常: key={}", lockKey, e);
            return -1;
        }
    }
}

