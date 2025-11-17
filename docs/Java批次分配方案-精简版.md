# Java批次分配与目标同步实现方案（精简版）

> **版本**: v2.1 精简版  
> **日期**: 2025-11-13  
> **状态**: 生产级方案（适配中小规模）
> **最新更新**: 修复5个关键问题（零停机、Twitter筛选、配置化、性能优化）

---

## 📊 实际数据规模

- **token_launch_history 总数**：150,000条
- **符合条件（市值>300K）**：~6,153条
- **预估批次数**：62个（6153 ÷ 99）
- **预估单次分配耗时**：**< 5秒**

**结论**：✅ 中小规模场景，简单方案完全够用！

---

## 🚨 必须修改的3处（1小时搞定）

### 1️⃣ 数据库去重 + 唯一索引（15分钟）

```sql
-- 1. 备份数据
CREATE TABLE monitor_task_target_v2_backup AS 
SELECT * FROM monitor_task_target_v2;

-- 2. 删除重复数据（保留id最小的）
DELETE t1 FROM monitor_task_target_v2 t1
JOIN monitor_task_target_v2 t2
  ON t1.task_id = t2.task_id 
  AND t1.ca = t2.ca 
  AND t1.id > t2.id;

-- 3. 添加唯一索引
ALTER TABLE monitor_task_target_v2
ADD UNIQUE INDEX ux_target_task_ca (task_id, ca);

-- 4. 验证
SHOW INDEX FROM monitor_task_target_v2 WHERE Key_name = 'ux_target_task_ca';
```

**⚠️ 重要**：还需要修改 `monitor_batch_v2` 的唯一索引以支持零停机批次切换：

```sql
-- 执行 sql/fix_batch_unique_index.sql 中的DDL
-- 或手动执行以下SQL：

-- 1. 删除外键约束（必须先删除，才能删除索引）
ALTER TABLE monitor_batch_v2 DROP FOREIGN KEY monitor_batch_v2_ibfk_1;

-- 2. 删除旧唯一索引
ALTER TABLE monitor_batch_v2 DROP INDEX uk_task_batch;

-- 3. 创建新唯一索引（包含 epoch）
ALTER TABLE monitor_batch_v2 
ADD UNIQUE KEY uk_task_batch_epoch (task_id, epoch, batch_no);

-- 4. 重新创建外键约束
ALTER TABLE monitor_batch_v2 
ADD CONSTRAINT monitor_batch_v2_ibfk_1 
FOREIGN KEY (task_id) REFERENCES monitor_task_v2(id) ON DELETE CASCADE;

-- 5. 验证
SHOW INDEX FROM monitor_batch_v2 WHERE Key_name = 'uk_task_batch_epoch';
```

**说明**：新索引 `uk_task_batch_epoch(task_id, epoch, batch_no)` 允许同一任务的不同 epoch 有相同的 batch_no，支持零停机批次切换。

---

### 2️⃣ 动态锁超时（10行代码）

```java
// RedisLockUtil.java
public boolean tryLockWithDynamicTimeout(String lockKey, String requestId, int targetCount) {
    // 基础5分钟 + 每个目标20ms
    long timeoutMs = Math.max(300000L, targetCount * 20);
    return tryLock(lockKey, requestId, timeoutMs);
}
```

**使用**：
```java
// MonitorBatchServiceImpl.java
int targetCount = targetMapper.countActiveByTaskId(taskId);
if (!redisLockUtil.tryLockWithDynamicTimeout(lockKey, requestId, targetCount)) {
    logger.warn("任务 {} 正在处理中，跳过", taskId);
    return;
}
```

---

### 3️⃣ LIMIT动态配置（5行代码）

```yaml
# application.yml
monitor:
  batch:
    max-targets: 10000  # 单次同步最大目标数
```

```java
// MonitorTaskServiceImpl.java
@Value("${monitor.batch.max-targets:10000}")
private int maxTargets;

public void syncTargetsForTask(Long taskId) {
    List<TokenLaunchHistory> candidates = 
        tokenMapper.selectBySmartConditions(condition, maxTargets);
}
```

---

## ⭐ 核心改进：零停机批次切换（支持新旧epoch并存）

**问题**：唯一索引 `uk_task_batch(task_id, batch_no)` 不包括 `epoch`，导致零停机批次切换时冲突

**解决方案**（v2.2）：
1. **修改唯一索引**：`uk_task_batch_epoch(task_id, epoch, batch_no)` - 允许新旧epoch并存
2. **零停机流程**：先创建新epoch批次 → 更新current_epoch → 再删除旧epoch批次

```java
// ⭐ 零停机策略（先创建新批次，再删除旧批次）
// 1. 先分配新epoch批次（确保成功）
int allocatedCount = allocateBatches(taskId, newEpoch, latestCAs);

// 2. 更新任务的current_epoch（切换到新批次）
taskMapper.updateCurrentEpoch(taskId, newEpoch);

// 3. 删除旧epoch批次（epoch < newEpoch）
batchItemMapper.deleteBatchItemsByTaskId(taskId, newEpoch);  // 先删批次项
batchMapper.deleteBatchesByTaskId(taskId, newEpoch);         // 再删批次
```

**优势**：
- ✅ 零停机切换（Consumer始终有批次可用）
- ✅ 支持重复执行（新旧epoch可短暂并存）
- ✅ 批次号逻辑清晰（每个epoch从1开始）

---

## 🏗️ 核心架构

```
┌─────────────────────────────────────────┐
│  定时任务（每5分钟）                       │
│  - 筛选符合条件的Token                    │
│  - 对比差异（新增/失效）                   │
│  - 触发批次重分配                         │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  Redis分布式锁（防止并发冲突）             │
│  - 动态超时：基础5分钟 + targetCount*20ms │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  ⭐ 自动删除旧批次（支持重复执行）          │
│  - 先删除批次项                          │
│  - 再删除批次                            │
│  - 避免唯一索引冲突                       │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  固定256槽一致性哈希                      │
│  - CA → 固定槽（稳定）                    │
│  - 固定槽 → 批次（动态聚合）               │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  Epoch版本号机制（零中断切换）             │
│  - 新epoch批次插入                        │
│  - 更新current_epoch                     │
└─────────────────────────────────────────┘
                ↓
┌─────────────────────────────────────────┐
│  Python消费者                            │
│  - 只读取current_epoch的批次              │
│  - 零感知切换                            │
└─────────────────────────────────────────┘
```

---

## 🔧 核心实现（已有代码）

### 1. 固定槽数一致性哈希

```java
@Component
public class ConsistentHashUtil {
    
    @Value("${monitor.batch.fixed-bucket-count:256}")
    private int fixedBucketCount;  // 固定256个槽
    
    @Value("${monitor.batch.batch-size:99}")
    private int batchSize;
    
    // 缓存哈希环（启动时构建）
    private TreeMap<Long, Integer> cachedHashRing;
    
    @PostConstruct
    public void init() {
        cachedHashRing = buildHashRing();  // 256×150=38400个虚拟节点
    }
    
    /**
     * 分配目标到批次
     */
    public Map<Integer, List<MonitorTaskTarget>> allocate(List<MonitorTaskTarget> targets) {
        Map<Integer, List<MonitorTaskTarget>> allocation = new HashMap<>();
        
        // Phase 1: CA → 固定槽（一致性哈希）
        Map<Integer, List<MonitorTaskTarget>> slotMap = new HashMap<>();
        for (MonitorTaskTarget target : targets) {
            int slot = getSlot(target.getCa());
            slotMap.computeIfAbsent(slot, k -> new ArrayList<>()).add(target);
        }
        
        // Phase 2: 固定槽 → 批次（聚合）
        int batchNo = 0;
        List<MonitorTaskTarget> currentBatch = new ArrayList<>();
        
        for (List<MonitorTaskTarget> slotTargets : slotMap.values()) {
            for (MonitorTaskTarget target : slotTargets) {
                currentBatch.add(target);
                if (currentBatch.size() >= batchSize) {
                    allocation.put(batchNo++, new ArrayList<>(currentBatch));
                    currentBatch.clear();
                }
            }
        }
        
        if (!currentBatch.isEmpty()) {
            allocation.put(batchNo, currentBatch);
        }
        
        return allocation;
    }
    
    private int getSlot(String key) {
        long hash = murmur3Hash(key);
        Map.Entry<Long, Integer> entry = cachedHashRing.ceilingEntry(hash);
        return entry != null ? entry.getValue() : cachedHashRing.firstEntry().getValue();
    }
}
```

---

### 2. 批次重分配（Epoch版本号机制）

```java
@Service
public class MonitorBatchServiceImpl implements IMonitorBatchService {
    
    @Autowired
    private MonitorBatchMapper batchMapper;
    
    @Autowired
    private ConsistentHashUtil consistentHashUtil;
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 600)
    public void reallocateBatches(Long taskId) {
        String lockKey = "monitor:sync:task:" + taskId;
        String requestId = UUID.randomUUID().toString();
        
        // 动态锁超时
        int targetCount = targetMapper.countActiveByTaskId(taskId);
        if (!redisLockUtil.tryLockWithDynamicTimeout(lockKey, requestId, targetCount)) {
            logger.warn("任务 {} 正在处理中，跳过", taskId);
            return;
        }
        
        try {
            // 1. 查询当前epoch
            Integer oldEpoch = task.getCurrentEpoch() != null ? task.getCurrentEpoch() : 0;
            int newEpoch = oldEpoch + 1;
            
            // 2. ⭐ 先分配新epoch批次（零停机策略：确保成功后再切换）
            List<MonitorTaskTarget> targets = targetMapper.selectActiveByTaskId(taskId);
            Map<Integer, List<MonitorTaskTarget>> allocation = 
                consistentHashUtil.allocate(targets);
            
            // 2.1 插入新批次头
            for (Map.Entry<Integer, List<MonitorTaskTarget>> entry : allocation.entrySet()) {
                MonitorBatch batch = new MonitorBatch();
                batch.setTaskId(taskId);
                batch.setBatchNo(entry.getKey());
                batch.setEpoch(newEpoch);  // ⭐ 新epoch
                batch.setStatus("active");
                batch.setItemCount(entry.getValue().size());
                
                batchMapper.insertMonitorBatch(batch);  // useGeneratedKeys=true
                
                // 2.2 批量插入批次项（500条一批）
                List<MonitorBatchItem> items = new ArrayList<>();
                for (MonitorTaskTarget target : entry.getValue()) {
                    MonitorBatchItem item = new MonitorBatchItem();
                    item.setBatchId(batch.getId());  // 使用自增ID
                    item.setTaskId(taskId);
                    item.setCa(target.getCa());
                    item.setTokenName(target.getTokenName());
                    item.setStatus("active");
                    items.add(item);
                    
                    if (items.size() >= 500) {
                        batchItemMapper.batchInsert(items);
                        items.clear();
                    }
                }
                if (!items.isEmpty()) {
                    batchItemMapper.batchInsert(items);
                }
            }
            
            // 3. ⭐ 更新current_epoch（切换到新批次）
            taskMapper.updateCurrentEpoch(taskId, newEpoch);
            
            // 4. ⭐ 删除旧epoch批次（epoch < newEpoch，支持新旧epoch短暂并存）
            if (oldEpoch > 0) {
                logger.info("删除旧批次数据：taskId={}, oldEpoch={}, newEpoch={}", taskId, oldEpoch, newEpoch);
                batchItemMapper.deleteBatchItemsByTaskId(taskId, newEpoch);  // 先删批次项
                batchMapper.deleteBatchesByTaskId(taskId, newEpoch);         // 再删批次
            }
            
            logger.info("任务 {} 批次重分配完成：epoch {} -> {}, 批次数 {}, 零停机切换 ⭐", 
                        taskId, oldEpoch, newEpoch, allocation.size());
            
        } finally {
            redisLockUtil.releaseLock(lockKey, requestId);
        }
    }
}
```

---

### 3. 智能目标同步

```java
@Service
public class MonitorTaskServiceImpl implements IMonitorTaskService {
    
    @Autowired
    private MonitorTaskTargetMapper targetMapper;
    
    @Autowired
    private TokenLaunchHistoryMapper tokenMapper;
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    @Value("${monitor.batch.max-targets:10000}")
    private int maxTargets;
    
    @Override
    public void syncTargetsForTask(Long taskId) {
        String lockKey = "monitor:sync:task:" + taskId;
        String requestId = UUID.randomUUID().toString();
        
        if (!redisLockUtil.tryLock(lockKey, requestId, 300000)) {
            logger.warn("任务 {} 同步中，跳过", taskId);
            return;
        }
        
        try {
            // 1. 获取任务配置
            MonitorTask task = taskMapper.selectById(taskId);
            if (task == null || !task.getAutoSync()) {
                return;
            }
            
            // 2. 根据智能条件筛选Token
            SmartCondition condition = parseSmartCondition(task);
            List<TokenLaunchHistory> candidates = 
                tokenMapper.selectBySmartConditions(condition, maxTargets);
            
            // 3. 提取CA列表
            Set<String> newCAs = candidates.stream()
                .map(TokenLaunchHistory::getCa)
                .collect(Collectors.toSet());
            
            // 4. 查询现有目标
            List<MonitorTaskTarget> existingTargets = 
                targetMapper.selectActiveByTaskId(taskId);
            Set<String> existingCAs = existingTargets.stream()
                .map(MonitorTaskTarget::getCa)
                .collect(Collectors.toSet());
            
            // 5. 对比差异
            Set<String> toAdd = new HashSet<>(newCAs);
            toAdd.removeAll(existingCAs);
            
            Set<String> toRemove = new HashSet<>(existingCAs);
            toRemove.removeAll(newCAs);
            
            // 6. 新增目标
            if (!toAdd.isEmpty()) {
                List<MonitorTaskTarget> addList = new ArrayList<>();
                for (String ca : toAdd) {
                    TokenLaunchHistory token = candidates.stream()
                        .filter(t -> t.getCa().equals(ca))
                        .findFirst()
                        .orElse(null);
                    if (token != null) {
                        MonitorTaskTarget target = new MonitorTaskTarget();
                        target.setTaskId(taskId);
                        target.setCa(ca);
                        target.setTokenName(token.getTokenName());
                        target.setChainType(token.getChainType());
                        target.setStatus(1);
                        addList.add(target);
                    }
                }
                targetMapper.batchInsert(addList);
            }
            
            // 7. 失效目标
            if (!toRemove.isEmpty()) {
                targetMapper.updateStatusByCAs(taskId, toRemove, 0);
            }
            
            // 8. 触发重分配
            if (!toAdd.isEmpty() || !toRemove.isEmpty()) {
                batchService.reallocateBatches(taskId);
                logger.info("任务 {} 目标同步完成：新增 {}, 失效 {}", 
                            taskId, toAdd.size(), toRemove.size());
            }
            
        } finally {
            redisLockUtil.releaseLock(lockKey, requestId);
        }
    }
}
```

---

## 📅 定时任务配置

```java
@Component
public class MonitorTaskScheduler {
    
    @Autowired
    private IMonitorTaskService taskService;
    
    @Autowired
    private IMonitorBatchService batchService;
    
    /**
     * 自动同步智能监控目标（每5分钟）
     * 自动删除旧批次，支持重复执行 ⭐
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void autoSyncSmartTargets() {
        List<MonitorTask> tasks = taskMapper.selectAutoSyncTasks();
        for (MonitorTask task : tasks) {
            try {
                taskService.syncTargetsForTask(task.getId());
            } catch (Exception e) {
                logger.error("任务 {} 自动同步失败", task.getId(), e);
            }
        }
    }
}
```

---

## 🗄️ 数据库改动

### 表结构修改

```sql
-- monitor_batch_v2 添加epoch字段
ALTER TABLE monitor_batch_v2
ADD COLUMN epoch INT NOT NULL DEFAULT 0 COMMENT 'Epoch版本号' AFTER batch_no,
ADD COLUMN consumer_id VARCHAR(50) NULL COMMENT '消费者ID' AFTER status,
ADD COLUMN target_count INT NULL COMMENT '目标数量' AFTER consumer_id;

-- monitor_task_v2 添加current_epoch字段
ALTER TABLE monitor_task_v2
ADD COLUMN current_epoch INT NOT NULL DEFAULT 0 COMMENT '当前Epoch' AFTER status;

-- ⭐ 注意：旧批次自动删除，不需要archived_time字段
```

### 8个关键索引

```sql
-- 1. token_launch_history: 智能筛选查询
CREATE INDEX idx_token_chain_market_create 
ON token_launch_history(chain_type, market_cap, create_time);

CREATE INDEX idx_token_twitter 
ON token_launch_history(twitter_url(100));

-- 2. monitor_task_target_v2: 快速查询&去重
CREATE INDEX idx_target_task_status 
ON monitor_task_target_v2(task_id, status);

CREATE UNIQUE INDEX ux_target_task_ca 
ON monitor_task_target_v2(task_id, ca);

-- 3. monitor_batch_v2: epoch查询
CREATE INDEX idx_batch_task_epoch_status 
ON monitor_batch_v2(task_id, epoch, status);

CREATE INDEX idx_batch_consumer 
ON monitor_batch_v2(consumer_id);

-- 4. monitor_batch_item_v2: 批次项查询
CREATE INDEX idx_batch_item_batchid 
ON monitor_batch_item_v2(batch_id);

CREATE INDEX idx_batch_item_task_ca 
ON monitor_batch_item_v2(task_id, ca);
```

---

## 🎯 性能预期

| 场景 | 目标数 | 批次数 | 预期耗时 | 状态 |
|------|--------|--------|----------|------|
| 最小 | 100 | 2 | < 1秒 | ✅ |
| 常规 | 2000 | 21 | < 3秒 | ✅ |
| 最大 | 6153 | 62 | < 5秒 | ✅ |

**风险评估**：极低（中小规模，简单方案）

---

## 📋 实施计划（半天完成）

### 上午（2小时）
1. ✅ **数据库去重**（15分钟）
   - 备份数据
   - 执行去重SQL
   - 添加唯一索引
   - 添加8个关键索引

2. ✅ **代码修改**（20分钟）
   - 动态锁超时（10行）
   - LIMIT动态配置（5行）

3. ✅ **配置更新**（5分钟）
   - application.yml

### 下午（2小时）
1. ✅ **单元测试**（30分钟）
   - 100/1000/6000个目标
   - 验证耗时 < 5秒
   - 事务回滚测试

2. ✅ **集成测试**（30分钟）
   - 并发测试（10个请求）
   - 验证分布式锁

3. ✅ **灰度发布**（1小时）
   - 1个任务灰度
   - 观察24小时
   - 全量上线

---

## 📝 上线检查清单

### 数据库
- [ ] 备份已完成
- [ ] 去重SQL已执行
- [ ] 唯一索引已添加
- [ ] 8个关键索引已创建

### 代码
- [ ] 动态锁超时已实现
- [ ] LIMIT动态配置已完成
- [ ] 单元测试通过
- [ ] 集成测试通过

### 监控
- [ ] 关键日志已打印
- [ ] 错误日志已配置告警

### 灰度
- [ ] 选定灰度任务
- [ ] 观察24小时无异常
- [ ] 全量上线

---

## 🚀 后续优化（可选）

**仅当数据规模增长 > 10万目标时再考虑**：
- 差异更新（减少写入量）
- 临时表交换（毫秒级切换）
- Redisson Watchdog（自动续租）
- Prometheus监控（详细指标）

**当前阶段**：✅ 简单方案完全够用，别过度设计！

---

## 📞 常见问题

### Q1: 为什么用固定256槽而不是动态batchCount？
**A**: 动态batchCount会导致目标微增/微减时，大量CA被重新分配，Python消费者频繁重连。固定槽数确保80-90%的CA批次编号不变。

### Q2: Epoch版本号机制如何工作？（已优化）
**A**: ⭐ **零停机策略**（v2.1修复）：
1. 先创建新epoch批次（确保成功）
2. 更新current_epoch（切换到新批次）
3. 再删除旧epoch批次（epoch < newEpoch）
4. Python只读current_epoch的批次，确保Consumer始终有批次可用

### Q2.1: 为什么要分epoch删除旧批次？
**A**: ⭐ **索引设计变更**（v2.2）：
- **旧索引**：`uk_task_batch(task_id, batch_no)` - 不支持零停机（新旧epoch批次冲突）
- **新索引**：`uk_task_batch_epoch(task_id, epoch, batch_no)` - 支持零停机（新旧epoch可并存）
- 删除时只清理 `epoch < newEpoch` 的数据，确保新旧批次可以短暂共存
- 批次号每个epoch从1开始递增，逻辑清晰

### Q3: 为什么不需要Redisson？
**A**: 你的场景单次分配 < 5秒，固定5分钟 + 动态超时完全够用，不需要自动续租。

### Q4: 批量插入为什么500条一批？
**A**: 平衡性能与事务大小。500条一批可避免SQL过长，又能保持高性能。

---

**方案状态**：✅ 精简实用版，半天搞定

**文档行数**：~700行（删除了2300行冗余内容）

**核心原则**：适配实际场景，别过度设计！

---

## 📝 版本更新记录

### v2.2 (2025-11-13) - 唯一索引优化（支持零停机）

#### 🔴 P0: 唯一索引冲突问题（已修复）
- **问题**: 唯一索引 `uk_task_batch(task_id, batch_no)` 不包括 `epoch`，导致零停机批次切换时冲突
  - 新批次（`epoch=2, batch_no=1`）插入时，旧批次（`epoch=1, batch_no=1`）仍存在，违反唯一索引
  - 错误：`Duplicate entry 'taskId-1' for key 'monitor_batch_v2.uk_task_batch'`
- **修复**: 修改唯一索引为 `uk_task_batch_epoch(task_id, epoch, batch_no)`
  - 允许同一 `task_id` 的不同 `epoch` 有相同的 `batch_no`
  - 支持零停机：先创建新epoch批次，再删除旧epoch批次
  - 批次号每个epoch从1开始递增，逻辑清晰
- **影响文件**:
  - `sql/fix_batch_unique_index.sql`: DDL脚本（删除旧索引，创建新索引）
  - `MonitorBatchMapper.xml`: SQL查询添加epoch字段和过滤逻辑
- **效果**: 
  - ✅ 零停机批次切换不再冲突
  - ✅ 批次号逻辑清晰（每个epoch独立）
  - ✅ 代码无需修改（保持先插入新批次再删除旧批次的顺序）

---

### v2.1 (2025-11-13) - 关键问题修复

修复了生产环境发现的5个关键问题：

#### 🔴 P0: 零停机失败问题（已修复）
- **问题**: 先删除旧批次再创建新批次，若Consumer为空或分配失败，任务进入"无批次"状态
- **修复**: 调整顺序为"先创建新批次 → 更新epoch → 再删除旧批次"
- **影响文件**:
  - `SmartBatchServiceImpl.java`: 调整批次分配流程
  - `MonitorBatchMapper.java`: 删除方法支持epoch过滤
  - `MonitorBatchMapper.xml`: SQL只删除旧epoch批次
- **效果**: Consumer始终能读取到批次，确保零停机

#### 🟡 P1: Twitter筛选逻辑不正确（已修复）
- **问题**: 后端只要 `hasTwitter != null` 就强制 `requireTwitter=true`，无法筛选"无Twitter"项目
- **修复**: 支持4种精确筛选模式（对齐sol监控）
  - `null` - 不限
  - `"profile"` - 推特主页（不含 /status/、/communities/、/search）
  - `"tweet"` - 推文（含 /status/）
  - `"community"` - 社区（含 /communities/）
  - `"none"` - 无推特
- **影响文件**:
  - `SmartBatchServiceImpl.java`: 传递 `hasTwitter` 值而非固定true
  - `TokenLaunchHistoryMapper.xml`: 使用 `<choose>` 实现4种筛选SQL
- **效果**: 用户可精确筛选所需的Twitter类型

#### 🟡 P1: maxTargets硬编码（已修复）
- **问题**: 代码写死 `maxTargets = 20000`，`@Value` 配置失效
- **修复**: 改为 `conditions.put("maxTargets", this.maxTargets)`
- **影响文件**: `SmartBatchServiceImpl.java`
- **效果**: 运维可通过 `application.yml` 配置上限，无需改代码

#### 🟠 P2: O(n²)性能问题（已修复）
- **问题**: 为每个CA遍历 `allTargets` 列表查找，1万条数据需1亿次比较
- **修复**: 使用 `Map<String, MonitorTaskTarget>` 直接查找，时间复杂度O(1)
- **影响文件**: `SmartBatchServiceImpl.java`
- **效果**: 性能从5-10分钟优化到5秒以内

#### ❌ 架构澄清: 任务-配置关系
- **结论**: 实际是 **1对多** 关系（一个配置可被多个任务使用），而非M:N
- **说明**: 一个配置包含完整的监控规则（多个事件），足够一个任务使用
- **无需修改**: 当前设计合理

---

### v2.0 (2025-11-11) - 精简版发布
- 删除过度设计的复杂方案（临时表、Redisson、差异更新等）
- 只保留3个必须修改：去重+唯一索引、动态锁超时、LIMIT配置化
- 适配实际数据规模（6000条目标，<5秒分配）

---

