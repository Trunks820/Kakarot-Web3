# 加密货币监控系统开发计划

## 项目概述
这是一个基于若依框架开发的功能完整的加密货币监控系统，集成了微信和Telegram机器人，提供实时价格监控、查询统计、排行榜等功能。系统采用Java + Python双端架构，确保高性能和灵活性。

## 开发阶段与时间规划

### 阶段一：核心模块扩展（1周）
- **第1周：基础框架扩展**
  - 创建crypto-monitor模块（Java模块）
  - 设计并创建数据库表结构（监控配置、价格记录、查询日志等）
  - 集成现有用户管理系统
  - 整合已有微信机器人和TG播报功能
  - 创建统一的Python服务环境

### 阶段二：核心功能开发（3周）

#### 第2周：价格监控服务
- 优化现有价格数据获取服务
  - 扩展币种价格数据获取（整合更多API源）
  - 建立价格数据存储与缓存机制
  - 设计监控配置管理界面
  - 开发价格波动预警机制

#### 第3周：机器人服务
- 扩展现有微信机器人功能
  - 增加更多命令支持
  - 优化价格查询功能
  - 开发新的价格提醒设置
- 增强Telegram机器人功能
  - 整合现有聪明钱播报
  - 添加更多交互功能
- 统一机器人与核心系统的数据交互

#### 第4周：数据记录与分析
- 实现统一查询记录系统
- 开发价格历史记录功能
- 实现涨跌倍数计算
- 开发群聊活跃度统计功能
- 设计基础数据分析服务

### 阶段三：高级功能与UI优化（2周）

#### 第5周：排行榜系统
- 开发查询次数排行功能
- 实现币种热度排行
- 开发价格涨幅排行
- 实现用户预测准确率排行
- 开发自定义排行生成功能

#### 第6周：界面优化与数据可视化
- 开发监控列表页面
- 实现查询统计图表
- 开发价格走势对比功能
- 设计排行榜展示界面
- 对整体UI进行优化

### 阶段四：系统集成与优化（1周）

#### 第7周：系统优化与部署
- 完善通知系统
- 优化数据库查询性能
- 实现Docker容器化配置
- 完成系统部署文档
- 进行系统负载测试

## 功能优先级分类

### 最高优先级（第一阶段必须完成）
1. crypto-monitor模块创建
2. 数据库表结构设计
3. 已有机器人功能整合
4. 统一的价格监控服务

### 高优先级（第二阶段关键功能）
1. 扩展现有机器人功能
2. 增强价格数据获取与存储
3. 涨跌倍数计算
4. 统一查询记录系统

### 中优先级（第三阶段增强功能）
1. 排行榜系统
2. 数据可视化与图表
3. 价格波动预警
4. UI优化

### 低优先级（最后阶段完善功能）
1. 高级数据分析
2. Docker容器化配置
3. 系统性能优化
4. 部署文档完善


## 数据库设计初步规划

### 核心表结构
1. **crypto_coin**：CA基础信息表
   - id: 主键
   - address: 合约地址
   - symbol: 币种符号
   - name: 币种名称
   - logo_url: 币种图标(如果有)
   - created_at: 创建时间
   - updated_at: 更新时间
   - chain_type: 链类型
   - description: 描述

2. **crypto_ca_record**: CA查询记录表
   - id: 主键
   - ca_id: 关联的CA ID
   - first_query_user_id: 首次查询用户ID
   - first_query_group_id: 首次查询群组ID
   - first_query_time: 首次查询时间
   - first_market_cap: 首次查询时市值
   - first_price: 首次查询时价格
   - highest_market_cap: 历史最高市值
   - highest_price: 历史最高价格
   - highest_time: 达到最高价格时间
   - max_multiple: 最大倍数（最高市值/首次查询市值）
   - is_successful: 是否成功（涨幅是否超过50%）
   - query_count: 查询次数
   - created_at: 创建时间
   - updated_at: 更新时间

3. **crypto_ca_query_record**：CA查询表
   - id: 主键
   - ca_id: 关联的CA ID
   - user_id: 查询用户ID
   - group_id: 查询群组ID
   - query_time: 查询时间
   - market_cap_at_query: 查询时市值
   - price_at_query: 查询时价格
   - created_at: 创建时间
   - multiple_from_first: 与首次查询的倍数

4. **crypto_group_statistics**： 群组CA统计表
   - id: 主键
   - group_id: 群组ID
   - total_ca_queries: 总CA查询次数
   - unique_ca_count: 不同CA数量(ca去重后)
   - successful_ca_count: 成功CA数量（涨幅超过50%）
   - win_rate: 胜率(成功CA数/不同CA数)
   - last_calculated_time: 最后计算时间
   - created_at: 创建时间
   - updated_at: 更新时间

5. **crypto_monitor_config**：监控配置表
   - id: 主键
   - user_id: 用户ID
   - coin_id: 币种ID
   - min_price: 最低价格监控
   - max_price: 最高价格监控
   - percent_change: 波动百分比
   - notification_type: 通知类型
   - is_active: 是否激活
   - created_at: 创建时间
   - updated_at: 更新时间
   - last_notification_time: 上次通知时间
   - notification_target: 通知目标(群ID或用户ID)

6. **crypto_bot_group**：机器人群组表
   - id: 主键
   - platform: 平台(wechat/telegram)
   - group_id: 平台上的群组ID
   - group_name: 群组名称
   - win_threshold: 胜率计算阈值(默认0.5) 
   - created_at: 创建时间
   - is_active: 是否激活

###  2025-05-22新增
1. **crypto_wallet**：钱包表
   - wallet_address: 钱包地址(主键)
   - wallet_name: 钱包备注
   - wallet_level: 重要度分级
   - wallet_chain_type: 链类型
   - add_time: 添加时间
   - last_active_time: 最后活跃时间
   - monitor_state: 监控状态

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

3. **crypto_price_history** 价格历史表(记录需要监控的代币)
   - id: 主键
   - coin_address: 代币地址(关联crypto_coin表)
   - price: 价格
   - market: 市值
   - recording_time: 记录时间
   - data_source: 数据来源

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
