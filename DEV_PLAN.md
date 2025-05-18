# 加密货币监控系统开发计划

## 项目概述
这是一个基于若依框架开发的功能完整的加密货币监控系统，集成了微信和Telegram机器人，提供实时价格监控、查询统计、排行榜等功能。系统采用Java + Python双端架构，确保高性能和灵活性。

## 开发阶段与时间规划

### 阶段一：核心模块扩展（2周）
- **第1-2周：基础框架扩展**
  - 创建crypto-monitor模块（Java模块）
  - 设计并创建数据库表结构（监控配置、价格记录、查询日志等）
  - 集成现有用户管理系统
  - 创建Python服务基础环境

### 阶段二：核心功能开发（6周）

#### 第3-4周：价格监控服务
- 开发Python价格监控服务
- 实现币种价格数据获取（使用ccxt或其他API）
- 建立价格数据存储与缓存机制
- 设计监控配置管理界面
- 开发价格波动预警机制

#### 第5-6周：机器人服务
- 开发微信机器人服务
  - 实现基础命令系统
  - 实现`ca`命令价格查询功能
  - 开发价格提醒设置功能
- 开发Telegram机器人服务（基本功能）
- 实现机器人与核心系统的数据交互

#### 第7-8周：数据记录与分析
- 实现查询记录系统
- 开发价格历史记录功能
- 实现涨跌倍数计算
- 开发群聊活跃度统计功能
- 设计基础数据分析服务

### 阶段三：高级功能与UI优化（4周）

#### 第9-10周：排行榜系统
- 开发查询次数排行功能
- 实现币种热度排行
- 开发价格涨幅排行
- 实现用户预测准确率排行
- 开发自定义排行生成功能

#### 第11-12周：界面优化与数据可视化
- 开发监控列表页面
- 实现查询统计图表
- 开发价格走势对比功能
- 设计排行榜展示界面
- 对整体UI进行优化

### 阶段四：系统集成与优化（3周）

#### 第13-14周：通知系统完善与Docker容器化
- 完善微信群通知功能
- 开发微信个人通知功能
- 优化Telegram通知系统
- 实现Docker容器化配置

#### 第15周：部署与性能优化
- 优化数据库查询性能
- 完善Docker Compose配置
- 进行系统负载测试
- 优化API响应速度
- 完成系统部署文档

## 功能优先级分类

### 最高优先级（第一阶段必须完成）
1. crypto-monitor模块创建
2. 数据库表结构设计
3. Python服务基础环境搭建
4. 价格监控基础服务

### 高优先级（第二阶段关键功能）
1. 微信机器人的`ca`命令查询功能
2. 价格数据获取与存储
3. 涨跌倍数计算
4. 基础查询记录系统

### 中优先级（第三阶段增强功能）
1. 排行榜系统
2. Telegram机器人服务
3. 数据可视化与图表
4. 价格波动预警

### 低优先级（最后阶段完善功能）
1. 用户预测准确率排行
2. 高级数据分析
3. Docker容器化配置
4. 系统性能优化

## 数据库设计初步规划

### 核心表结构
1. **crypto_coin**：币种信息表
   - id: 主键
   - symbol: 币种符号
   - name: 币种名称
   - initial_price: 初始价格
   - current_price: 当前价格
   - logo_url: 币种图标
   - contract_address: 合约地址
   - created_at: 创建时间
   - updated_at: 更新时间

2. **crypto_price_record**：价格记录表
   - id: 主键
   - coin_id: 币种ID
   - price: 价格
   - market_cap: 市值
   - volume_24h: 24小时交易量
   - record_time: 记录时间

3. **crypto_monitor_config**：监控配置表
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

4. **crypto_query_log**：查询日志表
   - id: 主键
   - user_id: 用户ID (可选，如果是游客则为空)
   - coin_id: 币种ID
   - platform: 查询平台(wechat/telegram/web)
   - group_id: 群组ID(如适用)
   - query_time: 查询时间
   - price_at_query: 查询时的价格

5. **crypto_bot_group**：机器人群组表
   - id: 主键
   - platform: 平台(wechat/telegram)
   - group_id: 平台上的群组ID
   - group_name: 群组名称
   - created_at: 创建时间
   - is_active: 是否激活

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

## 项目里程碑

1. **M1**：基础架构搭建完成 (第2周末)
2. **M2**：核心功能(价格监控、机器人)完成 (第6周末)
3. **M3**：数据记录与分析功能完成 (第8周末)
4. **M4**：高级功能与UI优化完成 (第12周末)
5. **M5**：系统集成与优化完成，项目上线 (第15周末) 