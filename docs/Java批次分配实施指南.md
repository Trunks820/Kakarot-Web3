# Java批次分配实施指南

> **执行时间**：半天（4小时）  
> **执行日期**：2025-11-11  
> **执行人**：按实际填写

---

## ✅ 已完成的准备工作

### 1. SQL脚本已创建 ✅
**文件**：`sql/monitor_v2_optimization.sql`
- 数据备份
- 去重逻辑
- 唯一索引
- Epoch字段
- 8个关键索引

### 2. 工具类已创建 ✅
**文件**：`ruoyi-crypto/src/main/java/com/ruoyi/crypto/util/RedisLockUtil.java`
- `tryLock()` - 固定超时
- `tryLockWithDynamicTimeout()` - 动态超时 ⭐
- `releaseLock()` - 安全释放（Lua脚本）

### 2.1 核心服务已实现 ✅
**文件**：`ruoyi-crypto/src/main/java/com/ruoyi/crypto/service/impl/SmartBatchServiceImpl.java`
- 智能目标同步 ⭐
- 自动删除旧批次（支持重复执行）⭐
- Epoch版本管理 ⭐
- 一致性哈希分配 ⭐

### 3. 配置已更新 ✅
**文件**：`ruoyi-admin/src/main/resources/application.yml`
```yaml
monitor:
  batch:
    fixed-bucket-count: 256  # 固定槽数
    virtual-nodes: 150       # 虚拟节点数
    batch-size: 99           # 批次大小
    max-targets: 10000       # 单次同步最大目标数
```

### 4. SQL方法已添加 ✅
**文件**：`ruoyi-crypto/src/main/resources/mapper/crypto/TokenLaunchHistoryMapper.xml`
- `selectBySmartConditions` - 智能筛选（支持LIMIT动态配置）

---

## 📋 实施步骤（按顺序执行）

### 上午任务（2小时）

#### 步骤1：执行数据库脚本（15-20分钟）

```bash
# 1. 连接数据库
mysql -h your_host -u your_user -p your_database

# 2. 执行脚本（逐步执行，每一步都验证）
source sql/monitor_v2_optimization.sql;

# 3. 验证结果
SHOW INDEX FROM monitor_task_target_v2;
SHOW INDEX FROM monitor_batch_v2;
SELECT COUNT(*) FROM monitor_task_target_v2_backup;
```

**检查清单**：
- [ ] 备份表已创建
- [ ] 重复数据已删除
- [ ] 唯一索引 `ux_target_task_ca` 已创建
- [ ] epoch字段已添加到 `monitor_batch_v2`
- [ ] current_epoch字段已添加到 `monitor_task_v2`
- [ ] 8个关键索引已全部创建

---

#### 步骤2：重启应用（5分钟）

```bash
# 停止应用
cd E:\KakarotWorkSpace\Kakarot-Web3
./bin/clean.bat

# 启动应用
./bin/run.bat

# 检查日志
tail -f ruoyi-admin/logs/sys-info.log
```

**检查清单**：
- [ ] 应用启动成功
- [ ] RedisLockUtil bean加载成功
- [ ] Monitor配置加载成功（日志中搜索"monitor.batch"）
- [ ] 无报错信息

---

#### 步骤3：验证Redis锁工具（10分钟）

打开浏览器控制台或Postman，测试API：

```bash
# 测试获取锁（固定超时）
POST http://localhost:8080/crypto/monitor-v2/test/redis-lock
{
  "lockKey": "test:lock:1",
  "requestId": "test-request-1",
  "expireMs": 5000
}

# 预期响应：{"success": true, "message": "获取锁成功"}

# 测试动态超时
POST http://localhost:8080/crypto/monitor-v2/test/redis-lock-dynamic
{
  "lockKey": "test:lock:2",
  "requestId": "test-request-2",
  "targetCount": 6000
}

# 预期响应：{"success": true, "timeout": 420000, "message": "获取锁成功，超时7分钟"}
```

**检查清单**：
- [ ] Redis连接正常
- [ ] 锁可以正常获取
- [ ] 锁可以正常释放
- [ ] 动态超时计算正确

---

### 下午任务（2小时）

#### 步骤4：单元测试（30分钟）

创建测试类：

```java
@SpringBootTest
public class MonitorV2Test {
    
    @Autowired
    private RedisLockUtil redisLockUtil;
    
    @Test
    public void testDynamicLockTimeout() {
        String lockKey = "monitor:sync:task:test";
        String requestId = UUID.randomUUID().toString();
        
        // 测试不同目标数量的超时时间
        int[] targetCounts = {100, 1000, 6153};
        for (int count : targetCounts) {
            boolean locked = redisLockUtil.tryLockWithDynamicTimeout(lockKey, requestId, count);
            assertTrue(locked);
            
            long ttl = redisLockUtil.getLockTTL(lockKey);
            logger.info("目标数 {} -> 锁超时 {}秒", count, ttl / 1000);
            
            redisLockUtil.releaseLock(lockKey, requestId);
        }
    }
}
```

**检查清单**：
- [ ] 100个目标：超时5分钟
- [ ] 1000个目标：超时5分钟20秒
- [ ] 6153个目标：超时7分钟3秒
- [ ] 锁释放成功

---

#### 步骤5：集成测试（30分钟）

测试并发锁：

```bash
# 使用JMeter或手动并发请求
for i in {1..10}; do
  curl -X POST "http://localhost:8080/crypto/monitor-v2/task/1/sync-targets" &
done
wait

# 检查日志
grep "获取锁成功" logs/sys-info.log | wc -l  # 应该 = 1
grep "正在处理中，跳过" logs/sys-info.log | wc -l  # 应该 >= 9
```

**检查清单**：
- [ ] 只有1个请求获取锁成功
- [ ] 其他请求被正确跳过
- [ ] 无异常日志
- [ ] 锁被正确释放

---

#### 步骤6：灰度发布（1小时）

选择一个测试任务进行灰度：

```sql
-- 1. 选择一个测试任务
SELECT id, task_name FROM monitor_task_v2 WHERE status = '1' LIMIT 1;

-- 2. 手动触发目标同步
```

访问：`http://localhost:8080/crypto/monitor-v2/task/{taskId}/sync-targets`

**观察指标**：
- [ ] 同步耗时 < 5秒
- [ ] 批次正确生成
- [ ] epoch正确递增
- [ ] 无报错
- [ ] Python消费者正常读取

**如果正常，继续观察24小时**：
- [ ] 定时任务（每5分钟）正常执行
- [ ] 批次健康检查正常
- [ ] 无重复分配
- [ ] 无消费者重连

---

## 🎯 性能验证清单

执行性能测试，记录实际耗时：

| 场景 | 目标数 | 预期耗时 | 实际耗时 | 状态 |
|------|--------|----------|----------|------|
| 最小 | 100 | < 1秒 | ___秒 | ⬜ |
| 常规 | 2000 | < 3秒 | ___秒 | ⬜ |
| 最大 | 6153 | < 5秒 | ___秒 | ⬜ |

**如果实际耗时 > 预期**，检查：
- 数据库索引是否创建成功
- 批量插入是否生效（500条一批）
- Redis连接是否正常

---

## 🚨 回滚方案（如果出现问题）

### 1. 回滚数据库

```sql
-- 恢复备份
TRUNCATE TABLE monitor_task_target_v2;
INSERT INTO monitor_task_target_v2 
SELECT * FROM monitor_task_target_v2_backup;

-- 删除索引
ALTER TABLE monitor_task_target_v2 DROP INDEX ux_target_task_ca;
ALTER TABLE monitor_batch_v2 DROP COLUMN epoch;
ALTER TABLE monitor_batch_v2 DROP COLUMN archived_time;
ALTER TABLE monitor_task_v2 DROP COLUMN current_epoch;
```

### 2. 回滚配置

```yaml
# 注释掉monitor配置
# monitor:
#   batch:
#     ...
```

### 3. 重启应用

```bash
./bin/clean.bat
./bin/run.bat
```

---

## 📝 实施记录表

| 步骤 | 开始时间 | 结束时间 | 状态 | 备注 |
|------|----------|----------|------|------|
| 步骤1：执行数据库脚本 | ___:___ | ___:___ | ⬜ | |
| 步骤2：重启应用 | ___:___ | ___:___ | ⬜ | |
| 步骤3：验证Redis锁 | ___:___ | ___:___ | ⬜ | |
| 步骤4：单元测试 | ___:___ | ___:___ | ⬜ | |
| 步骤5：集成测试 | ___:___ | ___:___ | ⬜ | |
| 步骤6：灰度发布 | ___:___ | ___:___ | ⬜ | |

**完成时间**：____年__月__日 __:__

**执行人签字**：___________

---

## ✅ 实施完成标志

- [ ] 所有数据库改动已执行并验证
- [ ] 应用正常启动无报错
- [ ] Redis锁工具测试通过
- [ ] 单元测试全部通过
- [ ] 集成测试全部通过
- [ ] 灰度任务运行正常（24小时）
- [ ] 性能指标达标（< 5秒）
- [ ] Python消费者正常工作
- [ ] 无异常日志
- [ ] 回滚方案已准备

**全部完成后，方可全量上线！** 🚀

