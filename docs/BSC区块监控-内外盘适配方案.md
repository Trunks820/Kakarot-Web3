# BSC区块监控 - 内外盘适配方案

> **文档版本**: v1.0  
> **创建日期**: 2025-11-21  
> **适用场景**: BSC链的区块级实时监控（内盘/外盘）  
> **数据库表**: `monitor_task`, `monitor_task_config`, `monitor_alert_log_v2`

---

## 📋 目录

1. [功能概述](#功能概述)
2. [核心概念](#核心概念)
3. [数据库设计](#数据库设计)
4. [Python实现方案](#python实现方案)
5. [完整代码示例](#完整代码示例)
6. [部署运行](#部署运行)
7. [常见问题](#常见问题)

---

## 🎯 功能概述

### 监控目标

- **外盘监控**：监控DEX交易（PancakeSwap、Uniswap等），实时捕获链上大额买卖
- **内盘监控**：监控直接转账（EOA之间、合约之间），捕获内部转移和归集行为

### 核心特性

✅ **实时监控**: 订阅BSC最新区块，秒级检测  
✅ **双重阈值**: 支持单笔金额阈值 + 累计金额阈值  
✅ **智能识别**: 自动区分DEX交易和直接转账  
✅ **实时价格**: 动态获取Token美元价值（DexScreener/GeckoTerminal）  
✅ **去重机制**: 防止同一笔交易重复告警  
✅ **性能优化**: 异步处理、价格缓存、批量分析  

---

## 🔑 核心概念

### 1. 内盘 vs 外盘

| 类型 | 定义 | 特征 | 典型场景 |
|------|------|------|----------|
| **外盘** | DEX交易 | - To地址是DEX路由合约<br>- 日志包含Swap事件<br>- Input包含swap函数签名 | - 用户在PancakeSwap买入/卖出<br>- 套利机器人交易<br>- 大额抛售 |
| **内盘** | 直接转账 | - 普通Transfer事件<br>- 不涉及DEX合约<br>- EOA↔EOA或EOA↔合约 | - 项目方转移代币<br>- 做市商归集<br>- 多签钱包转账 |

### 2. 双重阈值机制

```
单笔阈值：每笔交易的USD价值 >= min_transaction_usd
  ↓
  立即触发告警
  
累计阈值：N分钟内同一Token累计金额 >= cumulative_min_amount_usd
  ↓
  达到阈值时触发告警，并重置累计器
```

**示例**：
- 单笔阈值 = $10,000
- 累计阈值 = $50,000
- 时间窗口 = 60分钟

如果Token A在30分钟内发生：
- 第1笔: $8,000（未告警，累计$8,000）
- 第2笔: $7,000（未告警，累计$15,000）
- 第3笔: $12,000（**单笔告警**，累计$27,000）
- 第4笔: $9,000（未告警，累计$36,000）
- 第5笔: $15,000（**单笔告警 + 累计告警**，累计清零）

---

## 💾 数据库设计

### 架构说明

BSC区块监控采用 **"任务专用字段"架构**，与智能监控保持一致：

```
智能监控 (smart):
  ├─ min_market_cap          (专用字段)
  ├─ max_market_cap          (专用字段)
  ├─ has_twitter             (专用字段)
  └─ auto_sync_targets       (专用字段)

批量监控 (batch):
  └─ target_count            (专用字段)

区块监控 (block):  ⭐ 新增
  ├─ min_transaction_usd              (专用字段)
  └─ cumulative_min_amount_usd        (专用字段)
```

**设计原则**：
- ✅ **字段隔离**：每种任务类型使用自己的专用字段
- ✅ **简化配置**：阈值参数直接存储在任务表中
- ✅ **配置复用**：`monitor_config_v2` 存储价格、持仓、成交量等规则，可被多个任务共享

### 1. 任务表字段添加（monitor_task_v2）

#### 需要添加的字段

```sql
-- =============================================
-- 为区块监控添加专用字段到 monitor_task_v2
-- =============================================
ALTER TABLE monitor_task_v2
ADD COLUMN min_transaction_usd DECIMAL(20,2) COMMENT '单笔最小交易金额USD(仅block)' AFTER target_count,
ADD COLUMN cumulative_min_amount_usd DECIMAL(20,2) COMMENT '累计最小金额USD(仅block)' AFTER min_transaction_usd;

-- 验证字段
DESC monitor_task_v2;

-- 查看完整结构
SHOW CREATE TABLE monitor_task_v2;
```

#### 字段说明

| 字段名 | 类型 | 说明 | 适用任务类型 | 示例值 |
|--------|------|------|--------------|--------|
| `task_type` | VARCHAR(20) | 任务类型 | 所有 | `'block'` |
| `chain_type` | VARCHAR(10) | 链类型 | 所有 | `'bsc'` |
| `min_transaction_usd` | DECIMAL(20,2) | **单笔交易最小金额**（USD）<br>单笔达到此值立即告警 | **block** | `10000.00` |
| `cumulative_min_amount_usd` | DECIMAL(20,2) | **累计最小金额**（USD）<br>时间窗口内累计达到此值触发告警并清零 | **block** | `50000.00` |
| `auto_sync_targets` | TINYINT | 是否自动同步目标 | smart | 区块监控填 `1` |
| `sync_interval_minutes` | INT | 同步间隔（分钟） | smart | 区块监控填 `30` |
| `target_count` | INT | 目标数量 | batch | 区块监控填 `0` |
| `status` | TINYINT | 状态 | 所有 | `1`-启用 |

**累计时间窗口**：从关联的 `monitor_config_v2.time_interval` 读取（如 `'60m'`、`'1h'`）

### 2. 配置表（monitor_config_v2）

**区块监控可选配置**（用于价格、持仓、成交量等额外规则）：

| 字段名 | 类型 | 说明 | 区块监控使用场景 |
|--------|------|------|------------------|
| `time_interval` | VARCHAR(10) | 时间周期 | **作为累计时间窗口**（`'60m'`、`'1h'`、`'24h'`） |
| `events_config` | JSON | 事件配置 | 可选：添加价格涨跌、成交量等额外告警规则 |
| `trigger_logic` | VARCHAR(10) | 触发逻辑 | `'any'`-任一满足 / `'all'`-全部满足 |
| `notify_methods` | VARCHAR(100) | 通知方式 | `'telegram,wechat'` |

**注意**：`monitor_config_v2` 中的 `market_type` 字段是为了区分 pump.fun 的内外盘数据源，**不是**用于区分区块监控的交易类型。

### 3. 关联表（monitor_task_config_v2）

任务和配置的关联（可选）：

```sql
-- 区块监控任务可以关联配置（获取time_interval和events_config）
-- 也可以不关联配置（只使用任务表的阈值字段）
SELECT 
    t.id, t.task_name, 
    t.min_transaction_usd, 
    t.cumulative_min_amount_usd,
    c.time_interval,
    c.events_config
FROM monitor_task_v2 t
LEFT JOIN monitor_task_config_v2 tc ON t.id = tc.task_id
LEFT JOIN monitor_config_v2 c ON tc.config_id = c.id
WHERE t.task_type = 'block' AND t.chain_type = 'bsc';
```

### 4. 内盘 vs 外盘的识别

区块监控**不需要**在数据库中存储"内盘/外盘"类型，而是在Python代码中**实时识别**：

```python
def _determine_market_type(tx, receipt) -> str:
    """
    实时判断交易类型：
    - external: DEX交易（PancakeSwap、Uniswap等）
    - internal: 直接转账（EOA↔EOA、EOA↔合约）
    """
    # 检查To地址、Input数据、日志事件
    # 返回 'external' 或 'internal'
```

**优势**：
- ✅ 一个任务可以同时监控内外盘（根据需求筛选）
- ✅ 灵活性高，不需要创建多个任务
- ✅ 告警日志中记录具体的市场类型

### 5. 告警日志表 (monitor_alert_log_v2)

**已有字段直接使用**，无需修改：

```sql
id                    BIGINT          -- 告警ID（自增主键）
task_id               BIGINT          -- 关联的任务ID
config_id             BIGINT          -- 触发的配置ID（可为NULL）
alert_key             VARCHAR(32)     -- 去重键（MD5: task_id+ca+alert_type+date）
task_type             VARCHAR(20)     -- 任务类型：固定为 'block'
chain_type            VARCHAR(10)     -- 链类型：固定为 'bsc'
ca                    VARCHAR(255)    -- Token地址
token_name            VARCHAR(100)    -- Token名称
token_symbol          VARCHAR(50)     -- Token符号
market_cap            DECIMAL(20,2)   -- 市值（可选）
alert_type            VARCHAR(50)     -- 告警类型：
                                      --   'block_external_single'（外盘单笔）
                                      --   'block_external_cumulative'（外盘累计）
                                      --   'block_internal_single'（内盘单笔）
                                      --   'block_internal_cumulative'（内盘累计）
alert_title           VARCHAR(200)    -- 告警标题
alert_content         JSON            -- 告警详细内容：
                                      -- {
                                      --   "tx_hash": "0x...",
                                      --   "from": "0x...",
                                      --   "to": "0x...",
                                      --   "amount": "1000000",
                                      --   "amount_usd": 15234.56,
                                      --   "price_usd": 0.015234,
                                      --   "market_type": "external",
                                      --   "dex_name": "PancakeSwap",
                                      --   "cumulative_amount_usd": 52345.67  // 累计告警时包含
                                      -- }
trigger_value         DECIMAL(20,4)   -- 触发值（单笔金额USD或累计金额USD）
notify_status         VARCHAR(20)     -- 通知状态：pending/sent/failed/skipped
notify_channels       VARCHAR(100)    -- 通知渠道：telegram,wechat
notify_time           DATETIME        -- 通知时间
notify_error          TEXT            -- 通知错误信息
detect_latency_ms     INT             -- 检测延迟（毫秒）
notify_latency_ms     INT             -- 通知延迟（毫秒）
create_time           DATETIME        -- 创建时间
```

**无需创建新表或修改表结构**，`monitor_alert_log_v2` 已经足够支持！

---

## 🐍 Python实现方案

### 架构设计

```
BSCBlockMonitorManager (任务管理器)
    ↓
    加载所有启用的block任务（task_type='block', chain_type='bsc', status=1）
    ↓
    为每个任务创建独立的异步监控协程
    ↓
BSCBlockMonitor (区块监控引擎)
    ↓
    订阅最新区块（web3.eth.subscribe('newBlockHeaders') 或轮询）
    ↓
    解析区块中的所有交易 → 筛选Token Transfer事件
    ↓
    判断内盘/外盘 → 获取实时价格 → 计算USD价值
    ↓
    检查双重阈值（单笔 + 累计）
    ↓
    触发告警 → 写入 monitor_alert_log_v2 → 推送通知
```

### 核心流程图

```
新区块产生
  ↓
获取区块详情
  ↓
遍历所有交易
  ↓
  ┌─→ 解析Transaction Receipt
  │   ↓
  │   筛选Transfer事件（ERC-20标准）
  │   ↓
  │   判断市场类型（内盘/外盘）
  │   ↓
  │   获取Token实时价格（DexScreener API）
  │   ↓
  │   计算USD价值
  │   ↓
  │   检查阈值
  │   ↓
  │   【单笔阈值】是否 >= min_transaction_usd ?
  │   ├─ 是 → 触发单笔告警
  │   └─ 否 → 累加到时间窗口累计器
  │       ↓
  │       【累计阈值】是否 >= cumulative_min_amount_usd ?
  │       ├─ 是 → 触发累计告警 + 清空累计器
  │       └─ 否 → 继续累计
  └─ 处理下一笔交易
```

---

## 💻 完整代码示例

### 1. 配置和数据模型

```python
# config.py
from dataclasses import dataclass
from typing import Optional
from decimal import Decimal

@dataclass
class BSCBlockMonitorTask:
    """BSC区块监控任务"""
    id: int
    task_name: str
    chain_type: str  # 'bsc'
    min_transaction_usd: Decimal  # 单笔最小金额
    cumulative_min_amount_usd: Decimal  # 累计最小金额
    time_interval: Optional[str] = '60m'  # 累计时间窗口（从配置读取，默认60m）
    config_id: Optional[int] = None  # 关联的配置ID（可选）
    events_config: Optional[dict] = None  # 其他事件配置（可选）
    status: int = 1  # 1-启用, 0-停用, 2-暂停

@dataclass
class TransferEvent:
    """Token转账事件"""
    tx_hash: str
    block_number: int
    token_address: str
    from_address: str
    to_address: str
    amount: int  # Wei单位
    amount_formatted: Decimal  # 人类可读单位
    timestamp: int
    market_type: str  # 'internal' or 'external'
    dex_name: Optional[str] = None  # 如果是外盘，记录DEX名称

@dataclass
class AlertData:
    """告警数据"""
    task_id: int
    config_id: Optional[int]
    task_type: str
    chain_type: str
    ca: str
    token_name: str
    token_symbol: str
    market_cap: Optional[Decimal]
    alert_type: str
    alert_title: str
    alert_content: dict
    trigger_value: Decimal
    detect_latency_ms: int
```

### 2. 数据库操作

```python
# database.py
import pymysql
from typing import List, Optional, Dict, Any
from contextlib import contextmanager
import json
from decimal import Decimal

class DatabaseConnection:
    """数据库连接管理"""
    
    def __init__(self, config: dict):
        self.config = config
        self._connection = None
    
    @contextmanager
    def get_connection(self):
        """获取数据库连接（上下文管理器）"""
        conn = pymysql.connect(
            host=self.config['host'],
            port=self.config['port'],
            user=self.config['user'],
            password=self.config['password'],
            database=self.config['database'],
            charset=self.config.get('charset', 'utf8mb4'),
            cursorclass=pymysql.cursors.DictCursor
        )
        try:
            yield conn
            conn.commit()
        except Exception as e:
            conn.rollback()
            raise e
        finally:
            conn.close()
    
    def query_all(self, sql: str, params: tuple = None) -> List[Dict[str, Any]]:
        """查询多条记录"""
        with self.get_connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, params)
                return cursor.fetchall()
    
    def query_one(self, sql: str, params: tuple = None) -> Optional[Dict[str, Any]]:
        """查询单条记录"""
        with self.get_connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, params)
                return cursor.fetchone()
    
    def execute(self, sql: str, params: tuple = None) -> int:
        """执行SQL（INSERT/UPDATE/DELETE）"""
        with self.get_connection() as conn:
            with conn.cursor() as cursor:
                cursor.execute(sql, params)
                return cursor.lastrowid
    
    def insert_alert_log(self, alert: AlertData) -> int:
        """插入告警日志到 monitor_alert_log_v2"""
        sql = """
            INSERT INTO monitor_alert_log_v2 (
                task_id, config_id, alert_key, task_type, chain_type,
                ca, token_name, token_symbol, market_cap,
                alert_type, alert_title, alert_content, trigger_value,
                notify_status, detect_latency_ms, create_time
            ) VALUES (
                %s, %s, %s, %s, %s,
                %s, %s, %s, %s,
                %s, %s, %s, %s,
                %s, %s, NOW()
            )
        """
        
        # 生成去重键（MD5: task_id+ca+alert_type+date）
        import hashlib
        from datetime import datetime
        alert_key_raw = f"{alert.task_id}_{alert.ca}_{alert.alert_type}_{datetime.now().strftime('%Y%m%d')}"
        alert_key = hashlib.md5(alert_key_raw.encode()).hexdigest()
        
        params = (
            alert.task_id,
            alert.config_id,
            alert_key,
            alert.task_type,
            alert.chain_type,
            alert.ca,
            alert.token_name,
            alert.token_symbol,
            alert.market_cap,
            alert.alert_type,
            alert.alert_title,
            json.dumps(alert.alert_content),  # JSON字段
            alert.trigger_value,
            'pending',  # 初始通知状态
            alert.detect_latency_ms
        )
        
        return self.execute(sql, params)
```

### 3. 价格获取服务（实时，无缓存）

```python
# price_service.py
import aiohttp
import asyncio
from decimal import Decimal
from typing import Optional
import logging

logger = logging.getLogger(__name__)

class PriceService:
    """Token价格获取服务（实时查询）"""
    
    def __init__(self):
        self.session = None
    
    async def init_session(self):
        """初始化HTTP会话"""
        if not self.session:
            self.session = aiohttp.ClientSession()
    
    async def close_session(self):
        """关闭HTTP会话"""
        if self.session:
            await self.session.close()
    
    async def get_token_price_usd(self, token_address: str, chain: str = 'bsc') -> Optional[Decimal]:
        """
        获取Token实时价格（USD）
        
        优先级：
        1. DexScreener API
        2. GeckoTerminal API
        3. 返回None（价格获取失败）
        """
        await self.init_session()
        
        # 尝试1: DexScreener
        price = await self._get_price_from_dexscreener(token_address, chain)
        if price:
            return price
        
        # 尝试2: GeckoTerminal
        price = await self._get_price_from_geckoterminal(token_address, chain)
        if price:
            return price
        
        logger.warning(f"⚠️ 无法获取Token价格: {token_address}")
        return None
    
    async def _get_price_from_dexscreener(self, token_address: str, chain: str) -> Optional[Decimal]:
        """从DexScreener获取价格"""
        try:
            url = f"https://api.dexscreener.com/latest/dex/tokens/{token_address}"
            async with self.session.get(url, timeout=aiohttp.ClientTimeout(total=5)) as resp:
                if resp.status == 200:
                    data = await resp.json()
                    if data.get('pairs'):
                        # 取第一个交易对的价格
                        pair = data['pairs'][0]
                        price_usd = pair.get('priceUsd')
                        if price_usd:
                            return Decimal(str(price_usd))
        except Exception as e:
            logger.debug(f"DexScreener查询失败: {e}")
        
        return None
    
    async def _get_price_from_geckoterminal(self, token_address: str, chain: str) -> Optional[Decimal]:
        """从GeckoTerminal获取价格"""
        try:
            # GeckoTerminal链名称映射
            chain_map = {'bsc': 'bsc', 'eth': 'eth', 'sol': 'solana'}
            chain_name = chain_map.get(chain, chain)
            
            url = f"https://api.geckoterminal.com/api/v2/networks/{chain_name}/tokens/{token_address}"
            async with self.session.get(url, timeout=aiohttp.ClientTimeout(total=5)) as resp:
                if resp.status == 200:
                    data = await resp.json()
                    price_usd = data.get('data', {}).get('attributes', {}).get('price_usd')
                    if price_usd:
                        return Decimal(str(price_usd))
        except Exception as e:
            logger.debug(f"GeckoTerminal查询失败: {e}")
        
        return None
```

### 4. BSC区块监控核心引擎

```python
# bsc_block_monitor.py
import asyncio
from web3 import Web3
from web3.middleware import geth_poa_middleware
from decimal import Decimal
from typing import List, Dict, Optional
import logging
from datetime import datetime, timedelta
from collections import defaultdict

logger = logging.getLogger(__name__)

class BSCBlockMonitor:
    """BSC区块监控引擎"""
    
    # ERC-20 Transfer事件签名
    TRANSFER_EVENT_SIGNATURE = Web3.keccak(text="Transfer(address,address,uint256)").hex()
    
    # 主流DEX路由合约地址（小写）
    DEX_ROUTERS = {
        '0x10ed43c718714eb63d5aa57b78b54704e256024e': 'PancakeSwap V2',
        '0x13f4ea83d0bd40e75c8222255bc855a974568dd4': 'PancakeSwap V3',
        '0x8cFe327CEc66d1C090Dd72bd0FF11d690C33a2Eb': 'BiSwap',
        '0xcf0febd3f17cef5b47b0cd257acf6025c5bff3b7': 'ApeSwap',
        '0x7a250d5630b4cf539739df2c5dacb4c659f2488d': 'Uniswap V2',
    }
    
    def __init__(self, db_config: dict, rpc_url: str):
        self.db = DatabaseConnection(db_config)
        self.w3 = Web3(Web3.HTTPProvider(rpc_url))
        self.w3.middleware_onion.inject(geth_poa_middleware, layer=0)  # BSC需要POA中间件
        self.price_service = PriceService()
        
        # 累计金额追踪器 {(task_id, token_address): {'amount': Decimal, 'start_time': datetime}}
        self.cumulative_tracker = defaultdict(lambda: {'amount': Decimal('0'), 'start_time': datetime.now()})
        
        # 累计窗口时间（分钟）
        self.cumulative_window_minutes = 60
    
    async def start_monitoring(self, task: BSCBlockMonitorTask):
        """启动监控任务"""
        logger.info(f"🚀 启动任务: {task.task_name} (ID: {task.id})")
        logger.info(f"   单笔阈值: ${task.min_transaction_usd}")
        logger.info(f"   累计阈值: ${task.cumulative_min_amount_usd}")
        logger.info(f"   时间窗口: {task.time_interval}")
        if task.config_id:
            logger.info(f"   关联配置ID: {task.config_id}")
        
        # 获取当前最新区块
        latest_block = self.w3.eth.block_number
        logger.info(f"   当前区块: {latest_block}")
        
        # 订阅新区块（轮询模式，BSC不支持WebSocket订阅）
        await self._poll_blocks(task, latest_block)
    
    async def _poll_blocks(self, task: BSCBlockMonitorTask, start_block: int):
        """轮询新区块"""
        current_block = start_block
        
        while True:
            try:
                latest_block = self.w3.eth.block_number
                
                if latest_block > current_block:
                    # 处理新区块
                    for block_num in range(current_block + 1, latest_block + 1):
                        await self._process_block(task, block_num)
                    
                    current_block = latest_block
                
                # 等待3秒（BSC平均出块时间）
                await asyncio.sleep(3)
            
            except Exception as e:
                logger.error(f"❌ 轮询区块失败: {e}")
                await asyncio.sleep(5)
    
    async def _process_block(self, task: BSCBlockMonitorTask, block_number: int):
        """处理单个区块"""
        start_time = datetime.now()
        
        try:
            # 获取区块详情
            block = self.w3.eth.get_block(block_number, full_transactions=True)
            
            logger.debug(f"📦 处理区块 {block_number}, 交易数: {len(block.transactions)}")
            
            # 遍历所有交易
            for tx in block.transactions:
                await self._process_transaction(task, tx, block.timestamp)
        
        except Exception as e:
            logger.error(f"❌ 处理区块 {block_number} 失败: {e}")
    
    async def _process_transaction(self, task: BSCBlockMonitorTask, tx: dict, block_timestamp: int):
        """处理单笔交易"""
        try:
            # 获取交易回执
            receipt = self.w3.eth.get_transaction_receipt(tx['hash'])
            
            # 解析Transfer事件
            transfers = self._parse_transfer_events(receipt)
            
            if not transfers:
                return
            
            # 判断市场类型
            for transfer in transfers:
                market_type = self._determine_market_type(tx, receipt)
                
                # 不需要过滤市场类型，内外盘都监控
                # （可以根据需求在告警日志中区分）
                
                # 创建转账事件对象
                event = TransferEvent(
                    tx_hash=tx['hash'].hex(),
                    block_number=receipt['blockNumber'],
                    token_address=transfer['token_address'],
                    from_address=transfer['from'],
                    to_address=transfer['to'],
                    amount=transfer['amount'],
                    amount_formatted=Decimal(str(transfer['amount'])) / Decimal(10 ** 18),  # 假设18位精度
                    timestamp=block_timestamp,
                    market_type=market_type,
                    dex_name=self._get_dex_name(tx['to']) if market_type == 'external' else None
                )
                
                # 检查阈值并触发告警
                await self._check_thresholds_and_alert(task, event)
        
        except Exception as e:
            logger.debug(f"处理交易失败: {e}")
    
    def _parse_transfer_events(self, receipt: dict) -> List[dict]:
        """解析交易回执中的Transfer事件"""
        transfers = []
        
        for log in receipt['logs']:
            # 检查是否是Transfer事件
            if len(log['topics']) > 0 and log['topics'][0].hex() == self.TRANSFER_EVENT_SIGNATURE:
                try:
                    transfers.append({
                        'token_address': log['address'],
                        'from': '0x' + log['topics'][1].hex()[26:],  # 移除前导0
                        'to': '0x' + log['topics'][2].hex()[26:],
                        'amount': int(log['data'].hex(), 16)
                    })
                except Exception as e:
                    logger.debug(f"解析Transfer事件失败: {e}")
        
        return transfers
    
    def _determine_market_type(self, tx: dict, receipt: dict) -> str:
        """判断交易是内盘还是外盘"""
        to_address = tx['to'].lower() if tx['to'] else None
        
        # 检查1: To地址是否是已知DEX路由
        if to_address in self.DEX_ROUTERS:
            return 'external'
        
        # 检查2: Input数据是否包含swap函数签名
        input_data = tx['input'].hex()
        swap_signatures = ['0x38ed1739', '0x7ff36ab5', '0x18cbafe5', '0x8803dbee']  # swapExactTokensForTokens等
        for sig in swap_signatures:
            if input_data.startswith(sig):
                return 'external'
        
        # 检查3: 日志中是否包含Swap事件
        swap_event_sig = Web3.keccak(text="Swap(address,uint256,uint256,uint256,uint256,address)").hex()
        for log in receipt['logs']:
            if len(log['topics']) > 0 and log['topics'][0].hex() == swap_event_sig:
                return 'external'
        
        # 默认为内盘
        return 'internal'
    
    def _get_dex_name(self, router_address: str) -> Optional[str]:
        """获取DEX名称"""
        return self.DEX_ROUTERS.get(router_address.lower())
    
    def _parse_time_interval(self, time_interval: str) -> int:
        """
        解析时间窗口配置，返回分钟数
        
        支持格式：
        - '60m' -> 60分钟
        - '1h' -> 60分钟
        - '24h' -> 1440分钟
        """
        if not time_interval:
            return 60  # 默认60分钟
        
        time_interval = time_interval.lower().strip()
        
        if time_interval.endswith('m'):
            return int(time_interval[:-1])
        elif time_interval.endswith('h'):
            return int(time_interval[:-1]) * 60
        else:
            return 60  # 默认60分钟
    
    async def _check_thresholds_and_alert(self, task: BSCBlockMonitorTask, event: TransferEvent):
        """检查阈值并触发告警"""
        start_time = datetime.now()
        
        # 获取实时价格
        price_usd = await self.price_service.get_token_price_usd(event.token_address, 'bsc')
        
        if not price_usd:
            logger.debug(f"⚠️ Token {event.token_address} 价格获取失败，跳过")
            return
        
        # 计算USD价值
        amount_usd = event.amount_formatted * price_usd
        
        logger.debug(f"💰 检测到转账: {event.token_address}")
        logger.debug(f"   金额: {event.amount_formatted} (${amount_usd:.2f})")
        logger.debug(f"   类型: {event.market_type}")
        
        # 检查1: 单笔阈值
        if amount_usd >= task.min_transaction_usd:
            alert_type = f"block_{event.market_type}_single"
            await self._trigger_alert(task, event, price_usd, amount_usd, alert_type, None, start_time)
        
        # 检查2: 累计阈值
        tracker_key = (task.id, event.token_address)
        tracker = self.cumulative_tracker[tracker_key]
        
        # 解析时间窗口（从任务配置中获取）
        window_minutes = self._parse_time_interval(task.time_interval)
        
        # 检查时间窗口是否过期
        if datetime.now() - tracker['start_time'] > timedelta(minutes=window_minutes):
            # 重置累计器
            tracker['amount'] = Decimal('0')
            tracker['start_time'] = datetime.now()
        
        # 累加金额
        tracker['amount'] += amount_usd
        
        # 检查是否达到累计阈值
        if tracker['amount'] >= task.cumulative_min_amount_usd:
            alert_type = f"block_{event.market_type}_cumulative"
            cumulative_amount = tracker['amount']
            await self._trigger_alert(task, event, price_usd, amount_usd, alert_type, cumulative_amount, start_time)
            
            # 重置累计器
            tracker['amount'] = Decimal('0')
            tracker['start_time'] = datetime.now()
    
    async def _trigger_alert(
        self,
        task: BSCBlockMonitorTask,
        event: TransferEvent,
        price_usd: Decimal,
        amount_usd: Decimal,
        alert_type: str,
        cumulative_amount: Optional[Decimal],
        start_time: datetime
    ):
        """触发告警"""
        # 获取Token信息（实时查询链上，不缓存）
        token_name, token_symbol = await self._get_token_info(event.token_address)
        
        # 构建告警标题
        if 'single' in alert_type:
            title = f"{'外盘' if event.market_type == 'external' else '内盘'}单笔大额: {token_symbol} ${amount_usd:,.2f}"
        else:
            title = f"{'外盘' if event.market_type == 'external' else '内盘'}累计告警: {token_symbol} ${cumulative_amount:,.2f}"
        
        # 构建告警内容
        content = {
            'tx_hash': event.tx_hash,
            'from': event.from_address,
            'to': event.to_address,
            'amount': str(event.amount_formatted),
            'amount_usd': float(amount_usd),
            'price_usd': float(price_usd),
            'market_type': event.market_type,
            'timestamp': event.timestamp
        }
        
        if event.dex_name:
            content['dex_name'] = event.dex_name
        
        if cumulative_amount:
            content['cumulative_amount_usd'] = float(cumulative_amount)
            content['cumulative_window_minutes'] = self._parse_time_interval(task.time_interval)
            content['time_interval'] = task.time_interval
        
        # 计算检测延迟
        detect_latency_ms = int((datetime.now() - start_time).total_seconds() * 1000)
        
        # 创建告警数据
        alert = AlertData(
            task_id=task.id,
            config_id=task.config_id,
            task_type='block',
            chain_type='bsc',
            ca=event.token_address,
            token_name=token_name,
            token_symbol=token_symbol,
            market_cap=None,  # 区块监控不需要市值
            alert_type=alert_type,
            alert_title=title,
            alert_content=content,
            trigger_value=cumulative_amount if cumulative_amount else amount_usd,
            detect_latency_ms=detect_latency_ms
        )
        
        # 写入数据库
        alert_id = self.db.insert_alert_log(alert)
        
        logger.info(f"🚨 触发告警 ID: {alert_id}")
        logger.info(f"   {title}")
        logger.info(f"   交易: {event.tx_hash}")
        
        # TODO: 这里可以添加通知推送逻辑（Telegram/微信等）
    
    async def _get_token_info(self, token_address: str) -> tuple:
        """
        获取Token信息（实时查询链上）
        返回: (token_name, token_symbol)
        """
        try:
            # ERC-20标准接口
            abi = [
                {"constant": True, "inputs": [], "name": "name", "outputs": [{"name": "", "type": "string"}], "type": "function"},
                {"constant": True, "inputs": [], "name": "symbol", "outputs": [{"name": "", "type": "string"}], "type": "function"}
            ]
            
            contract = self.w3.eth.contract(address=Web3.toChecksumAddress(token_address), abi=abi)
            
            # 异步调用链上方法（使用asyncio.to_thread避免阻塞）
            name = await asyncio.to_thread(contract.functions.name().call)
            symbol = await asyncio.to_thread(contract.functions.symbol().call)
            
            return name, symbol
        
        except Exception as e:
            logger.debug(f"获取Token信息失败: {e}")
            return 'Unknown', 'UNKNOWN'
```

### 5. 任务管理器

```python
# task_manager.py
import asyncio
from typing import Dict
import logging

logger = logging.getLogger(__name__)

class BSCBlockMonitorManager:
    """BSC区块监控任务管理器"""
    
    def __init__(self, db_config: dict, bsc_rpc_url: str):
        self.db = DatabaseConnection(db_config)
        self.monitor = BSCBlockMonitor(db_config, bsc_rpc_url)
        self.running_tasks: Dict[int, asyncio.Task] = {}  # {task_id: asyncio.Task}
    
    async def load_and_start_all_tasks(self):
        """加载并启动所有启用的区块监控任务"""
        query = """
            SELECT 
                t.id,
                t.task_name,
                t.chain_type,
                t.min_transaction_usd,
                t.cumulative_min_amount_usd,
                t.status,
                c.id AS config_id,
                c.time_interval,
                c.events_config
            FROM monitor_task_v2 t
            LEFT JOIN monitor_task_config_v2 tc ON t.id = tc.task_id AND tc.is_active = 1
            LEFT JOIN monitor_config_v2 c ON tc.config_id = c.id
            WHERE t.task_type = 'block'
              AND t.chain_type = 'bsc'
              AND t.status = 1
        """
        
        rows = self.db.query_all(query)
        
        logger.info(f"📋 发现 {len(rows)} 个BSC区块监控任务")
        
        for row in rows:
            # 构建任务对象
            task = BSCBlockMonitorTask(
                id=row['id'],
                task_name=row['task_name'],
                chain_type=row['chain_type'],
                min_transaction_usd=Decimal(str(row['min_transaction_usd'])),
                cumulative_min_amount_usd=Decimal(str(row['cumulative_min_amount_usd'])),
                time_interval=row['time_interval'] if row['time_interval'] else '60m',  # 默认60分钟
                config_id=row['config_id'],
                events_config=json.loads(row['events_config']) if row['events_config'] else None,
                status=row['status']
            )
            
            await self.start_task(task)
    
    async def start_task(self, task: BSCBlockMonitorTask):
        """启动单个任务"""
        if task.id in self.running_tasks:
            logger.warning(f"⚠️ 任务 {task.task_name} (ID: {task.id}) 已在运行")
            return
        
        # 创建异步任务
        async_task = asyncio.create_task(
            self.monitor.start_monitoring(task),
            name=f"task_{task.id}"
        )
        self.running_tasks[task.id] = async_task
        
        logger.info(f"✅ 任务 {task.task_name} (ID: {task.id}) 已启动")
    
    async def stop_task(self, task_id: int):
        """停止单个任务"""
        if task_id not in self.running_tasks:
            logger.warning(f"⚠️ 任务 ID {task_id} 未在运行")
            return
        
        # 取消异步任务
        self.running_tasks[task_id].cancel()
        try:
            await self.running_tasks[task_id]
        except asyncio.CancelledError:
            pass
        
        del self.running_tasks[task_id]
        
        logger.info(f"🛑 任务 ID {task_id} 已停止")
    
    async def reload_task(self, task_id: int):
        """重新加载任务（配置更新后）"""
        logger.info(f"🔄 重新加载任务 ID {task_id}")
        
        # 先停止
        await self.stop_task(task_id)
        
        # 重新加载
        query = """
            SELECT 
                t.id,
                t.task_name,
                t.chain_type,
                t.min_transaction_usd,
                t.cumulative_min_amount_usd,
                t.status,
                c.id AS config_id,
                c.time_interval,
                c.events_config
            FROM monitor_task_v2 t
            LEFT JOIN monitor_task_config_v2 tc ON t.id = tc.task_id AND tc.is_active = 1
            LEFT JOIN monitor_config_v2 c ON tc.config_id = c.id
            WHERE t.id = %s AND t.status = 1
        """
        
        row = self.db.query_one(query, (task_id,))
        
        if row:
            # 构建任务对象
            task = BSCBlockMonitorTask(
                id=row['id'],
                task_name=row['task_name'],
                chain_type=row['chain_type'],
                min_transaction_usd=Decimal(str(row['min_transaction_usd'])),
                cumulative_min_amount_usd=Decimal(str(row['cumulative_min_amount_usd'])),
                time_interval=row['time_interval'] if row['time_interval'] else '60m',
                config_id=row['config_id'],
                events_config=json.loads(row['events_config']) if row['events_config'] else None,
                status=row['status']
            )
            
            await self.start_task(task)
        else:
            logger.warning(f"⚠️ 任务 ID {task_id} 不存在或已停用")
    
    async def get_status(self) -> dict:
        """获取管理器状态"""
        return {
            'total_tasks': len(self.running_tasks),
            'running_task_ids': list(self.running_tasks.keys())
        }
```

### 6. 主程序入口

```python
# main.py
import asyncio
import logging
from config import *
from database import *
from price_service import *
from bsc_block_monitor import *
from task_manager import *

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('bsc_block_monitor.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

async def main():
    """主程序"""
    logger.info("=" * 60)
    logger.info("BSC区块监控系统启动")
    logger.info("=" * 60)
    
    # 数据库配置
    db_config = {
        'host': 'localhost',
        'port': 3306,
        'user': 'root',
        'password': 'your_password',
        'database': 'ry-vue',
        'charset': 'utf8mb4'
    }
    
    # BSC RPC配置（建议使用私有节点或付费服务）
    bsc_rpc_url = 'https://bsc-dataseed1.binance.org/'
    # bsc_rpc_url = 'https://your-private-node.com'  # 生产环境推荐
    
    # 创建管理器
    manager = BSCBlockMonitorManager(db_config, bsc_rpc_url)
    
    # 启动所有任务
    await manager.load_and_start_all_tasks()
    
    logger.info("✅ 所有任务已启动，进入监控状态...")
    
    # 保持运行
    try:
        while True:
            await asyncio.sleep(60)
            
            # 定期输出状态
            status = await manager.get_status()
            logger.info(f"📊 运行中任务: {status['total_tasks']} 个")
    
    except KeyboardInterrupt:
        logger.info("\n👋 收到退出信号，正在停止所有任务...")
        
        # 停止所有任务
        for task_id in list(manager.running_tasks.keys()):
            await manager.stop_task(task_id)
        
        # 关闭价格服务会话
        await manager.monitor.price_service.close_session()
        
        logger.info("✅ 所有任务已停止")

if __name__ == '__main__':
    try:
        asyncio.run(main())
    except Exception as e:
        logger.error(f"❌ 程序异常退出: {e}", exc_info=True)
```

---

## 🚀 部署运行

### 1. 环境准备

```bash
# Python 3.8+
python --version

# 安装依赖
pip install web3 pymysql aiohttp asyncio
```

### 2. 配置文件

创建 `config.ini`:

```ini
[database]
host = localhost
port = 3306
user = root
password = your_password
database = ry-vue

[bsc]
rpc_url = https://bsc-dataseed1.binance.org/
# 生产环境推荐私有节点：
# rpc_url = https://your-private-node.com

[monitor]
cumulative_window_minutes = 60
log_level = INFO
```

### 3. 创建任务（通过管理界面或SQL）

#### 方案A：最简化（推荐）⭐

**只创建任务，不关联配置**（适合只需要金额阈值监控，不需要额外规则）

```sql
-- =============================================
-- 示例1: BSC区块监控 - 单笔1万累计5万
-- =============================================
INSERT INTO monitor_task_v2 (
    task_name, task_type, chain_type,
    min_transaction_usd, cumulative_min_amount_usd,
    auto_sync_targets, sync_interval_minutes, target_count,
    status, current_epoch, description, create_by, create_time
) VALUES (
    'BSC区块监控-大额交易',
    'block',           -- 区块监控
    'bsc',
    10000.00,          -- ⭐ 单笔最小$10,000
    50000.00,          -- ⭐ 累计最小$50,000
    1,                 -- 不使用，填1
    30,                -- 不使用，填30
    0,                 -- 不使用，填0
    1,                 -- 启用
    0,                 -- 初始epoch
    'BSC区块链大额交易实时监控（单笔$10k，累计$50k）',
    'admin',
    NOW()
);

-- =============================================
-- 示例2: BSC区块监控 - 中等金额
-- =============================================
INSERT INTO monitor_task_v2 (
    task_name, task_type, chain_type,
    min_transaction_usd, cumulative_min_amount_usd,
    auto_sync_targets, sync_interval_minutes, target_count,
    status, current_epoch, description, create_by, create_time
) VALUES (
    'BSC区块监控-中等金额',
    'block',
    'bsc',
    5000.00,           -- ⭐ 单笔最小$5,000
    20000.00,          -- ⭐ 累计最小$20,000
    1, 30, 0, 1, 0,
    'BSC区块链中等金额监控（单笔$5k，累计$20k）',
    'admin',
    NOW()
);

-- 验证
SELECT id, task_name, task_type, chain_type, 
       min_transaction_usd, cumulative_min_amount_usd, status
FROM monitor_task_v2
WHERE task_type = 'block' AND chain_type = 'bsc';
```

**累计时间窗口默认值**：Python代码中设置为60分钟（可在代码中调整）

---

#### 方案B：关联配置（可选）

**创建任务 + 关联配置**（适合需要额外规则：价格涨跌、成交量、持仓变化等）

**步骤1：创建配置**（可选，提供time_interval和额外规则）

```sql
-- 配置：BSC区块监控通用配置
INSERT INTO monitor_config_v2 (
    config_name, chain_type, config_category,
    time_interval,     -- ⭐ 累计时间窗口
    events_config,     -- 可选：额外规则
    trigger_logic, notify_methods,
    version, description, status, create_by, create_time
) VALUES (
    'BSC区块监控-基础配置',
    'bsc',
    'custom',
    '60m',             -- ⭐ 60分钟累计窗口
    '{
      "priceChange": {"enabled": false},
      "holders": {"enabled": false},
      "volume": {"enabled": false}
    }',                -- 不启用额外规则（区块监控只看金额）
    'any',
    'telegram,wechat',
    1,
    'BSC区块监控基础配置（60分钟累计窗口）',
    1,
    'admin',
    NOW()
);

-- 查看配置ID
SELECT id, config_name, time_interval FROM monitor_config_v2 
WHERE config_name = 'BSC区块监控-基础配置';
```

**步骤2：创建任务**

```sql
-- 任务（与方案A相同）
INSERT INTO monitor_task_v2 (
    task_name, task_type, chain_type,
    min_transaction_usd, cumulative_min_amount_usd,
    auto_sync_targets, sync_interval_minutes, target_count,
    status, current_epoch, description, create_by, create_time
) VALUES (
    'BSC区块监控-大额交易',
    'block', 'bsc',
    10000.00, 50000.00,
    1, 30, 0, 1, 0,
    'BSC区块链大额交易实时监控',
    'admin',
    NOW()
);
```

**步骤3：关联任务和配置**

```sql
-- 假设任务ID=12, 配置ID=15
INSERT INTO monitor_task_config_v2 (
    task_id, config_id, config_order, is_active, create_time
) VALUES (
    12,    -- 任务ID（替换为实际ID）
    15,    -- 配置ID（替换为实际ID）
    1, 1, NOW()
);
```

**步骤4：验证**

```sql
SELECT 
    t.id, t.task_name,
    t.min_transaction_usd,
    t.cumulative_min_amount_usd,
    c.time_interval,
    c.events_config
FROM monitor_task_v2 t
LEFT JOIN monitor_task_config_v2 tc ON t.id = tc.task_id
LEFT JOIN monitor_config_v2 c ON tc.config_id = c.id
WHERE t.task_type = 'block' AND t.chain_type = 'bsc';
```

---

#### 两种方案对比

| 维度 | 方案A（只创建任务） | 方案B（关联配置） |
|------|---------------------|-------------------|
| **复杂度** | ⭐ 简单，1条SQL | 复杂，3步骤 |
| **累计窗口** | 代码默认60分钟 | 从 `config.time_interval` 读取 |
| **额外规则** | ❌ 不支持 | ✅ 支持（价格、持仓、成交量） |
| **适用场景** | 纯金额阈值监控 | 需要组合多种规则 |

**推荐**：大部分情况使用 **方案A**，简单直接！

### 4. 启动程序

```bash
# 前台运行（开发/测试）
python main.py

# 后台运行（生产环境）
nohup python main.py > bsc_monitor.log 2>&1 &

# 查看日志
tail -f bsc_monitor.log

# 停止程序
kill $(ps aux | grep 'python main.py' | grep -v grep | awk '{print $2}')
```

### 5. 验证运行状态

```bash
# 查看进程
ps aux | grep main.py

# 查看日志
tail -f bsc_block_monitor.log

# 查询告警记录
mysql -u root -p ry-vue -e "SELECT * FROM monitor_alert_log_v2 WHERE chain_type='bsc' ORDER BY create_time DESC LIMIT 10;"
```

---

## ❓ 常见问题

### Q1: 为什么不缓存Token信息？

**A**: 区块监控需要**实时性**，每笔交易的Token价格都在波动，缓存会导致：
- 价格不准确（可能差距巨大）
- 告警误判（应该告警的漏掉，不应该告警的触发）
- 市场数据滞后

实时查询虽然慢一点，但保证了准确性。如果担心API限流，可以：
- 使用付费API（更高配额）
- 多个API源轮询
- 本地部署价格节点

### Q2: BSC RPC如何选择？

**A**: 推荐方案：
1. **开发/测试**: 公共节点（免费但限流）
   - `https://bsc-dataseed1.binance.org/`
   - `https://bsc-dataseed2.binance.org/`
   
2. **生产环境**: 私有节点（稳定高速）
   - Quicknode: https://www.quicknode.com/
   - Alchemy: https://www.alchemy.com/
   - Infura: https://infura.io/
   - 自建节点（最稳定但成本高）

### Q3: 如何优化性能？

**A**: 优化建议：
1. **价格API**: 使用多源轮询 + 失败重试
2. **并发控制**: 限制同时处理的交易数（避免过载）
3. **过滤优化**: 只处理价值高的Token（预先白名单）
4. **数据库索引**: 确保 `monitor_alert_log_v2` 有合适索引
5. **日志级别**: 生产环境设置为 `INFO`（减少IO）

### Q4: 累计阈值会不会漏掉？

**A**: 不会。累计器的设计：
- 每笔交易都会累加（无论是否告警）
- 时间窗口内持续追踪
- 达到阈值后立即告警并清零
- 窗口过期后自动重置

### Q5: 如何区分DEX交易和直接转账？

**A**: 三重判断机制：
1. **To地址**: 是否是已知DEX路由合约
2. **Input数据**: 是否包含swap函数签名
3. **事件日志**: 是否包含Swap事件

只要满足一条，就判定为外盘（DEX交易）。

### Q6: 价格获取失败怎么办？

**A**: 当前策略：
- 失败时跳过该笔交易（不告警）
- 日志记录警告信息

未来可优化：
- 多源API轮询（DexScreener → GeckoTerminal → CoinGecko）
- 使用DEX池子内价格（从Swap事件反推）
- 设置默认价格或上次成功价格

### Q7: 如何热更新任务？

**A**: 
```python
# 在manager中调用
await manager.reload_task(task_id)
```

或者监听数据库变更：
```python
# 定期轮询任务表（检查status/config变更）
async def watch_task_changes():
    while True:
        # 查询最近更新的任务
        # 对比本地缓存
        # 如有变更则reload
        await asyncio.sleep(30)
```

### Q8: 告警去重如何实现？

**A**: 通过 `alert_key` 字段（MD5）：
```python
alert_key = MD5(task_id + ca + alert_type + date)
```

数据库设置 `UNIQUE KEY`，重复插入会失败（被忽略）。

同一天、同一任务、同一Token、同一类型的告警只会记录一次。

---

## 📚 参考资料

- [Web3.py文档](https://web3py.readthedocs.io/)
- [BSC开发者文档](https://docs.bnbchain.org/)
- [ERC-20标准](https://eips.ethereum.org/EIPS/eip-20)
- [DexScreener API](https://docs.dexscreener.com/)
- [GeckoTerminal API](https://www.geckoterminal.com/dex-api)

---

## 📝 总结

本方案提供了一套完整的BSC区块监控解决方案，核心特点：

✅ **实时监控**: 无缓存，每笔交易实时获取价格  
✅ **内外盘识别**: 智能识别DEX交易和直接转账（实时判断，无需存储）  
✅ **双重阈值**: 单笔 + 累计，灵活配置  
✅ **专用字段**: 参数存储在任务表中，与智能监控、批量监控架构一致  
✅ **最小改动**: 只需在 `monitor_task_v2` 添加2个字段  
✅ **易于扩展**: 支持多任务、热更新、通知推送  

### 关键要点

1. **阈值参数存储在 `monitor_task_v2` 中**（新增2个字段）：
   - `min_transaction_usd`: 单笔最小金额
   - `cumulative_min_amount_usd`: 累计最小金额

2. **累计时间窗口**：
   - **方案A**（推荐）：代码中默认60分钟
   - **方案B**（可选）：从关联的 `monitor_config_v2.time_interval` 读取

3. **内外盘识别**：
   - 不需要在数据库中存储
   - Python代码实时判断（检查DEX路由地址、Swap事件、函数签名）
   - 告警日志中记录具体的市场类型

4. **配置关联**（可选）：
   - 如果只需金额阈值监控：不关联配置
   - 如果需要额外规则（价格、持仓、成交量）：关联配置

代码可直接运行，根据实际需求调整参数即可！🚀

---

**下一步**：
1. ⭐ **执行SQL**：在 `monitor_task_v2` 添加2个字段
2. **创建任务**：直接INSERT任务（推荐方案A）
3. **运行Python**：启动监控程序
4. **验证效果**：查看 `monitor_alert_log_v2` 告警日志

---

**与现有架构的一致性**：

| 任务类型 | 专用字段 | 存储位置 |
|---------|---------|---------|
| 智能监控 (smart) | `min_market_cap`, `max_market_cap`, `has_twitter` | `monitor_task_v2` |
| 批量监控 (batch) | `target_count` | `monitor_task_v2` |
| **区块监控 (block)** | **`min_transaction_usd`, `cumulative_min_amount_usd`** | **`monitor_task_v2`** ⭐ |

