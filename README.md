# 加密货币监控系统 (Cryptocurrency Monitoring System)

## 项目简介

这是一个基于若依框架开发的功能完整的加密货币监控系统，集成了微信和Telegram机器人，提供实时价格监控、查询统计、排行榜等功能。系统采用Java + Python双端架构，确保高性能和灵活性。

## 系统架构

### 后端服务
- **Java Web系统** (Spring Boot)
    - 用户界面管理
    - 身份认证服务
    - 监控配置管理
    - RESTful API服务

- **Python服务集群**
    - 价格/市值监控服务
    - 微信机器人
    - Telegram机器人
    - 数据分析服务

### 数据存储
- MySQL: 用户数据与查询记录
- Redis: 实时数据缓存

## 核心功能

### 1. 用户管理
- 用户注册/登录
- 个人资料管理
- 权限控制系统

### 2. 监控配置
- 创建/编辑/删除监控
- 自定义监控参数
- 监控状态管理

### 3. 价格监控
- 单币种价格监控
- 市值监控
- 价格波动预警

### 4. 查询记录系统
- 用户查询日志
- 价格历史记录
- 涨跌倍数计算
- 群聊活跃度统计

### 5. 排行榜系统
- 查询次数排行
- 币种热度排行
- 价格涨幅排行
- 用户预测准确率排行

### 6. 通知系统
- 微信群通知
- 微信个人通知
- Telegram通知

## 技术栈

### Java端
- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf/Vue.js
- MySQL

### Python端
- FastAPI/Flask
- APScheduler
- WeChatpy
- python-telegram-bot
- requests/ccxt

### 前端
- Bootstrap
- Vue.js/React (可选)
- Charts.js

### 运维
- Docker
- Docker Compose

## 快速开始

### 环境要求
- JDK 17+
- Python 3.8+
- MySQL 8.0+
- Redis 6.0+
- Docker & Docker Compose

## 机器人命令

### 微信机器人
- `/<币种>`: 查询币种当前价格
- `ca`: 查询当前ca信息
- `/top`: 查询当天发送ca涨跌倍数
- `设置提醒 ca <价格>`: 设置价格提醒

## 配置说明

### 机器人配置
```yaml
bot:
  wechat:
    token: your_wechat_token
  telegram:
    token: your_telegram_token
```

## 项目结构说明

```
├── ruoyi-ui              // 前端框架 [80]
├── ruoyi-gateway         // 网关模块 [8080]
├── ruoyi-auth           // 认证中心 [9200]
├── ruoyi-api           // 接口模块
│       └── ruoyi-api-system                          // 系统接口
├── ruoyi-common         // 通用模块
│       └── ruoyi-common-core                         // 核心模块
│       └── ruoyi-common-datascope                    // 权限范围
│       └── ruoyi-common-datasource                   // 多数据源
│       └── ruoyi-common-log                          // 日志记录
│       └── ruoyi-common-redis                        // 缓存服务
│       └── ruoyi-common-security                     // 安全模块
│       └── ruoyi-common-swagger                      // 系统接口
├── ruoyi-modules         // 业务模块
│       └── ruoyi-system                              // 系统模块 [9201]
│       └── ruoyi-gen                                 // 代码生成 [9202]
│       └── ruoyi-job                                 // 定时任务 [9203]
│       └── ruoyi-file                                // 文件服务 [9300]
│       └── ruoyi-crypto                          // 加密货币监控模块 [9204]
├── ruoyi-visual         // 图形化管理模块
│       └── ruoyi-visual-monitor                      // 监控中心 [9100]
├── docker               // 容器化部署脚本
└── python-service      // Python服务集群
        └── price_monitor                             // 价格监控服务
        └── wechat_bot                                // 微信机器人服务
        └── telegram_bot                              // Telegram机器人服务
        └── data_analysis                             // 数据分析服务
```

## 致谢

感谢以下开源项目的支持：
- RuoYi-Vue 
- Spring Boot
- FastAPI
- WeChatpy
- python-telegram-bot
- Vue.js
- 等其他开源社区的贡献者