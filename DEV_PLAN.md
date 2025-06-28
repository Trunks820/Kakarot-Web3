# 加密货币监控系统开发计划

## 项目概述
这是一个基于若依框架开发的功能完整的加密货币监控系统，集成了微信和Telegram机器人，提供实时价格监控、查询统计、排行榜等功能。系统采用Java + Python双端架构，确保高性能和灵活性。

## 开发阶段与时间规划

### 阶段一：信号预警与代币监控（2周）- 🎯 **最高优先级**

#### 第1周：信号预警页面开发
- **OKX信号展示页面**
  - 集成已有OKX聪明钱信号API
  - 创建信号展示后台页面
  - 信号历史记录查询
  - 信号有效性统计分析
  
- **预警阈值管理系统**
  - 创建预警配置管理页面
  - 热度预警阈值设置（多少个钱包算热度）
  - 大额交易阈值配置（金额标准）
  - 时间窗口配置（异常活跃检测）
  - 阈值历史变更记录

#### 第2周：代币监控系统完善
- **被监控代币列表页面**
  - 展示所有被监控的代币
  - 监控配置管理（价格阈值、涨跌幅）
  - 监控状态控制（启用/禁用）
  - 监控效果统计分析
  
- **监控功能优化**
  - 完善crypto_monitor_config表功能
  - 添加更多监控条件（成交量、流动性等）
  - 批量监控设置功能
  - 监控历史记录查询

### 阶段二：钱包预警系统（2周）- 🎯 **核心差异化功能**

#### 第3周：交易预警逻辑开发
- **实时交易检测**
  - 基于crypto_wallet_transactions表的实时监控
  - 大额交易预警（基于配置阈值）
  - 多钱包同币种交易预警
  - 新币种首次交易发现预警
  
- **预警数据管理**
  - 创建crypto_alert_record表
  - 预警记录存储与查询
  - 预警去重机制

#### 第4周：热度分析系统
- **热度计算算法**
  - 代币关注度实时计算
  - 聪明钱共识分析
  - 异常活跃度检测
  - 热度趋势分析
  
- **预警触发机制**
  - 热度阈值触发逻辑
  - 预警级别分类（低、中、高）
  - 预警冷却机制（避免重复推送）

### 阶段三：预警推送系统（2周）- 🎯 **用户体验优化**

#### 第5周：TG推送系统（第一优先级）
- **推送功能开发**
  - 预警消息格式化与模板
  - TG机器人推送集成
  - 推送频率控制
  - 用户订阅管理
  
- **推送优化**
  - 消息重要性分级
  - 推送时间智能控制
  - 推送效果统计

#### 第6周：首页预警面板（第二优先级）
- **实时预警展示**
  - 首页预警面板设计
  - 实时预警数据展示
  - 预警历史查看功能
  - 预警统计图表
  
- **微信机器人预警集成（条件允许时）**
  - 预警消息推送
  - 用户交互优化

### 阶段四：数据可视化与系统优化（1周）

#### 第7周：完善与优化
- **数据可视化完善**
  - 监控数据图表优化
  - 预警效果分析图表
  - 钱包行为分析可视化
  
- **系统性能优化**
  - 数据库查询优化
  - 实时数据处理优化
  - 缓存策略完善
  - 系统监控与告警

## 功能优先级分类

### 最高优先级（阶段一：立即开发）
1. **OKX信号展示页面** - 集成已有API，快速见效
2. **预警阈值管理系统** - 核心配置功能
3. **被监控代币列表页面** - 完善监控体系
4. **代币监控功能优化** - 提升监控效果

### 高优先级（阶段二：核心差异化）
1. **实时交易预警逻辑** - 基于钱包监控的独特功能
2. **热度分析算法** - 聪明钱共识分析
3. **预警数据管理** - 预警记录与去重
4. **预警触发机制** - 智能预警系统

### 中优先级（阶段三：用户体验）
1. **TG推送系统** - 预警消息及时推送
2. **首页预警面板** - 直观的预警展示
3. **推送优化机制** - 智能推送控制
4. **微信机器人集成** - 多渠道预警推送

### 低优先级（阶段四：完善优化）
1. **数据可视化完善** - 监控数据图表
2. **系统性能优化** - 数据库和缓存优化  
3. **预警效果分析** - 预警准确性统计
4. **系统监控告警** - 系统稳定性保障


## 数据库设计初步规划

### 核心表结构
1. **crypto_coin**：CA基础信息表
   - coin_id: 主键
   - address: 合约地址
   - symbol: 币种符号
   - name: 币种名称
   - logo_url: 币种图标(如果有)
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间
   - chain_type: 链类型
   - description: 描述

2. **crypto_ca_record**: CA查询记录表
   - id: 主键
   - ca_id: 关联的CA ID
   - first_query_user_id: 首次查询用户ID
   - first_query_user_name: 首次查询用户名称
   - first_query_group_id: 首次查询群组ID
   - first_query_group_name: 首次查询群组名称
   - first_query_time: 首次查询时间
   - first_market_cap: 首次查询时市值
   - first_price: 首次查询时价格
   - highest_market_cap: 历史最高市值
   - highest_price: 历史最高价格
   - highest_time: 达到最高价格时间
   - max_multiple: 最大倍数（最高市值/首次查询市值）
   - is_successful: 是否成功（涨幅是否超过50%）
   - query_count: 查询次数
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间

3. **crypto_ca_query_record**：CA查询表
   - id: 主键
   - ca_id: 关联的CA ID
   - user_id: 查询用户ID
   - user_name: 查询用户名
   - group_id: 查询群组ID
   - group_name: 查询群组名
   - query_time: 查询时间
   - market_cap_at_query: 查询时市值
   - price_at_query: 查询时价格
   - created_at: 创建时间
   - multiple_from_first: 与首次查询的倍数
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间
   - chain_type: 链类型

4. **crypto_group_statistics**： 群组CA统计表
   - id: 主键
   - group_id: 群组ID
   - total_ca_queries: 总CA查询次数
   - unique_ca_count: 不同CA数量(ca去重后)
   - successful_ca_count: 成功CA数量（涨幅超过50%）
   - win_rate: 胜率(成功CA数/不同CA数)
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间

5. **crypto_monitor_config**：监控配置表
   - id: 主键
   - user_id: 用户ID
   - coin_id: 币种ID
   - min_price: 最低价格监控
   - max_price: 最高价格监控
   - percent_change: 波动百分比
   - notification_type: 通知类型
   - is_active: 是否激活
   - last_notification_time: 上次通知时间
   - notification_target: 通知目标(群ID或用户ID)
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间
   - chain_type: 链类型

6. **crypto_bot_group**：机器人群组表
   - id: 主键
   - platform: 平台(wechat/telegram)
   - group_id: 平台上的群组ID
   - group_name: 群组名称
   - win_threshold: 胜率计算阈值(默认0.5) 
   - is_active: 是否激活
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间

###  2025-05-22新增
1. **crypto_wallet**：钱包表
   - wallet_address: 钱包地址(主键)
   - wallet_name: 钱包备注
   - wallet_chain_type: 链类型
   - add_time: 添加时间
   - last_active_time: 最后活跃时间
   - monitor_state: 监控状态
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间
   - chain_type: 链类型

2. **crypto_wallet_transactions**：存储钱包交易记录
   - id: 主键
   - wallet_address: 钱包地址(外键)
   - coin_address: 代币地址(关联crypto_coin表)
   - operation_type: 操作类型(买入/卖出/清仓)
   - transaction_amount: 交易金额
   - chain_type: 货币类型(SOL/BNB/USDT)
   - price: 价格
   - market: 市值
   - safe_info: 安全评估信息
   - transaction_time: 交易时间戳
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间
   - chain_type: 链类型

3. **crypto_price_history** 价格历史表(记录需要监控的代币)
   - id: 主键
   - coin_address: 代币地址(关联crypto_coin表)
   - price: 价格
   - market: 市值
   - recording_time: 记录时间
   - data_source: 数据来源
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间
   - chain_type: 链类型

###  2025-06-28新增（预警系统相关）
4. **crypto_alert_config**：预警配置表
   - id: 主键
   - alert_type: 预警类型(热度预警/大额交易/异常活跃等)
   - alert_name: 预警名称
   - threshold_value: 阈值数值
   - threshold_unit: 阈值单位(个/万U/%)
   - time_window: 时间窗口(分钟)
   - description: 配置说明
   - is_active: 是否启用(0:禁用,1:启用)
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间

5. **crypto_alert_record**：预警记录表
   - id: 主键
   - alert_type: 预警类型
   - alert_level: 预警级别(1:低,2:中,3:高)
   - coin_address: 相关代币地址
   - coin_symbol: 代币符号
   - wallet_addresses: 相关钱包地址列表(JSON格式)
   - alert_content: 预警内容描述
   - trigger_value: 触发值
   - threshold_value: 阈值
   - is_sent: 是否已推送(0:未推送,1:已推送)
   - send_channels: 推送渠道(TG/WeChat/Web)
   - send_time: 推送时间
   - alert_time: 预警触发时间
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间

6. **crypto_okx_signals**：OKX信号记录表
   - id: 主键
   - signal_type: 信号类型(买入/卖出/观察)
   - coin_symbol: 代币符号
   - coin_address: 代币地址
   - signal_content: 信号内容
   - signal_source: 信号来源
   - confidence_level: 置信度(1-5)
   - price_at_signal: 信号时价格
   - market_cap_at_signal: 信号时市值
   - signal_time: 信号时间
   - is_processed: 是否已处理
   - effectiveness: 信号有效性(后续验证)
   - del_flag: 删除状态
   - create_by: 创建人
   - create_time: 创建时间
   - update_by: 修改人
   - update_time: 修改时间

### 数据库表关系
- 用户可以创建多个监控配置
- 每个币种可以有多个价格记录
- 每个币种可以被多个用户监控
- 查询日志关联用户和币种

## 技术选型与模块划分

### Java端技术栈
- 框架：若依框架(Spring Boot)
- 安全：Spring Security
- 数据库：MySQL
- 缓存：Redis
- 任务调度：Quartz

### Python端技术栈
- Web框架：FastAPI
- 爬虫框架：Scrapy/Requests
- 数据处理：Pandas
- 机器人框架：WeChatpy, python-telegram-bot
- 加密货币API：CCXT

### 模块划分
1. **crypto-monitor**：Java端核心模块
   - controller: API控制器
   - service: 业务逻辑
   - mapper: 数据库映射
   - model: 数据模型
   - config: 配置类

2. **python-services**：Python服务集群
   - price_monitor: 价格监控服务
   - wechat_bot: 微信机器人服务
   - telegram_bot: Telegram机器人服务
   - data_analysis: 数据分析服务

## 接口规划

### RESTful API
1. `/api/crypto/coins`: 币种相关操作
2. `/api/crypto/monitor`: 监控配置相关操作
3. `/api/crypto/records`: 价格记录相关操作
4. `/api/crypto/statistics`: 统计数据相关操作
5. `/api/crypto/ranking`: 排行榜相关操作
6. `/api/crypto/notification`: 通知相关操作

### Python服务API
1. `/price/current`: 获取当前价格
2. `/price/history`: 获取历史价格
3. `/bot/wechat/message`: 处理微信消息
4. `/bot/telegram/message`: 处理Telegram消息
5. `/analysis/trend`: 趋势分析

## 部署架构

### 开发环境
- JDK 17+
- Python 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 14+ (前端开发)

### 生产环境
- Docker容器化部署
- Nginx反向代理
- 负载均衡
- 数据备份策略

## 系统菜单结构规划

### 后台管理菜单
```
加密货币管理
├── 代币管理
│   ├── 代币查询          (已有 - 基础查询功能)
│   └── 监控代币列表      (新增 - 阶段一第2周)
├── 钱包管理             (已有 - 完整功能)
├── 信号预警
│   ├── OKX信号展示      (新增 - 阶段一第1周)
│   ├── 预警历史         (新增 - 阶段二)
│   └── 阈值配置         (新增 - 阶段一第1周)
├── 机器人管理           (已有 - TG机器人控制)
└── 数据统计
    ├── 查询统计         (已有基础版)
    ├── 用户排行         (已有基础版)
    └── 预警效果分析     (新增 - 阶段四)
```

### 预警推送渠道优先级
1. **TG机器人推送** (阶段三第1优先级)
2. **首页预警面板** (阶段三第2优先级)  
3. **微信机器人推送** (条件允许时)

### 核心差异化功能
- **基于钱包监控的实时预警** - 市面上少有的功能
- **聪明钱共识分析** - 多钱包同币种操作检测
- **OKX信号集成** - 第三方信号源整合
- **智能预警阈值管理** - 动态配置系统
