# Python服务开发计划

## 开发优先级和步骤

### 第一阶段：基础架构（最高优先级）
1. **机器人状态监控**
   - 实现心跳机制
   - 实现状态检查
   - 实现自动重启
   - 与Java系统集成

2. **数据存储实现**
   - 数据库连接池
   - 消息存储
   - 状态存储
   - 数据同步机制

3. **基础API实现**
   - 状态查询API
   - 消息历史API
   - 数据同步API

### 第二阶段：核心功能（次优先级）
1. **预警系统**
   - 预警条件设置
   - 预警通知实现
   - 预警历史记录
   - 预警规则优化

2. **价格提醒服务**
   - 价格监控实现
   - 提醒发送机制
   - 提醒管理功能
   - 与预警系统集成

3. **交易统计分析**
   - 钱包活跃度分析
   - 代币热度分析
   - 市场趋势分析
   - 数据导出功能

4. **指定钱包交易记录**
   - 交易查询功能
   - 交易模式分析
   - 交易链接追踪
   - 可视化展示

### 第三阶段：高级功能（最后实现）
1. **钱包盈亏跟踪**
   - 持仓分析
   - 收益计算
   - 历史表现分析
   - 报表生成

2. **系统优化**
   - 性能优化
   - 缓存策略
   - 异步处理
   - 监控告警

## 开发步骤说明
1. 每个功能模块遵循以下开发步骤：
   - 基础功能实现
   - 单元测试编写
   - 集成测试验证
   - 性能优化
   - 文档完善

2. 开发规范：
   - 编写详细的注释和文档
   - 使用类型注解
   - 实现错误处理机制

3. 测试要求：
   - 单元测试覆盖率>80%
   - 集成测试覆盖所有关键流程
   - 性能测试满足响应时间要求
   - 压力测试验证系统稳定性

## 1. 数据存储与集成
### 数据表设计
- **查看dev_plan中 0522创建的**

### 与Java系统集成
- 使用crypto_coin表作为代币基础信息
- 使用crypto_monitor_config表作为价格监控配置
- 实现数据同步机制，确保Python和Java系统数据一致
- 添加数据库连接池管理
- 实现定期数据备份机制

## 2. 交易统计分析
### 核心分析维度
- **钱包活跃度分析**
  - 每个钱包的交易频率
  - 活跃时间段分析
  - 交易金额分布
  - 交易偏好分析(偏好的代币类型)

- **代币热度分析**
  - 被交易最频繁的代币排名
  - 交易量最大的代币排名
  - 买入/卖出比例最高的代币
  - 与crypto_ca_record表数据关联分析

- **市场趋势分析**
  - 整体买入/卖出比例
  - 每日交易量变化
  - 热门代币变化趋势
  - 与Java系统统计数据整合

### 数据导出与可视化
- 添加时间窗口分析(日/周/月维度)
- 实现数据导出功能(CSV/Excel)
- 生成分析报告API，供Java系统调用

## 3. 钱包盈亏跟踪(暂定先不做)
### 核心功能
- **持仓分析**
  - 追踪每个钱包的代币持仓情况
  - 计算每笔交易的买入/卖出均价
  - 估算当前持仓价值
  - 与crypto_coin表价格数据关联

- **收益计算**
  - 已实现收益(已卖出部分)
  - 未实现收益(当前持仓)
  - 总收益率计算
  - 按时间段统计收益

- **历史表现**
  - 历史交易的盈亏记录
  - 盈利最多/亏损最多的代币
  - 最佳/最差交易的识别
  - 与Java系统查询记录关联

## 4. 指定钱包交易记录
### 核心功能
- **详细交易查询**
  - 按钱包地址筛选交易
  - 按时间范围筛选
  - 按代币筛选
  - 与crypto_coin表关联查询

- **交易模式分析**
  - 识别重复出现的交易模式
  - 分析交易时间规律
  - 关联交易的识别(多个相关钱包同时操作)
  - 生成交易模式报告

- **交易链接追踪**
  - 链接到区块链浏览器查看原始交易
  - 关联交易的可视化
  - 提供API接口供Java系统调用

## 5. 预警系统
### 核心功能
- **触发条件设置**
  - 多钱包同时操作同一代币
  - 大额交易预警
  - 异常交易频率预警
  - 新代币首次交易预警
  - 与crypto_monitor_config表集成

- **预警通知**
  - TG消息通知
  - 微信消息通知
  - Webhook集成
  - 与Java系统通知机制集成

- **预警历史**
  - 记录已触发的预警
  - 预警有效性分析
  - 预警规则优化建议
  - 提供API接口供Java系统查询

## 6. 机器人服务
### 核心功能
- **微信机器人**
  - 实现心跳机制，向Java系统报告状态
  - 处理用户查询请求
  - 发送价格提醒和预警通知
  - 记录消息到数据库

- **Telegram机器人**
  - 实现心跳机制，向Java系统报告状态
  - 处理用户查询请求
  - 发送价格提醒和预警通知
  - 记录消息到数据库

- **机器人状态监控**
  - 定期检查机器人运行状态
  - 自动重启异常机器人
  - 向Java系统报告状态变化

## 7. 价格提醒服务
### 核心功能
- **价格监控**
  - 定期获取代币价格
  - 与crypto_monitor_config表配置比对
  - 触发价格提醒条件

- **提醒发送**
  - 通过微信机器人发送提醒
  - 通过Telegram机器人发送提醒
  - 记录提醒历史

- **提醒管理**
  - 提供API接口管理提醒配置
  - 与Java系统监控配置同步
  - 支持批量提醒管理

## 8. 系统部署与维护
### 部署方案
- Docker容器化配置
- Docker Compose管理多个服务
- 自动化部署脚本

### 监控与日志
- 系统监控
- 日志记录
- 异常告警

### 性能优化
- 数据库查询优化
- 缓存策略
- 异步处理机制