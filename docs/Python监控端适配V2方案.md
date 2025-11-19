# Python监控端适配V2方案

> **文档版本**: v1.2  
> **创建日期**: 2025-11-10  
> **最后更新**: 2025-11-19  
> **目标**: 让Python监控端适配新的monitor_*_v2表结构
> **最新更新**: 补充批量监控和区块监控适配方案

---

## 📋 目录

1. [核心变化](#核心变化)
2. [数据库连接](#数据库连接)
3. [加载任务和配置](#加载任务和配置)
4. [批次分配算法](#批次分配算法)
5. [配置变更感知](#配置变更感知)
6. [心跳上报机制](#心跳上报机制)
7. [完整代码示例](#完整代码示例)
8. [三种任务类型适配方案](#三种任务类型适配方案) ⭐ 新增

---

## 🔄 核心变化

### 旧系统 vs 新系统

| 维度 | 旧系统 | 新系统 V2 |
|------|--------|-----------|
| **配置表** | `quick_monitor_template` | `monitor_config_v2` |
| **任务表** | 无独立任务表 | `monitor_task_v2` |
| **目标表** | `sol_ws_batch_pool`（混合） | `monitor_task_target_v2` |
| **批次表** | `sol_ws_batch_pool`（混合） | `monitor_batch_v2` + `monitor_batch_item_v2` |
| **告警表** | 分散多处 | `monitor_alert_log_v2` |
| **关联关系** | 1:N（配置→Token） | ~~M:N（配置↔任务）~~ 1:N（配置→任务） + 1:N（任务→目标） ⭐ |

### 关键概念变化

```
旧系统：配置 → Token列表（batch_id分组）

新系统：配置 → 任务 → 目标列表 → 批次分配
         (1:N)  (1:1)  (1:N)
```

### ⭐ 重要说明：任务-配置关系

**架构设计**：
- ✅ 一个任务只能绑定**一个配置**（1:1关系）
- ✅ 一个配置可以被**多个任务**使用（1:N关系）
- ✅ 单个配置足够一个任务使用（可包含价格+持仓+成交量等多个事件规则）

**代码实现**：
- Python代码中仍使用 `configs` 数组，但**实际只有1个元素**
- 这样设计是为了代码兼容性和扩展性（未来如果需要支持多配置，改动最小）
- 实际使用时：`config = task['configs'][0]`

---

## 🔌 数据库连接

### 1. 配置文件（config.py）

```python
# config.py
import os
from dotenv import load_dotenv

load_dotenv()

DB_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'port': int(os.getenv('DB_PORT', 3306)),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD'),
    'database': os.getenv('DB_NAME', 'kakarot_web3'),
    'charset': 'utf8mb4'
}

REDIS_CONFIG = {
    'host': os.getenv('REDIS_HOST', 'localhost'),
    'port': int(os.getenv('REDIS_PORT', 6379)),
    'db': int(os.getenv('REDIS_DB', 0)),
    'password': os.getenv('REDIS_PASSWORD'),
    'decode_responses': True
}

# 监控配置
HEARTBEAT_INTERVAL = 30  # 心跳间隔（秒）
CONFIG_POLL_INTERVAL = 5  # 配置轮询间隔（秒）
BATCH_SIZE = 99  # 批次大小
```

---

## 📊 加载任务和配置

### 2. 数据加载器（data_loader.py）

```python
# data_loader.py
import pymysql
from typing import List, Dict
import json

class MonitorDataLoader:
    """从V2表结构加载任务和配置"""
    
    def __init__(self, db_config: dict):
        self.db_config = db_config
    
    def get_connection(self):
        """获取数据库连接"""
        return pymysql.connect(**self.db_config)
    
    def load_active_tasks(self) -> List[Dict]:
        """
        加载所有活跃任务（status=1）
        
        返回格式：
        [
            {
                'task_id': 1,
                'task_name': 'SOL智能监控',
                'task_type': 'smart',
                'chain_type': 'sol',
                'configs': [配置列表],
                'targets': [目标列表]  # 仅smart/batch有
            }
        ]
        """
        conn = self.get_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        
        try:
            # 查询活跃任务
            cursor.execute("""
                SELECT 
                    id as task_id,
                    task_name,
                    task_type,
                    chain_type,
                    status,
                    create_time,
                    update_time
                FROM monitor_task_v2
                WHERE status = 1
                  AND del_flag = 0
                ORDER BY id
            """)
            
            tasks = cursor.fetchall()
            
            # 为每个任务加载关联的配置和目标
            for task in tasks:
                task['configs'] = self._load_task_configs(cursor, task['task_id'])
                
                # 只有smart/batch类型任务才有目标
                if task['task_type'] in ['smart', 'batch']:
                    task['targets'] = self._load_task_targets(cursor, task['task_id'])
                else:
                    task['targets'] = []
            
            return tasks
            
        finally:
            cursor.close()
            conn.close()
    
    def _load_task_configs(self, cursor, task_id: int) -> List[Dict]:
        """加载任务关联的配置"""
        cursor.execute("""
            SELECT 
                c.id as config_id,
                c.config_name,
                c.config_category,
                c.chain_type,
                c.source,
                c.market_type,
                c.min_transaction_usd,
                c.cumulative_min_amount_usd,
                c.time_interval,
                c.top_holders_threshold,
                c.events_config,
                c.trigger_logic,
                c.notify_methods,
                c.version
            FROM monitor_config_v2 c
            INNER JOIN monitor_task_config_v2 tc ON c.id = tc.config_id
            WHERE tc.task_id = %s
              AND c.status = 1
              AND c.del_flag = 0
            ORDER BY tc.config_order
        """, (task_id,))
        
        configs = cursor.fetchall()
        
        # 解析 JSON 字段
        for config in configs:
            if config['events_config']:
                try:
                    config['events_config'] = json.loads(config['events_config'])
                except:
                    config['events_config'] = {}
        
        return configs
    
    def _load_task_targets(self, cursor, task_id: int) -> List[Dict]:
        """加载任务的监控目标"""
        cursor.execute("""
            SELECT 
                id as target_id,
                target_type,
                target_value as ca,
                token_name,
                token_symbol,
                market_cap,
                pair_address,
                source,
                batch_id,
                batch_order,
                priority,
                status
            FROM monitor_task_target_v2
            WHERE task_id = %s
              AND status = 1
            ORDER BY batch_id, batch_order
        """, (task_id,))
        
        return cursor.fetchall()
    
    def load_task_by_id(self, task_id: int) -> Dict:
        """加载单个任务"""
        conn = self.get_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        
        try:
            cursor.execute("""
                SELECT 
                    id as task_id,
                    task_name,
                    task_type,
                    chain_type,
                    status
                FROM monitor_task_v2
                WHERE id = %s
            """, (task_id,))
            
            task = cursor.fetchone()
            if not task:
                return None
            
            task['configs'] = self._load_task_configs(cursor, task_id)
            
            if task['task_type'] in ['smart', 'batch']:
                task['targets'] = self._load_task_targets(cursor, task_id)
            else:
                task['targets'] = []
            
            return task
            
        finally:
            cursor.close()
            conn.close()
```

---

## 🔢 批次分配算法

### 3. 批次分配器（batch_allocator.py）

```python
# batch_allocator.py
import hashlib
import pymysql
from typing import List, Dict
import logging

logger = logging.getLogger(__name__)

class BatchAllocator:
    """一致性哈希批次分配器"""
    
    BATCH_SIZE = 99
    HASH_SALT = "kakarot_v2_2025"  # 盐值版本
    
    def __init__(self, db_config: dict):
        self.db_config = db_config
    
    def get_connection(self):
        return pymysql.connect(**self.db_config)
    
    def allocate_batches(self, task_id: int):
        """
        为任务的所有目标分配批次
        
        步骤：
        1. 读取所有活跃目标
        2. 使用一致性哈希排序
        3. 按BATCH_SIZE分组（每批≤99个）
        4. 更新目标的batch_id和batch_order
        5. 同步批次头表
        """
        conn = self.get_connection()
        cursor = conn.cursor(pymysql.cursors.DictCursor)
        
        try:
            # 1. 读取活跃目标
            cursor.execute("""
                SELECT id, target_value, batch_id
                FROM monitor_task_target_v2
                WHERE task_id = %s AND status = 1
                ORDER BY id
            """, (task_id,))
            
            targets = cursor.fetchall()
            
            if not targets:
                logger.info(f"任务 {task_id} 无目标，跳过批次分配")
                return
            
            # 2. 一致性哈希排序
            targets_sorted = sorted(
                targets, 
                key=lambda x: self._hash_target(task_id, x['target_value'])
            )
            
            # 3. 分配批次号
            total_batches = (len(targets_sorted) + self.BATCH_SIZE - 1) // self.BATCH_SIZE
            
            updates = []
            for idx, target in enumerate(targets_sorted):
                new_batch_no = (idx // self.BATCH_SIZE) + 1
                new_batch_order = idx % self.BATCH_SIZE
                
                # 只更新变化的目标（减少数据库写入）
                if target.get('batch_id') != new_batch_no:
                    updates.append((new_batch_no, new_batch_order, target['id']))
            
            # 4. 批量更新
            if updates:
                cursor.executemany("""
                    UPDATE monitor_task_target_v2
                    SET batch_id = %s, batch_order = %s, update_time = NOW()
                    WHERE id = %s
                """, updates)
                
                logger.info(f"任务 {task_id}: {len(targets_sorted)} 个目标, "
                          f"{total_batches} 个批次, 变更 {len(updates)} 项")
            
            # 5. 同步批次头表
            self._sync_batch_headers(cursor, task_id, total_batches)
            
            conn.commit()
            
        except Exception as e:
            conn.rollback()
            logger.error(f"批次分配失败: {e}")
            raise
        finally:
            cursor.close()
            conn.close()
    
    def _hash_target(self, task_id: int, target_value: str) -> int:
        """计算目标的哈希值（一致性哈希）"""
        hash_input = f"{task_id}:{target_value}:{self.HASH_SALT}"
        hash_hex = hashlib.md5(hash_input.encode()).hexdigest()
        return int(hash_hex[:8], 16)  # 取前8位十六进制
    
    def _sync_batch_headers(self, cursor, task_id: int, expected_batches: int):
        """同步批次头表"""
        for batch_no in range(1, expected_batches + 1):
            # 计算批次项数
            cursor.execute("""
                SELECT COUNT(*) as item_count
                FROM monitor_task_target_v2
                WHERE task_id = %s AND batch_id = %s AND status = 1
            """, (task_id, batch_no))
            
            result = cursor.fetchone()
            item_count = result['item_count'] if result else 0
            
            # 插入或更新批次头
            cursor.execute("""
                INSERT INTO monitor_batch_v2 
                    (task_id, batch_no, item_count, status, create_time, update_time)
                VALUES (%s, %s, %s, 'pending', NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                    item_count = VALUES(item_count),
                    update_time = NOW()
            """, (task_id, batch_no, item_count))
```

---

## 🔔 配置变更感知

### 4. 配置监听器（config_watcher.py）

```python
# config_watcher.py
import redis
import threading
import time
import logging
from datetime import datetime
from typing import Callable

logger = logging.getLogger(__name__)

class ConfigWatcher:
    """配置变更监听器（Redis Pub/Sub + 轮询兜底）"""
    
    def __init__(self, redis_config: dict, db_config: dict):
        self.redis_client = redis.Redis(**redis_config)
        self.db_config = db_config
        self.config_cache = {}  # {config_id: {'version': 1, 'last_reload': datetime}}
        self.last_poll_time = datetime.now()
        self.callbacks = []  # 配置变更回调函数
        self.running = False
    
    def register_callback(self, callback: Callable[[int, int], None]):
        """
        注册配置变更回调函数
        
        Args:
            callback: 回调函数，签名为 callback(config_id, version)
        """
        self.callbacks.append(callback)
    
    def start(self):
        """启动配置监听"""
        self.running = True
        
        # 启动Redis订阅线程
        threading.Thread(target=self._subscribe_redis, daemon=True).start()
        
        # 启动轮询兜底线程
        threading.Thread(target=self._poll_changes, daemon=True).start()
        
        logger.info("配置监听器已启动")
    
    def stop(self):
        """停止配置监听"""
        self.running = False
        logger.info("配置监听器已停止")
    
    def _subscribe_redis(self):
        """Redis Pub/Sub订阅（实时）"""
        try:
            pubsub = self.redis_client.pubsub()
            pubsub.subscribe('monitor:config:changed')
            
            logger.info("Redis订阅已启动: monitor:config:changed")
            
            for message in pubsub.listen():
                if not self.running:
                    break
                
                if message['type'] == 'message':
                    try:
                        import json
                        event = json.loads(message['data'])
                        config_id = event.get('configId')
                        version = event.get('version')
                        
                        if config_id:
                            self._reload_config(config_id, version)
                    except Exception as e:
                        logger.error(f"处理Redis消息失败: {e}")
        
        except Exception as e:
            logger.error(f"Redis订阅失败: {e}")
    
    def _poll_changes(self):
        """轮询增量（5秒间隔，兜底）"""
        import pymysql
        
        while self.running:
            try:
                time.sleep(5)
                
                conn = pymysql.connect(**self.db_config)
                cursor = conn.cursor(pymysql.cursors.DictCursor)
                
                # 查询最近更新的配置
                cursor.execute("""
                    SELECT id, version, update_time
                    FROM monitor_config_v2
                    WHERE update_time > %s
                      AND status = 1
                    ORDER BY update_time DESC
                """, (self.last_poll_time,))
                
                changed_configs = cursor.fetchall()
                
                for config in changed_configs:
                    config_id = config['id']
                    version = config['version']
                    
                    # 检查版本是否更新
                    cached_version = self.config_cache.get(config_id, {}).get('version', 0)
                    if version > cached_version:
                        self._reload_config(config_id, version)
                
                self.last_poll_time = datetime.now()
                
                cursor.close()
                conn.close()
                
            except Exception as e:
                logger.error(f"轮询配置失败: {e}")
    
    def _reload_config(self, config_id: int, version: int):
        """
        重载配置（2秒去抖，幂等）
        
        Args:
            config_id: 配置ID
            version: 配置版本
        """
        # 2秒去抖：避免短时间内重复重载
        last_reload = self.config_cache.get(config_id, {}).get('last_reload')
        if last_reload:
            delta = (datetime.now() - last_reload).total_seconds()
            if delta < 2:
                logger.debug(f"配置 {config_id} 跳过重载（去抖）")
                return
        
        # 检查版本
        cached_version = self.config_cache.get(config_id, {}).get('version', 0)
        if version <= cached_version:
            logger.debug(f"配置 {config_id} 版本未变化，跳过")
            return
        
        # 更新缓存
        self.config_cache[config_id] = {
            'version': version,
            'last_reload': datetime.now()
        }
        
        logger.info(f"✅ 配置已重载: configId={config_id}, version={version}")
        
        # 调用回调函数
        for callback in self.callbacks:
            try:
                callback(config_id, version)
            except Exception as e:
                logger.error(f"配置变更回调失败: {e}")
```

---

## 💓 心跳上报机制

### 5. 心跳上报器（heartbeat_reporter.py）

```python
# heartbeat_reporter.py
import pymysql
import threading
import time
import os
import logging
from datetime import datetime

logger = logging.getLogger(__name__)

class HeartbeatReporter:
    """批次心跳上报器"""
    
    def __init__(self, db_config: dict, interval: int = 30):
        self.db_config = db_config
        self.interval = interval
        self.running = False
        self.worker_id = f"{os.getpid()}@{os.uname().nodename}"  # 进程ID@主机名
        self.batch_map = {}  # {batch_id: {'task_id': x, 'batch_no': y}}
    
    def register_batch(self, batch_id: int, task_id: int, batch_no: int):
        """注册需要上报心跳的批次"""
        self.batch_map[batch_id] = {
            'task_id': task_id,
            'batch_no': batch_no
        }
        logger.info(f"注册批次心跳: batch_id={batch_id}, task={task_id}, no={batch_no}")
    
    def unregister_batch(self, batch_id: int):
        """取消注册批次"""
        if batch_id in self.batch_map:
            del self.batch_map[batch_id]
            logger.info(f"取消批次心跳: batch_id={batch_id}")
    
    def start(self):
        """启动心跳上报线程"""
        self.running = True
        threading.Thread(target=self._heartbeat_loop, daemon=True).start()
        logger.info(f"心跳上报器已启动: worker_id={self.worker_id}, interval={self.interval}s")
    
    def stop(self):
        """停止心跳上报"""
        self.running = False
        logger.info("心跳上报器已停止")
    
    def _heartbeat_loop(self):
        """心跳循环"""
        while self.running:
            try:
                self._report_heartbeat()
                time.sleep(self.interval)
            except Exception as e:
                logger.error(f"心跳上报失败: {e}")
    
    def _report_heartbeat(self):
        """上报心跳"""
        if not self.batch_map:
            return
        
        conn = pymysql.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            for batch_id, info in self.batch_map.items():
                task_id = info['task_id']
                batch_no = info['batch_no']
                
                # 更新批次心跳
                cursor.execute("""
                    UPDATE monitor_batch_v2
                    SET last_heartbeat = NOW(),
                        worker_id = %s,
                        worker_pid = %s,
                        status = 'running',
                        update_time = NOW()
                    WHERE task_id = %s AND batch_no = %s
                """, (self.worker_id, os.getpid(), task_id, batch_no))
            
            conn.commit()
            logger.debug(f"心跳已上报: {len(self.batch_map)} 个批次")
            
        except Exception as e:
            conn.rollback()
            logger.error(f"心跳上报数据库错误: {e}")
        finally:
            cursor.close()
            conn.close()
    
    def report_alert(self, batch_id: int, task_id: int, batch_no: int):
        """上报告警数（增量）"""
        conn = pymysql.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            cursor.execute("""
                UPDATE monitor_batch_v2
                SET total_alerts = total_alerts + 1,
                    last_alert_time = NOW(),
                    update_time = NOW()
                WHERE task_id = %s AND batch_no = %s
            """, (task_id, batch_no))
            
            conn.commit()
            
        except Exception as e:
            conn.rollback()
            logger.error(f"上报告警数失败: {e}")
        finally:
            cursor.close()
            conn.close()
    
    def report_error(self, batch_id: int, task_id: int, batch_no: int, error_msg: str):
        """上报错误"""
        conn = pymysql.connect(**self.db_config)
        cursor = conn.cursor()
        
        try:
            cursor.execute("""
                UPDATE monitor_batch_v2
                SET error_count = error_count + 1,
                    last_error = %s,
                    update_time = NOW()
                WHERE task_id = %s AND batch_no = %s
            """, (error_msg[:500], task_id, batch_no))  # 限制错误信息长度
            
            conn.commit()
            
        except Exception as e:
            conn.rollback()
            logger.error(f"上报错误失败: {e}")
        finally:
            cursor.close()
            conn.close()
```

---

## 📦 完整代码示例

### 6. 监控主程序（monitor_main.py）

```python
# monitor_main.py
import logging
import signal
import sys
from config import DB_CONFIG, REDIS_CONFIG, HEARTBEAT_INTERVAL
from data_loader import MonitorDataLoader
from batch_allocator import BatchAllocator
from config_watcher import ConfigWatcher
from heartbeat_reporter import HeartbeatReporter

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class MonitorService:
    """监控服务主类"""
    
    def __init__(self):
        self.loader = MonitorDataLoader(DB_CONFIG)
        self.allocator = BatchAllocator(DB_CONFIG)
        self.watcher = ConfigWatcher(REDIS_CONFIG, DB_CONFIG)
        self.heartbeat = HeartbeatReporter(DB_CONFIG, HEARTBEAT_INTERVAL)
        
        self.tasks = []  # 当前加载的任务列表
        self.running = False
    
    def start(self):
        """启动监控服务"""
        logger.info("=" * 60)
        logger.info("监控系统 V2.0 启动中...")
        logger.info("=" * 60)
        
        # 1. 加载所有任务
        self._load_all_tasks()
        
        # 2. 启动配置监听
        self.watcher.register_callback(self._on_config_changed)
        self.watcher.start()
        
        # 3. 启动心跳上报
        self.heartbeat.start()
        
        # 4. 注册信号处理
        signal.signal(signal.SIGINT, self._signal_handler)
        signal.signal(signal.SIGTERM, self._signal_handler)
        
        self.running = True
        logger.info("✅ 监控系统 V2.0 已启动！")
        
        # 5. 开始监控循环
        self._monitor_loop()
    
    def _load_all_tasks(self):
        """加载所有活跃任务"""
        logger.info("正在加载任务...")
        
        self.tasks = self.loader.load_active_tasks()
        
        logger.info(f"✅ 已加载 {len(self.tasks)} 个任务")
        
        for task in self.tasks:
            logger.info(f"  - 任务 {task['task_id']}: {task['task_name']} "
                       f"({task['task_type']}, {task['chain_type']}) "
                       f"配置×{len(task['configs'])} 目标×{len(task['targets'])}")
            
            # 注册批次心跳
            if task['targets']:
                batch_ids = set(t['batch_id'] for t in task['targets'] if t.get('batch_id'))
                for batch_id in batch_ids:
                    self.heartbeat.register_batch(
                        batch_id, 
                        task['task_id'], 
                        batch_id
                    )
    
    def _on_config_changed(self, config_id: int, version: int):
        """配置变更回调"""
        logger.info(f"🔄 配置变更: config_id={config_id}, version={version}")
        
        # 找到使用该配置的任务
        affected_tasks = []
        for task in self.tasks:
            for config in task['configs']:
                if config['config_id'] == config_id:
                    affected_tasks.append(task['task_id'])
                    break
        
        if affected_tasks:
            logger.info(f"  影响任务: {affected_tasks}")
            # 重新加载这些任务
            for task_id in affected_tasks:
                self._reload_task(task_id)
    
    def _reload_task(self, task_id: int):
        """重新加载任务"""
        logger.info(f"重新加载任务 {task_id}...")
        
        new_task = self.loader.load_task_by_id(task_id)
        if not new_task:
            logger.warning(f"任务 {task_id} 不存在或已停用")
            return
        
        # 更新任务列表
        for i, task in enumerate(self.tasks):
            if task['task_id'] == task_id:
                self.tasks[i] = new_task
                logger.info(f"✅ 任务 {task_id} 已重载")
                break
    
    def _monitor_loop(self):
        """监控主循环"""
        import time
        
        while self.running:
            try:
                # 这里实现你的监控逻辑
                # 例如：处理WebSocket数据、检查告警规则等
                
                for task in self.tasks:
                    # 根据任务类型执行不同的监控逻辑
                    if task['task_type'] == 'smart':
                        self._monitor_smart_task(task)
                    elif task['task_type'] == 'batch':
                        self._monitor_batch_task(task)
                    elif task['task_type'] == 'block':
                        self._monitor_block_task(task)
                
                time.sleep(1)  # 避免CPU占用过高
                
            except Exception as e:
                logger.error(f"监控循环错误: {e}", exc_info=True)
    
    def _monitor_smart_task(self, task: dict):
        """监控智能任务"""
        # 实现你的智能监控逻辑
        pass
    
    def _monitor_batch_task(self, task: dict):
        """监控批量任务"""
        # 实现你的批量监控逻辑
        pass
    
    def _monitor_block_task(self, task: dict):
        """监控区块任务"""
        # 实现你的区块监控逻辑
        pass
    
    def _signal_handler(self, signum, frame):
        """信号处理（优雅退出）"""
        logger.info(f"收到信号 {signum}，正在优雅退出...")
        self.stop()
    
    def stop(self):
        """停止监控服务"""
        self.running = False
        self.watcher.stop()
        self.heartbeat.stop()
        logger.info("✅ 监控系统已停止")
        sys.exit(0)


if __name__ == '__main__':
    service = MonitorService()
    service.start()
```

---

## 🚀 使用示例

### 7. 启动监控服务

```bash
# 1. 安装依赖
pip install pymysql redis python-dotenv

# 2. 配置环境变量（.env）
cat > .env << EOF
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=kakarot_web3

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0
EOF

# 3. 运行批次分配（首次运行或目标变更后）
python batch_allocator_cli.py --task-id 1

# 4. 启动监控服务
python monitor_main.py
```

### 8. 批次分配CLI工具（batch_allocator_cli.py）

```python
# batch_allocator_cli.py
import argparse
import logging
from config import DB_CONFIG
from batch_allocator import BatchAllocator

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def main():
    parser = argparse.ArgumentParser(description='批次分配工具')
    parser.add_argument('--task-id', type=int, required=True, help='任务ID')
    args = parser.parse_args()
    
    allocator = BatchAllocator(DB_CONFIG)
    
    logger.info(f"开始为任务 {args.task_id} 分配批次...")
    allocator.allocate_batches(args.task_id)
    logger.info("✅ 批次分配完成")

if __name__ == '__main__':
    main()
```

---

## 📊 数据流对比

### 旧系统

```
Python启动
  → 查询 sol_ws_batch_pool (WHERE is_active=1)
  → 按 batch_id 分组
  → 订阅 WebSocket
  → 检测事件 → 发送告警
```

### 新系统V2

```
Python启动
  → 查询 monitor_task_v2 (WHERE status=1)
  → JOIN monitor_task_config_v2 加载配置
  → JOIN monitor_config_v2 加载规则
  → JOIN monitor_task_target_v2 加载目标
  → 按 batch_id 分组
  → 订阅 WebSocket
  → 检测事件 → 写入 monitor_alert_log_v2
  → 发送告警 + 上报心跳
```

---

## ✅ 关键检查清单

- [ ] 确认新表 `monitor_*_v2` 已创建
- [ ] 配置数据库连接（DB_CONFIG）
- [ ] 配置Redis连接（REDIS_CONFIG）
- [ ] 实现 `_monitor_smart_task` 监控逻辑
- [ ] 实现 `_monitor_batch_task` 监控逻辑
- [ ] 实现 `_monitor_block_task` 监控逻辑
- [ ] 实现告警写入 `monitor_alert_log_v2`
- [ ] 测试配置变更感知（Redis Pub/Sub）
- [ ] 测试心跳上报机制
- [ ] 测试批次分配算法

---

## 🔧 常见问题

### Q1: 旧系统的监控逻辑如何迁移？

**A**: 核心监控逻辑不变，只是数据源从旧表改为新表：

```python
# 旧代码
tokens = db.query("SELECT * FROM sol_ws_batch_pool WHERE is_active=1")

# 新代码
tasks = loader.load_active_tasks()
for task in tasks:
    for target in task['targets']:
        # target['ca'] 就是要监控的Token地址
```

### Q2: 如何处理配置变更？

**A**: 使用 `ConfigWatcher` 监听配置变更，自动重载：

```python
watcher.register_callback(lambda config_id, version: 
    print(f"配置{config_id}已更新到v{version}")
)
watcher.start()
```

### Q3: 批次分配何时触发？

**A**: 在以下情况触发：

1. 任务首次创建时
2. 新增/删除目标时
3. 手动调用 `allocate_batches(task_id)`

### Q4: 如何保证心跳不中断？

**A**: `HeartbeatReporter` 使用独立线程，30秒一次上报，即使主程序阻塞也不影响。

---

## 🔌 WebSocket推送集成

### 9. WebSocket推送器（websocket_pusher.py）

Python监控端可以通过WebSocket主动推送状态更新到前端。

```python
# websocket_pusher.py
import websocket
import json
import threading
import time
import logging
from typing import Dict, Any

logger = logging.getLogger(__name__)

class WebSocketPusher:
    """WebSocket推送器（连接到后端WebSocket服务）"""
    
    def __init__(self, ws_url: str):
        """
        初始化WebSocket推送器
        
        Args:
            ws_url: WebSocket服务器地址，例如：ws://localhost:8080/websocket/monitor
        """
        self.ws_url = ws_url
        self.ws = None
        self.connected = False
        self.running = False
        self.reconnect_interval = 5  # 重连间隔（秒）
    
    def connect(self):
        """连接到WebSocket服务器"""
        self.running = True
        threading.Thread(target=self._connect_loop, daemon=True).start()
        logger.info(f"WebSocket推送器已启动: {self.ws_url}")
    
    def _connect_loop(self):
        """连接循环（支持自动重连）"""
        while self.running:
            try:
                logger.info(f"正在连接WebSocket服务器: {self.ws_url}")
                
                self.ws = websocket.WebSocketApp(
                    self.ws_url,
                    on_open=self._on_open,
                    on_message=self._on_message,
                    on_error=self._on_error,
                    on_close=self._on_close
                )
                
                # 阻塞运行（直到连接关闭）
                self.ws.run_forever()
                
            except Exception as e:
                logger.error(f"WebSocket连接失败: {e}")
            
            # 如果还在运行状态，等待后重连
            if self.running:
                logger.info(f"{self.reconnect_interval}秒后尝试重连...")
                time.sleep(self.reconnect_interval)
    
    def _on_open(self, ws):
        """连接成功回调"""
        self.connected = True
        logger.info("✅ WebSocket连接成功")
        
        # 启动心跳线程
        threading.Thread(target=self._heartbeat_loop, daemon=True).start()
    
    def _on_message(self, ws, message):
        """接收消息回调"""
        try:
            data = json.loads(message)
            msg_type = data.get('type')
            
            if msg_type == 'pong':
                logger.debug("收到心跳响应")
            elif msg_type == 'connected':
                logger.info(f"服务器确认连接: {data.get('message')}")
            else:
                logger.info(f"收到消息: {msg_type}")
        except Exception as e:
            logger.error(f"处理消息失败: {e}")
    
    def _on_error(self, ws, error):
        """错误回调"""
        logger.error(f"WebSocket错误: {error}")
    
    def _on_close(self, ws, close_status_code, close_msg):
        """连接关闭回调"""
        self.connected = False
        logger.info(f"WebSocket连接关闭: {close_status_code} - {close_msg}")
    
    def _heartbeat_loop(self):
        """心跳循环"""
        while self.running and self.connected:
            try:
                self.send_message({'type': 'ping'})
                time.sleep(30)  # 每30秒发送一次心跳
            except Exception as e:
                logger.error(f"发送心跳失败: {e}")
                break
    
    def send_message(self, message: Dict[str, Any]):
        """发送消息"""
        if self.ws and self.connected:
            try:
                self.ws.send(json.dumps(message))
            except Exception as e:
                logger.error(f"发送消息失败: {e}")
        else:
            logger.warning("WebSocket未连接，无法发送消息")
    
    def push_batch_status(self, batch_id: int, status: str, details: Dict[str, Any] = None):
        """
        推送批次状态更新
        
        Args:
            batch_id: 批次ID
            status: 状态 (pending/running/paused/stopped/completed/error)
            details: 详细信息
        """
        message = {
            'type': 'batch_status',
            'data': {
                'batchId': batch_id,
                'status': status,
                'details': details or {},
                'timestamp': int(time.time() * 1000)
            }
        }
        self.send_message(message)
        logger.info(f"推送批次状态: batchId={batch_id}, status={status}")
    
    def push_task_status(self, task_id: int, status: int, message: str = ''):
        """
        推送任务状态更新
        
        Args:
            task_id: 任务ID
            status: 状态 (0=停止, 1=运行)
            message: 状态消息
        """
        msg = {
            'type': 'task_status',
            'data': {
                'taskId': task_id,
                'status': status,
                'message': message,
                'timestamp': int(time.time() * 1000)
            }
        }
        self.send_message(msg)
        logger.info(f"推送任务状态: taskId={task_id}, status={status}")
    
    def push_alert(self, alert_id: int, alert_type: str, alert_data: Dict[str, Any]):
        """
        推送告警通知
        
        Args:
            alert_id: 告警ID
            alert_type: 告警类型 (price_change/holder_change/volume_change/block_event)
            alert_data: 告警数据
        """
        message = {
            'type': 'alert',
            'data': {
                'alertId': alert_id,
                'alertType': alert_type,
                'alertData': alert_data,
                'timestamp': int(time.time() * 1000)
            }
        }
        self.send_message(message)
        logger.info(f"推送告警: alertId={alert_id}, type={alert_type}")
    
    def close(self):
        """关闭连接"""
        self.running = False
        self.connected = False
        if self.ws:
            self.ws.close()
        logger.info("WebSocket推送器已关闭")


# 使用示例
if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    
    # 创建WebSocket推送器
    pusher = WebSocketPusher('ws://localhost:8080/websocket/monitor')
    pusher.connect()
    
    # 等待连接建立
    time.sleep(2)
    
    # 推送测试消息
    pusher.push_batch_status(1, 'running', {'itemCount': 50, 'progress': 30})
    pusher.push_task_status(1, 1, '任务已启动')
    pusher.push_alert(1, 'price_change', {
        'tokenName': 'TestToken',
        'tokenSymbol': 'TEST',
        'ca': '0x1234567890',
        'priceChange': 25.5,
        'volume': 100000
    })
    
    # 保持运行
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        pusher.close()
```

### 集成到监控主程序

```python
# monitor_main.py (更新版)
from websocket_pusher import WebSocketPusher

class MonitorService:
    def __init__(self):
        # ... 其他初始化 ...
        
        # 初始化WebSocket推送器
        ws_url = os.getenv('WS_URL', 'ws://localhost:8080/websocket/monitor')
        self.ws_pusher = WebSocketPusher(ws_url)
    
    def start(self):
        # ... 其他启动代码 ...
        
        # 启动WebSocket推送
        self.ws_pusher.connect()
    
    def _on_batch_status_changed(self, batch_id, status):
        """批次状态变更时推送"""
        self.ws_pusher.push_batch_status(batch_id, status, {
            'itemCount': 50,
            'progress': 75
        })
    
    def _on_alert_triggered(self, alert_id, alert_type, alert_data):
        """触发告警时推送"""
        self.ws_pusher.push_alert(alert_id, alert_type, alert_data)
    
    def stop(self):
        # ... 其他停止代码 ...
        
        # 关闭WebSocket
        self.ws_pusher.close()
```

---

## 📡 WebSocket消息格式

### 前端 → 后端（Python）

**1. 心跳**
```json
{
  "type": "ping"
}
```

**2. 订阅主题**
```json
{
  "type": "subscribe",
  "topic": "batch_status"
}
```

### 后端（Python）→ 前端

**1. 连接成功**
```json
{
  "type": "connected",
  "sessionId": "abc123",
  "message": "WebSocket连接成功",
  "timestamp": 1699999999999
}
```

**2. 心跳响应**
```json
{
  "type": "pong",
  "timestamp": 1699999999999
}
```

**3. 批次状态更新**
```json
{
  "type": "batch_status",
  "data": {
    "batchId": 1,
    "status": "running",
    "details": {
      "itemCount": 50,
      "progress": 30,
      "errorCount": 0
    }
  },
  "timestamp": 1699999999999
}
```

**4. 任务状态更新**
```json
{
  "type": "task_status",
  "data": {
    "taskId": 1,
    "status": 1,
    "message": "任务已启动"
  },
  "timestamp": 1699999999999
}
```

**5. 告警通知**
```json
{
  "type": "alert",
  "data": {
    "alertId": 123,
    "alertType": "price_change",
    "alertData": {
      "tokenName": "Solana",
      "tokenSymbol": "SOL",
      "ca": "So11111111111111111111111111111111111111112",
      "priceChange": 25.5,
      "volume": 1000000,
      "marketCap": 50000000
    }
  },
  "timestamp": 1699999999999
}
```

---

## 📝 版本更新记录

### v1.2 (2025-11-19) - 补充批量和区块监控适配方案

**新增内容**：
- ✅ 补充**批量监控任务 (batch)** 完整适配方案
- ✅ 补充**区块监控任务 (block)** 完整适配方案
- ✅ 添加三种任务类型对比表格
- ✅ 明确批量监控可直接复用智能监控逻辑
- ✅ 明确区块监控需要从配置驱动（不再硬编码）
- ✅ 提供区块监控配置结构（events_config）建议

**核心要点**：
1. **批量监控**：数据结构和处理逻辑与智能监控完全一致，唯一区别是目标来源（手动 vs 自动）
2. **区块监控**：无目标列表，配置驱动订阅，心跳机制为任务级别（非批次级别）
3. **适配难度**：智能监控（已完成）< 批量监控（简单）< 区块监控（中等）

---

### v1.1 (2025-11-13) - 架构关系修正

**修正内容**：
- ✅ 修正任务-配置关系：~~M:N（多对多）~~ → **1:N（一对多）**
- ✅ 明确说明：一个任务只能绑定一个配置
- ✅ 补充：Python代码中 `configs` 数组实际只有1个元素
- ✅ 更新：关系图和说明文字

**修改原因**：
- 单个配置足够一个任务使用（可包含多个事件规则）
- 前端实现采用单选（configId），而非多选（configIds）
- 简化业务逻辑，降低复杂度

**代码影响**：
- ⚠️ Python代码中保持 `configs` 数组结构（兼容性）
- ⚠️ 实际使用时取第一个元素：`config = task['configs'][0]`
- ⚠️ SQL查询会只返回1条配置记录

---

## 🎯 三种任务类型适配方案

### 1️⃣ 智能监控任务 (smart) ✅ 已完成

**特点**：
- 自动从 `token_launch_history` 筛选目标
- 监控指定的 Token 列表
- 基于事件规则触发告警

**适配要点**：
- ✅ 从 `task['targets']` 获取监控目标（CA地址）
- ✅ 使用 `task['configs'][0]['events_config']` 获取事件规则
- ✅ 按 `batch_id` 分组，每批≤99个目标
- ✅ 为每个批次订阅 WebSocket（价格/持仓/成交量）
- ✅ 触发告警时写入 `monitor_alert_log_v2`
- ✅ 上报批次心跳到 `monitor_batch_v2`

---

### 2️⃣ 批量监控任务 (batch) 🔧 待适配

**特点**：
- 用户手动上传 CA 列表
- 监控大量 Token（可能>1000个）
- 使用统一的监控规则

**与智能监控的区别**：
- 智能监控：目标自动同步（定时任务）
- 批量监控：目标手动导入（一次性）

**适配方案**：

#### 数据加载方式
```
与智能监控完全相同：
1. 从 monitor_task_v2 加载任务信息
2. 从 monitor_task_target_v2 加载目标列表（CA地址）
3. 从 monitor_config_v2 加载监控规则
4. 按 batch_id 分组处理
```

#### 核心流程
1. **启动阶段**
   - 加载任务：`task_type = 'batch'`
   - 读取目标：与 smart 相同，都来自 `monitor_task_target_v2`
   - 检查批次状态：确认哪些批次需要运行

2. **监控执行**
   - 与智能监控逻辑**完全一致**
   - 订阅 WebSocket 价格/持仓/成交量数据
   - 检测事件规则（`events_config`）
   - 触发告警写入数据库

3. **心跳上报**
   - 每30秒上报批次心跳
   - 记录处理进度和错误计数
   - 告警时更新 `total_alerts` 和 `last_alert_time`

#### 关键注意点
- ⚠️ **批次数量可能很大**：如果有5000个目标，会生成51个批次
- ⚠️ **内存管理**：不要一次性加载所有批次到内存
- ⚠️ **优雅处理**：支持动态加载/卸载批次
- ⚠️ **目标不自动更新**：批量任务的目标是静态的，不会自动同步

#### 实现建议
```
建议1：复用智能监控的代码
- _monitor_batch_task() 可以直接调用 _monitor_smart_task()
- 两者的数据结构和处理逻辑完全一致
- 唯一区别是目标来源（自动 vs 手动）

建议2：分批加载（如果目标很多）
- 不要一次性订阅所有批次
- 按优先级或顺序逐批处理
- 动态注册/注销批次心跳

建议3：添加批次过滤
- 根据 batch_status 过滤（只处理 running 状态）
- 根据 epoch 过滤（只处理最新版本）
```

---

### 3️⃣ 区块监控任务 (block) 🔧 待适配

**特点**：
- 全链监控，无具体目标
- 监控新区块、交易、合约事件
- 当前是**写死的**，没有读取配置

**核心变化**：
```
旧方式：硬编码订阅参数（写死在代码里）
新方式：从 monitor_config_v2.events_config 读取配置
```

**适配方案**：

#### 数据结构差异
| 字段 | 智能/批量监控 | 区块监控 |
|------|-------------|---------|
| `task['targets']` | ✅ 有（CA列表） | ❌ 无（空数组） |
| `task['configs'][0]` | ✅ 有 | ✅ 有 |
| `events_config` | 价格/持仓/成交量规则 | 区块/交易/事件规则 ⭐ |

#### 配置结构（events_config）
建议的 JSON 结构：
```json
{
  "monitor_new_blocks": true,           // 是否监控新区块
  "block_interval_threshold": 15,       // 出块间隔阈值（秒）
  
  "monitor_large_transactions": true,   // 是否监控大额交易
  "transaction_amount_threshold": 100000, // 交易金额阈值（USD）
  
  "monitor_contract_events": true,      // 是否监控合约事件
  "contract_addresses": [                // 要监控的合约地址列表
    "0x1234...",
    "0x5678..."
  ],
  "event_signatures": [                  // 要监控的事件签名
    "Transfer(address,address,uint256)",
    "Approval(address,address,uint256)"
  ],
  
  "chain_specific": {                    // 链特定配置
    "sol": {
      "monitor_program_logs": true,
      "program_ids": ["TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"]
    },
    "eth": {
      "monitor_pending_txs": false
    }
  }
}
```

#### 核心流程

1. **启动阶段**
   ```
   - 加载任务：task_type = 'block'
   - 读取配置：config = task['configs'][0]
   - 解析规则：events_config = config['events_config']
   - 跳过目标加载：task['targets'] = []（空数组）
   ```

2. **订阅策略**
   ```
   根据 events_config 动态决定订阅内容：
   
   if events_config['monitor_new_blocks']:
       订阅 newHeads（新区块）
   
   if events_config['monitor_contract_events']:
       订阅 logs（合约事件）
       - 过滤条件：contract_addresses
       - 过滤条件：event_signatures
   
   if events_config['monitor_large_transactions']:
       订阅 pendingTransactions 或解析区块中的交易
   ```

3. **告警触发**
   ```
   不同于智能/批量监控（按Token维度），区块监控按事件维度：
   
   - 新区块告警：出块间隔异常
   - 大额交易告警：交易金额超过阈值
   - 合约事件告警：监听到指定事件
   
   告警写入：
   - alert_type: 'block_event' / 'large_transaction' / 'contract_event'
   - alert_data: 包含区块号、交易哈希、事件详情等
   ```

4. **心跳上报**
   ```
   区块监控没有批次概念，心跳方式不同：
   
   - 不使用 monitor_batch_v2（因为没有批次）
   - 直接通过 WebSocket 推送任务状态
   - 或者更新 monitor_task_v2.last_heartbeat 字段
   ```

#### 关键注意点

- ⚠️ **无目标列表**：`task['targets']` 为空，不要尝试访问
- ⚠️ **配置驱动**：所有订阅参数从 `events_config` 读取，不要硬编码
- ⚠️ **链差异**：不同链的 WebSocket 格式不同（SOL vs ETH vs BSC）
- ⚠️ **性能考虑**：全链监控数据量大，需要合理过滤
- ⚠️ **心跳机制**：没有批次概念，需要设计任务级别的心跳

#### 实现建议

```
建议1：配置模板化
- 在前端创建区块任务时，提供配置模板
- 用户勾选要监控的事件类型
- 自动生成 events_config JSON

建议2：订阅管理器
- 创建独立的 BlockSubscriptionManager
- 根据 events_config 动态管理订阅
- 支持热更新（配置变更时重新订阅）

建议3：告警分类
- 区块告警：block_abnormal（出块异常）
- 交易告警：large_transaction（大额交易）
- 事件告警：contract_event（合约事件）
- 便于前端分类展示和筛选

建议4：链适配层
- 抽象不同链的差异
- 统一的事件处理接口
- 便于后续扩展新链
```

---

## 📝 总结

### 核心变更点

1. ✅ **数据加载**：从 `sol_ws_batch_pool` → `monitor_task_v2` + `monitor_task_target_v2`
2. ✅ **配置管理**：从单表 → ~~多对多关联~~ 一对多关联（配置可复用）⭐
3. ✅ **批次分配**：新增一致性哈希算法
4. ✅ **配置感知**：Redis Pub/Sub + 轮询兜底
5. ✅ **心跳上报**：独立线程，定时上报状态
6. ✅ **WebSocket推送**：实时状态更新到前端

### 三种任务类型对比

| 维度 | 智能监控 (smart) | 批量监控 (batch) | 区块监控 (block) |
|------|----------------|----------------|----------------|
| **目标来源** | 自动筛选 | 手动导入 | 无目标 |
| **目标表** | ✅ `monitor_task_target_v2` | ✅ `monitor_task_target_v2` | ❌ 不使用 |
| **批次分配** | ✅ 需要 | ✅ 需要 | ❌ 不需要 |
| **监控内容** | Token 价格/持仓/成交量 | Token 价格/持仓/成交量 | 区块/交易/事件 |
| **配置结构** | 事件规则（Token维度） | 事件规则（Token维度） | 事件规则（链维度）⭐ |
| **心跳上报** | 批次级别 | 批次级别 | 任务级别 ⭐ |
| **适配难度** | ✅ 已完成 | 🟡 简单（复用smart） | 🔴 中等（需重构） |

### 优势

- 📊 **数据结构清晰**：职责分离，便于维护
- 🚀 **性能优化**：配置复用，减少冗余
- 🔔 **实时感知**：配置变更<2秒生效
- 💓 **健康监控**：心跳机制，故障可见
- 🔌 **实时推送**：WebSocket双向通信，前端实时更新
- 🎯 **类型明确**：三种任务类型各司其职，互不干扰

---

## 附录C：关键实现细节（FAQ）

### Q1: Consumer ID 应该如何生成？

**推荐：动态生成（hostname + pid）** ✅

```python
import socket
import os

# 启动时生成唯一的 consumer_id
CONSUMER_ID = f"{socket.gethostname()}-{os.getpid()}"
# 例如: "server-01-12345"
```

**配置示例**：

```python
# config.py
CONSUMER_ID_PREFIX = os.getenv("CONSUMER_ID_PREFIX", socket.gethostname())
CONSUMER_ID = f"{CONSUMER_ID_PREFIX}-{os.getpid()}"
```

**优势**：
- ✅ 多实例部署时自动唯一
- ✅ 重启后自动生成新ID（避免僵尸批次）
- ✅ 便于运维排查（hostname + pid）

---

### Q2: 批次ID应该使用哪个字段？

**关键变更**：使用 `monitor_batch_v2.id` 而不是 `batch_no` ⭐

| 字段 | 类型 | 唯一性 | 用途 |
|------|------|--------|------|
| `id` | BIGINT | 全局唯一 ⭐ | Python端使用（心跳/状态上报） |
| `batch_no` | INT | 任务内唯一 | 显示用（如"第3批"） |

**Python端修改**：

```python
# ❌ 错误（batch_no只在任务内唯一，跨任务会冲突）
batch_id = batch['batch_no']

# ✅ 正确（id是全局唯一的主键）
batch_id = batch['id']
```

**SQL查询修改**：

```sql
SELECT 
    b.id AS batch_id,          -- 全局唯一ID，Python端使用 ⭐
    b.task_id,
    b.batch_no,                -- 任务内批次号，显示用（"第3批"）
    b.epoch,
    b.consumer_id,
    b.status
FROM monitor_batch_v2 b
INNER JOIN monitor_task_v2 t ON b.task_id = t.id
WHERE b.task_id = ? 
  AND b.epoch = t.current_epoch
ORDER BY b.batch_no;
```

---

### Q3: Python端需要直接更新数据库心跳吗？

**答案：不需要！** ❌

**正确的心跳流程**：

```
Python端                Java端                 数据库
  |                      |                      |
  | 1. 执行批次          |                      |
  |                      |                      |
  | 2. WebSocket心跳 --->| 3. 接收心跳          |
  |    (每30秒)          |                      |
  |                      | 4. 更新数据库 -----> | 5. 持久化
  |                      |                      |
  |                      | 6. 广播给前端 -----> | (WebSocket推送)
  |                      |    (实时展示)        |
```

**Python端实现**（只推送，不写库）：

```python
class MonitorTaskRunner:
    def run_batch(self, batch):
        batch_id = batch['id']  # 全局唯一ID
        
        # ✅ 只通过WebSocket上报
        self.ws_client.update_batch_status(
            batch_id=batch_id,
            status="running",
            progress=50
        )
        
        # ❌ 不直接更新数据库
        # self.db.update_batch_heartbeat(batch_id)  # 删除这行！
```

**WebSocket消息格式**：

```json
{
  "type": "batch_heartbeat",
  "data": {
    "batchId": 12345,          // 全局唯一ID（monitor_batch_v2.id）⭐
    "taskId": 4,               // 任务ID（可选，用于验证）
    "consumerId": "server-01-12345",
    "progress": 50,
    "timestamp": 1731484800
  }
}
```

**优势**：
- ✅ **单一写入点**：只有Java更新数据库，避免冲突
- ✅ **实时性**：WebSocket推送，前端立即可见
- ✅ **解耦**：Python无需关心数据库schema变更
- ✅ **安全性**：Python无需数据库写权限

---

### Q4: 是否需要实现批次优先级？

**当前版本：不实现** ⭐

**理由**：
1. ✅ 每个任务独立运行，不存在跨任务竞争
2. ✅ Python端按顺序消费批次，全量处理
3. ✅ 增加优先级会增加复杂度，收益不大

**如果未来需要支持**：

```sql
-- 添加优先级字段
ALTER TABLE monitor_batch_v2 
ADD COLUMN priority INT DEFAULT 0 COMMENT '批次优先级（越大越优先）';

-- Python端按优先级排序
SELECT ... 
FROM monitor_batch_v2 
ORDER BY priority DESC, batch_no ASC;
```

**适用场景**：
- 🎯 VIP任务优先处理
- 🎯 高价值目标优先监控
- 🎯 紧急任务插队

---

### Q5: 自动更新后，批次会立即切换吗？

**答案：不会立即切换，采用零停机策略** ✅

**流程**：

```
1. Java端检测到配置/筛选条件变更
   ↓
2. 智能同步：生成新的目标列表
   ↓
3. 分配新批次（epoch+1）
   ↓
4. 更新 monitor_task_v2.current_epoch = epoch+1
   ↓
5. Python端下次查询时自动获取新批次
   ↓
6. 旧批次继续运行，直到完成
   ↓
7. 10分钟后Java端清理旧批次数据
```

**关键点**：
- ✅ Python端始终查询 `epoch = current_epoch` 的批次
- ✅ 旧批次不会立即中断，完成当前轮次即可
- ✅ 新批次和旧批次可短暂并存（grace period）
- ✅ 10分钟窗口期，确保旧批次平滑退出

**Python端代码**（无需修改）：

```python
# 每次查询都会自动获取最新epoch的批次
batches = self.get_batches_for_task(task_id)
# 查询条件：WHERE epoch = (SELECT current_epoch FROM monitor_task_v2 WHERE id = ?)
```

---

**文档版本**: v1.2  
**最后更新**: 2025-11-19  
**作者**: Kakarot Team  
**状态**: 智能监控✅ | 批量监控🔧 | 区块监控🔧

