package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.util.RedisLockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Redis锁测试接口（临时测试用，上线后可删除）
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@RestController
@RequestMapping("/crypto/test/redis-lock")
public class TestRedisLockController extends BaseController {
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    /**
     * 测试基本锁功能
     */
    @GetMapping("/basic")
    public AjaxResult testBasicLock() {
        String lockKey = "test:lock:basic";
        String requestId = UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁（5秒超时）
            boolean locked = redisLockUtil.tryLock(lockKey, requestId, 5000);
            
            if (locked) {
                // 模拟业务处理
                Thread.sleep(1000);
                
                // 释放锁
                boolean released = redisLockUtil.releaseLock(lockKey, requestId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("locked", true);
                result.put("released", released);
                result.put("message", "基本锁功能测试成功");
                
                return AjaxResult.success(result);
            } else {
                return AjaxResult.error("获取锁失败");
            }
        } catch (Exception e) {
            logger.error("测试基本锁功能失败", e);
            return AjaxResult.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试动态超时功能 ⭐
     */
    @GetMapping("/dynamic-timeout")
    public AjaxResult testDynamicTimeout(@RequestParam(defaultValue = "6000") int targetCount) {
        String lockKey = "test:lock:dynamic";
        String requestId = UUID.randomUUID().toString();
        
        try {
            // 使用动态超时
            boolean locked = redisLockUtil.tryLockWithDynamicTimeout(lockKey, requestId, targetCount);
            
            if (locked) {
                // 获取锁的TTL
                long ttl = redisLockUtil.getLockTTL(lockKey);
                
                // 释放锁
                redisLockUtil.releaseLock(lockKey, requestId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("targetCount", targetCount);
                result.put("lockTimeout", ttl / 1000 + "秒");
                result.put("calculation", "基础300秒 + " + targetCount + " × 0.02秒 = " + (ttl / 1000) + "秒");
                result.put("message", "动态超时测试成功");
                
                return AjaxResult.success(result);
            } else {
                return AjaxResult.error("获取锁失败");
            }
        } catch (Exception e) {
            logger.error("测试动态超时失败", e);
            return AjaxResult.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试并发锁（模拟竞争）
     */
    @GetMapping("/concurrent")
    public AjaxResult testConcurrentLock() {
        String lockKey = "test:lock:concurrent";
        String requestId = UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁
            boolean locked = redisLockUtil.tryLock(lockKey, requestId, 10000);
            
            Map<String, Object> result = new HashMap<>();
            result.put("locked", locked);
            result.put("requestId", requestId);
            
            if (locked) {
                result.put("message", "✅ 获取锁成功！其他请求会被阻塞");
                result.put("tip", "5秒后锁自动释放，或调用 /release?requestId=" + requestId);
                
                // 5秒后自动释放
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        redisLockUtil.releaseLock(lockKey, requestId);
                        logger.info("并发锁测试：5秒后自动释放锁 requestId={}", requestId);
                    } catch (Exception e) {
                        logger.error("自动释放锁失败", e);
                    }
                }).start();
                
                return AjaxResult.success(result);
            } else {
                result.put("message", "❌ 获取锁失败，锁已被占用");
                result.put("tip", "请等待其他请求释放锁");
                return AjaxResult.warn("锁已被占用", result);
            }
        } catch (Exception e) {
            logger.error("测试并发锁失败", e);
            return AjaxResult.error("测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动释放锁
     */
    @GetMapping("/release")
    public AjaxResult releaseLock(@RequestParam String requestId) {
        String lockKey = "test:lock:concurrent";
        
        try {
            boolean released = redisLockUtil.releaseLock(lockKey, requestId);
            
            if (released) {
                return AjaxResult.success("✅ 锁释放成功");
            } else {
                return AjaxResult.error("❌ 释放失败（锁不存在或requestId不匹配）");
            }
        } catch (Exception e) {
            logger.error("释放锁失败", e);
            return AjaxResult.error("释放失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查锁状态
     */
    @GetMapping("/status")
    public AjaxResult checkLockStatus(@RequestParam(defaultValue = "test:lock:concurrent") String lockKey) {
        try {
            boolean isLocked = redisLockUtil.isLocked(lockKey);
            long ttl = redisLockUtil.getLockTTL(lockKey);
            
            Map<String, Object> result = new HashMap<>();
            result.put("lockKey", lockKey);
            result.put("isLocked", isLocked);
            result.put("ttl", ttl > 0 ? ttl / 1000 + "秒" : "已过期");
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("检查锁状态失败", e);
            return AjaxResult.error("检查失败: " + e.getMessage());
        }
    }
}

