# 智能监控 Epoch 问题修复

> **问题**: 智能监控任务 `current_epoch = 0`，但批次 `epoch = 50`  
> **任务ID**: 9 (smart)  
> **影响**: Python端无法查询到批次

---

## 🔍 问题分析

### 数据状态

```sql
-- 任务
SELECT id, task_name, task_type, current_epoch 
FROM monitor_task_v2 
WHERE id = 9;
-- 结果：current_epoch = 0 ❌

-- 批次
SELECT DISTINCT epoch, COUNT(*) 
FROM monitor_batch_v2 
WHERE task_id = 9 
GROUP BY epoch 
ORDER BY epoch DESC LIMIT 5;
-- 结果：epoch = 50, 49, 48... ✅
```

### 为什么会这样？

智能监控的 `syncTargetsAndAllocateBatches()` **应该会更新 current_epoch**（代码第232-236行）：

```java
// 7.2 更新任务的current_epoch（切换到新批次）
MonitorTask updateTask = new MonitorTask();
updateTask.setId(taskId);
updateTask.setCurrentEpoch(newEpoch);  // ← 应该会更新
updateTask.setUpdateTime(new Date());
monitorTaskMapper.updateMonitorTask(updateTask);
```

但是 `current_epoch` 仍然是 0，说明：

---

## 🐛 根本原因

### 原因1：创建时没有初始化 ⭐ 最可能

**文件**: `MonitorTaskServiceImpl.java` - `insertSmartTask()` 方法

```java
@Override
@Transactional
public int insertSmartTask(MonitorTask monitorTask) {
    monitorTask.setTaskType("smart");
    monitorTask.setCreateBy(SecurityUtils.getUsername());
    monitorTask.setCreateTime(new Date());
    
    if (monitorTask.getStatus() == null) {
        monitorTask.setStatus(1);
    }
    
    // ❌ 缺失：没有初始化 current_epoch = 0
    // ⚠️ 数据库默认值可能是 NULL，导致后续判断出错
    
    int result = monitorTaskMapper.insertMonitorTask(monitorTask);
    
    // 关联配置...
    
    return result;
}
```

**问题**：
- 创建时 `current_epoch` 可能是 `NULL`
- 第一次同步时，代码判断 `oldEpoch = 0`（NULL 被当作 0）
- 但 `existingMaxEpoch = 50`（因为已经同步了50次）
- 计算：`newEpoch = max(0, 50) + 1 = 51`
- **但是！** `updateMonitorTask()` 可能只更新非 NULL 字段
- 如果 `current_epoch` 一开始是 NULL，UPDATE 可能不生效

---

### 原因2：UPDATE 语句有问题

**检查**: `MonitorTaskMapper.xml` - `updateMonitorTask` 方法

可能使用了动态 SQL，只更新非 NULL 字段：

```xml
<update id="updateMonitorTask" parameterType="MonitorTask">
    UPDATE monitor_task_v2
    <set>
        <if test="taskName != null">task_name = #{taskName},</if>
        <if test="currentEpoch != null">current_epoch = #{currentEpoch},</if>
        <!-- ⚠️ 如果 currentEpoch 为 NULL，这条不会执行 -->
        <!-- 但初始值就是 NULL，所以永远不会更新！ -->
    </set>
    WHERE id = #{id}
</update>
```

---

### 原因3：数据库字段默认值问题

```sql
-- 检查表结构
SHOW CREATE TABLE monitor_task_v2;

-- 如果 current_epoch 字段定义为：
-- current_epoch INT DEFAULT NULL
-- ↑ 这会导致创建时是 NULL，而不是 0
```

---

## 🔧 修复方案

### 方案1：立即修复数据（紧急）

```sql
-- 将所有任务的 current_epoch 更新为最大的 batch epoch
UPDATE monitor_task_v2 t
SET current_epoch = (
    SELECT COALESCE(MAX(epoch), 0)
    FROM monitor_batch_v2 b 
    WHERE b.task_id = t.id
)
WHERE task_type = 'smart'
  AND id IN (
      SELECT DISTINCT task_id 
      FROM monitor_batch_v2
  );

-- 验证
SELECT 
    t.id,
    t.task_name,
    t.task_type,
    t.current_epoch,
    COALESCE(MAX(b.epoch), 0) as max_batch_epoch
FROM monitor_task_v2 t
LEFT JOIN monitor_batch_v2 b ON t.id = b.task_id
WHERE t.task_type = 'smart'
GROUP BY t.id, t.task_name, t.task_type, t.current_epoch
HAVING t.current_epoch != COALESCE(MAX(b.epoch), 0);
-- 预期：返回空（说明已修复）
```

---

### 方案2：修复创建逻辑

**文件**: `MonitorTaskServiceImpl.java`

```java
@Override
@Transactional
public int insertSmartTask(MonitorTask monitorTask) {
    monitorTask.setTaskType("smart");
    monitorTask.setCreateBy(SecurityUtils.getUsername());
    monitorTask.setCreateTime(new Date());
    
    if (monitorTask.getStatus() == null) {
        monitorTask.setStatus(1);
    }
    
    // ⭐ 新增：初始化 current_epoch = 0
    monitorTask.setCurrentEpoch(0);
    
    int result = monitorTaskMapper.insertMonitorTask(monitorTask);
    
    // 关联配置...
    
    return result;
}
```

---

### 方案3：修复数据库字段默认值

```sql
-- 检查当前定义
SHOW CREATE TABLE monitor_task_v2;

-- 如果是 NULL，修改为 DEFAULT 0
ALTER TABLE monitor_task_v2 
MODIFY COLUMN current_epoch INT DEFAULT 0 COMMENT '当前批次版本';

-- 更新现有的 NULL 值
UPDATE monitor_task_v2 
SET current_epoch = 0 
WHERE current_epoch IS NULL;
```

---

### 方案4：检查 UPDATE 语句

**文件**: `MonitorTaskMapper.xml`

**如果使用动态 SQL，确保 current_epoch 总是更新**：

```xml
<update id="updateMonitorTask" parameterType="MonitorTask">
    UPDATE monitor_task_v2
    <set>
        <if test="taskName != null">task_name = #{taskName},</if>
        <if test="taskType != null">task_type = #{taskType},</if>
        <if test="chainType != null">chain_type = #{chainType},</if>
        
        <!-- ⭐ 方案A：允许更新为 0 -->
        <if test="currentEpoch != null">current_epoch = #{currentEpoch},</if>
        
        <!-- ⭐ 方案B（推荐）：即使是 NULL 也更新（使用 COALESCE） -->
        current_epoch = #{currentEpoch},
        
        <if test="status != null">status = #{status},</if>
        <if test="updateTime != null">update_time = #{updateTime},</if>
        <if test="updateBy != null and updateBy != ''">update_by = #{updateBy},</if>
    </set>
    WHERE id = #{id}
</update>
```

---

## 🧪 验证修复

### 步骤1：修复现有数据

```sql
UPDATE monitor_task_v2 t
SET current_epoch = (
    SELECT COALESCE(MAX(epoch), 0)
    FROM monitor_batch_v2 b 
    WHERE b.task_id = t.id
)
WHERE id = 9;

-- 检查
SELECT id, task_name, current_epoch 
FROM monitor_task_v2 
WHERE id = 9;
-- 预期：current_epoch = 50 ✅
```

---

### 步骤2：测试新创建的智能任务

```bash
# 1. 创建智能任务
POST /crypto/monitor-v2/task/smart
{
  "taskName": "测试智能任务",
  "chainType": "sol",
  "configId": 1,
  "autoSyncTargets": 1,
  "syncIntervalMinutes": 5
}

# 2. 立即查询
SELECT id, task_name, current_epoch 
FROM monitor_task_v2 
WHERE id = (最新的task_id);
-- 预期：current_epoch = 0 ✅（初始值）

# 3. 触发第一次同步
# 等待定时任务执行，或手动调用同步接口

# 4. 再次查询
SELECT id, current_epoch 
FROM monitor_task_v2 
WHERE id = (最新的task_id);
-- 预期：current_epoch = 1 ✅（第一次同步后）

SELECT DISTINCT epoch 
FROM monitor_batch_v2 
WHERE task_id = (最新的task_id);
-- 预期：epoch = 1 ✅（与 current_epoch 一致）
```

---

### 步骤3：测试多次同步

```sql
-- 手动触发多次同步（或等待定时任务）
-- 每次同步后检查

SELECT 
    t.id,
    t.current_epoch,
    COUNT(DISTINCT b.epoch) as epoch_count,
    MAX(b.epoch) as max_epoch
FROM monitor_task_v2 t
LEFT JOIN monitor_batch_v2 b ON t.id = b.task_id
WHERE t.id = (测试的task_id)
GROUP BY t.id, t.current_epoch;

-- 预期：
-- current_epoch 应该等于 max_epoch
-- 例如：current_epoch = 3, max_epoch = 3 ✅
```

---

## 🚨 为什么 epoch 会到 50？

**可能的原因**：

### 1. 定时任务频繁执行

**检查定时任务配置**：

```java
// SmartBatchSyncTask.java
@Scheduled(cron = "0 * * * * ?")  // 每分钟执行一次
public void scanAndExecuteTasks() {
    // ...
}
```

**如果**：
- 任务配置了 `syncIntervalMinutes = 1`（每分钟同步）
- 定时任务运行了 50 分钟
- 每次都触发了批次分配
- 结果：epoch = 50

---

### 2. 手动测试触发

**检查是否有手动调用**：

```sql
-- 查看批次创建时间
SELECT 
    task_id,
    epoch,
    COUNT(*) as batch_count,
    MIN(create_time) as first_batch_time,
    MAX(create_time) as last_batch_time
FROM monitor_batch_v2
WHERE task_id = 9
GROUP BY task_id, epoch
ORDER BY epoch DESC
LIMIT 10;

-- 如果时间间隔很短，说明是频繁触发
```

---

### 3. 有多个 Consumer 实例

**检查批次分配时的 Consumer 数量**：

```sql
SELECT 
    task_id,
    epoch,
    consumer_id,
    COUNT(*) as batch_count
FROM monitor_batch_v2
WHERE task_id = 9
  AND epoch = 50
GROUP BY task_id, epoch, consumer_id;

-- 如果有多个 consumer_id，说明有多实例
-- 但这不应该导致 epoch 激增
```

---

### 4. 目标频繁变化

**检查目标变化历史**：

如果智能监控的筛选条件导致目标频繁增删，每次都会触发批次重新分配（epoch + 1）。

```sql
-- 检查任务配置
SELECT 
    id, 
    task_name, 
    auto_sync_targets, 
    sync_interval_minutes
FROM monitor_task_v2
WHERE id = 9;

-- 如果 sync_interval_minutes = 1，且目标经常变化
-- 那么每分钟都会 epoch + 1
-- 50分钟 = epoch 50
```

---

## 🎯 建议

### 立即执行

1. **修复数据**（SQL UPDATE）
2. **检查定时任务配置**（是否过于频繁）
3. **修改创建逻辑**（初始化 current_epoch = 0）

### 长期优化

1. **优化同步策略**
   - 如果目标没有变化，不要重新分配批次
   - 可以添加"目标变更检测"逻辑

2. **调整同步间隔**
   - 如果不需要实时性，可以设置更长的间隔（如 5-10 分钟）

3. **添加监控**
   - 监控 epoch 增长速度
   - 如果异常快速增长，发出告警

---

**文档版本**: v1.0  
**最后更新**: 2025-11-19  
**任务类型**: 智能监控 (smart)

