package com.ruoyi.crypto.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 一致性哈希工具类（固定槽数）
 * 
 * 核心特性：
 * 1. 固定256个槽位（不随Consumer数量变化）
 * 2. 使用虚拟节点提高均衡性
 * 3. Consumer变更时只影响部分槽位，不影响整体分布
 * 4. 线程安全，支持动态更新Consumer列表
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@Component
public class ConsistentHashUtil {
    
    /**
     * 固定槽数（从配置文件读取，默认256）
     */
    @Value("${monitor.batch.fixed-bucket-count:256}")
    private int fixedBucketCount;
    
    /**
     * 虚拟节点数（从配置文件读取，默认150）
     */
    @Value("${monitor.batch.virtual-nodes:150}")
    private int virtualNodes;
    
    /**
     * 哈希环：key=哈希值，value=Consumer ID
     * TreeMap保证有序，用于快速查找顺时针最近节点
     */
    private TreeMap<Long, String> hashRing;
    
    /**
     * 当前Consumer列表（用于检测变更）
     */
    private List<String> currentConsumers;
    
    /**
     * MD5摘要算法（用于计算哈希值）
     */
    private MessageDigest md5Digest;
    
    /**
     * Bean初始化后执行
     */
    @PostConstruct
    public void init() {
        try {
            this.md5Digest = MessageDigest.getInstance("MD5");
            this.hashRing = new TreeMap<>();
            this.currentConsumers = new ArrayList<>();
        } catch (Exception e) {
            throw new RuntimeException("初始化ConsistentHashUtil失败", e);
        }
    }
    
    /**
     * 更新Consumer列表并重建哈希环
     * 
     * @param consumers Consumer ID列表
     */
    public synchronized void updateConsumers(List<String> consumers) {
        if (consumers == null || consumers.isEmpty()) {
            // 如果没有Consumer，清空哈希环
            this.hashRing.clear();
            this.currentConsumers.clear();
            return;
        }
        
        // 检查是否有变化
        if (this.currentConsumers.equals(consumers)) {
            return; // 没有变化，无需重建
        }
        
        // 重建哈希环
        this.hashRing.clear();
        for (String consumer : consumers) {
            // 为每个Consumer创建虚拟节点
            for (int i = 0; i < virtualNodes; i++) {
                String virtualKey = consumer + "#VN" + i;
                long hash = hash(virtualKey);
                hashRing.put(hash, consumer);
            }
        }
        
        // 更新当前Consumer列表
        this.currentConsumers = new ArrayList<>(consumers);
    }
    
    /**
     * 计算CA应该分配到哪个批次（固定槽数）
     * 
     * @param ca 合约地址
     * @return 槽位编号（0 ~ fixedBucketCount-1）
     */
    public int getBucketForCA(String ca) {
        long hash = hash(ca);
        // 取模运算，映射到固定槽位
        return (int) (Math.abs(hash) % fixedBucketCount);
    }
    
    /**
     * 获取槽位对应的Consumer ID
     * 
     * @param bucketId 槽位编号
     * @return Consumer ID，如果没有Consumer则返回null
     */
    public String getConsumerForBucket(int bucketId) {
        if (hashRing.isEmpty()) {
            return null;
        }
        
        // 将槽位编号映射到哈希环
        long bucketHash = hash("BUCKET#" + bucketId);
        
        // 顺时针查找最近的Consumer
        Map.Entry<Long, String> entry = hashRing.ceilingEntry(bucketHash);
        if (entry == null) {
            // 如果没找到，返回第一个（环形）
            entry = hashRing.firstEntry();
        }
        
        return entry.getValue();
    }
    
    /**
     * 获取CA对应的Consumer ID
     * 
     * @param ca 合约地址
     * @return Consumer ID，如果没有Consumer则返回null
     */
    public String getConsumerForCA(String ca) {
        int bucketId = getBucketForCA(ca);
        return getConsumerForBucket(bucketId);
    }
    
    /**
     * 批量获取CA对应的Consumer ID
     * 
     * @param caList CA列表
     * @return Map<ConsumerID, List<CA>>
     */
    public Map<String, List<String>> groupCAsByConsumer(List<String> caList) {
        Map<String, List<String>> result = new HashMap<>();
        
        for (String ca : caList) {
            String consumer = getConsumerForCA(ca);
            if (consumer != null) {
                result.computeIfAbsent(consumer, k -> new ArrayList<>()).add(ca);
            }
        }
        
        return result;
    }
    
    /**
     * 获取所有槽位的分配情况
     * 
     * @return Map<ConsumerID, List<BucketID>>
     */
    public Map<String, List<Integer>> getAllBucketAssignments() {
        Map<String, List<Integer>> result = new HashMap<>();
        
        for (int bucketId = 0; bucketId < fixedBucketCount; bucketId++) {
            String consumer = getConsumerForBucket(bucketId);
            if (consumer != null) {
                result.computeIfAbsent(consumer, k -> new ArrayList<>()).add(bucketId);
            }
        }
        
        return result;
    }
    
    /**
     * 计算哈希值（MD5前8字节转long）
     * 
     * @param key 键值
     * @return 哈希值
     */
    private long hash(String key) {
        try {
            byte[] digest = md5Digest.digest(key.getBytes(StandardCharsets.UTF_8));
            // 取前8字节转long
            long hash = 0;
            for (int i = 0; i < 8; i++) {
                hash = (hash << 8) | (digest[i] & 0xFF);
            }
            return hash;
        } catch (Exception e) {
            throw new RuntimeException("计算哈希值失败: " + key, e);
        }
    }
    
    /**
     * 获取当前Consumer列表
     * 
     * @return Consumer列表
     */
    public List<String> getCurrentConsumers() {
        return new ArrayList<>(currentConsumers);
    }
    
    /**
     * 获取固定槽数
     * 
     * @return 槽位数量
     */
    public int getFixedBucketCount() {
        return fixedBucketCount;
    }
    
    /**
     * 获取虚拟节点数
     * 
     * @return 虚拟节点数
     */
    public int getVirtualNodes() {
        return virtualNodes;
    }
}

