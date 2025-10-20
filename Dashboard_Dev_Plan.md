# 首页重构开发文档

> 通用工作台 - 模块化Widget设计
> 
> 版本: v1.0 | 日期: 2025-10-20

---

## 📑 文档导航

- [第一部分：产品与设计说明](#第一部分产品与设计说明)
- [第二部分：技术实现方案](#第二部分技术实现方案)
- [第三部分：代码示例参考](#第三部分代码示例参考)

---
---

# 第一部分：产品与设计说明

> 本部分描述产品需求、用户体验、界面设计，与具体技术实现无关

---

## 1. 项目背景与目标

### 1.1 重构原因

**当前问题**：
- 首页功能固定，不支持扩展
- 组件耦合度高（Token监控、预警中心、OKX信号等）
- 无法适应未来新功能（微信机器人、Telegram等）

**重构目标**：
- ✅ 模块化Widget设计，低耦合
- ✅ 支持多功能并存（Token监控 + 微信机器人 + ...）
- ✅ 用户可配置显示哪些Widget
- ✅ 统一的通知中心
- ✅ 高扩展性，添加新功能无需改动现有代码

### 1.2 要替换的组件

```
删除清单（5个组件）：
1. EnhancedDataSummary - 数据概览（简化保留）
2. AlertPanel - 预警中心（删除）
3. MonitorTokens - 监控代币（删除）
4. PopularCA - 热门CA（删除）
5. OkxSignalPreview - OKX信号（删除）
```

---

## 2. 页面布局设计

### 2.1 整体结构

```
┌─────────────────────────────────────────────────────────────┐
│  1️⃣ 欢迎头部区域                                             │
│  - 用户问候、当前日期                                         │
│  - 系统状态、待处理事项                                       │
│  - 配置工作台按钮                                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  2️⃣ Widget展示区域（3列网格布局）                            │
│                                                              │
│  ┌──────────────────────┐  ┌──────────────────────┐        │
│  │ Widget 1             │  │ Widget 2             │        │
│  │ Token监控            │  │ 微信机器人（占位）    │        │
│  │                      │  │                      │        │
│  │ • SOL链: 245个      │  │ • 功能开发中          │        │
│  │ • BSC链: 89个       │  │ • 计划功能列表        │        │
│  │ • 今日触发: 15次    │  │                      │        │
│  │                      │  │ [开始配置]            │        │
│  │ [详情] [新增] [报表]│  │ [查看文档]            │        │
│  └──────────────────────┘  └──────────────────────┘        │
│                                                              │
│  ┌──────────────────────┐                                   │
│  │ Widget 3             │                                   │
│  │ Twitter推送          │                                   │
│  │                      │                                   │
│  │ • 今日推送: 156次    │                                   │
│  │ • 成功率: 98.7%      │                                   │
│  │ • 待同步: 2个        │                                   │
│  │                      │                                   │
│  │ [推送日志] [同步]    │                                   │
│  └──────────────────────┘                                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  3️⃣ 通知中心区域（底部，全宽）                               │
│                                                              │
│  🔔 最新动态 (5)                     [标记已读] [查看全部]   │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ • 5分钟前  [Token监控] DEAR触发涨幅预警 (+15.2%)      │  │
│  │ • 10分钟前 [Twitter] 推送配置同步成功                 │  │
│  │ • 15分钟前 [Token监控] PEPE持币人数增长 (+12.3%)      │  │
│  │ • 20分钟前 [系统] 新增120个Token                      │  │
│  │ • 25分钟前 [Token监控] BSC链新增监控配置              │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 响应式适配

#### 桌面端（≥1200px）
- Widget区域：3列网格
- 每个Widget最小高度300px

#### 平板端（768px - 1200px）
- Widget区域：2列网格
- 自动换行

#### 移动端（<768px）
- Widget区域：1列堆叠
- 通知中心可滑动查看

---

## 3. Widget设计规范

### 3.1 Widget卡片结构

每个Widget统一采用以下结构：

```
┌─────────────────────────────────────┐
│ Header                              │
│ ┌─────────────────────────────────┐│
│ │ 🎯 Widget标题      [运行状态]   ││
│ └─────────────────────────────────┘│
├─────────────────────────────────────┤
│ Body                                │
│ ┌─────────────────────────────────┐│
│ │ 核心数据展示                     ││
│ │ • 统计数字                       ││
│ │ • 图表                           ││
│ │ • 列表                           ││
│ └─────────────────────────────────┘│
├─────────────────────────────────────┤
│ Footer                              │
│ ┌─────────────────────────────────┐│
│ │ [操作按钮1] [操作按钮2]          ││
│ └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

### 3.2 Widget类型定义

#### Widget 1: Token监控
- **标题**：📊 Token监控
- **状态标签**：运行中（绿色）
- **核心内容**：
  - 链状态（SOL/BSC）
  - 监控数量
  - 今日触发统计
- **操作按钮**：查看详情 | 新增监控 | 查看报表

#### Widget 2: 微信机器人（占位）
- **标题**：🤖 微信机器人
- **状态标签**：即将上线（灰色）
- **核心内容**：
  - "功能开发中..."提示
  - 计划功能列表
- **操作按钮**：开始配置（禁用） | 查看文档

#### Widget 3: Twitter推送
- **标题**：🐦 Twitter推送
- **状态标签**：运行中（绿色）
- **核心内容**：
  - 今日推送统计
  - 成功率进度条
  - 待同步配置列表
- **操作按钮**：推送日志 | 同步配置

### 3.3 即将上线Widget的展示规范

对于未开发完成的Widget（如微信机器人）：
- ✅ 显示"即将上线"状态标签
- ✅ 显示"功能开发中..."提示
- ✅ 列出计划功能清单
- ✅ 操作按钮设为禁用状态
- ✅ 提供文档链接（如果有）
- ❌ 不隐藏Widget，让用户知道即将支持什么功能

---

## 4. 通知中心设计

### 4.1 通知来源

所有功能模块的通知都汇总到通知中心：

| 模块 | 通知类型 | 示例 |
|------|---------|------|
| Token监控 | 预警通知 | DEAR触发涨幅预警 (+15.2%) |
| Twitter | 状态通知 | 推送配置同步成功 |
| 系统 | 信息通知 | 新增120个Token |
| 微信机器人 | 消息通知 | 群聊收到新消息（未来） |

### 4.2 通知级别

- 🔴 **Alert（预警）**：红色点 + 红色时间线
- 🟡 **Warning（警告）**：橙色点 + 橙色时间线
- 🟢 **Success（成功）**：绿色点 + 绿色时间线
- 🔵 **Info（信息）**：蓝色点 + 蓝色时间线

### 4.3 通知交互

- **点击通知**：跳转到对应详情页
- **未读标记**：背景色高亮 + 红点提示
- **标记已读**：单个标记或批量标记
- **查看全部**：跳转到通知列表页

---

## 5. 用户配置功能

### 5.1 配置工作台对话框

用户点击"配置工作台"按钮，弹出对话框：

```
┌─────────────────────────────────────┐
│ ⚙️ 配置工作台                        │
├─────────────────────────────────────┤
│                                     │
│ Widget管理：                         │
│                                     │
│ ☑ 📊 Token监控          [启用]     │
│ ☑ 🤖 微信机器人         [启用]     │
│ ☑ 🐦 Twitter推送        [启用]     │
│                                     │
│ [重置为默认] [保存]                  │
└─────────────────────────────────────┘
```

### 5.2 配置项

- **Widget启用/禁用**：勾选框控制
- **Widget排序**：拖拽排序（未来功能）
- **重置为默认**：恢复初始配置

---

## 6. 设计规范

### 6.1 色彩规范

| 用途 | 颜色 | 说明 |
|------|------|------|
| 主色调 | #409EFF | Element Plus Primary |
| 成功色 | #67C23A | 运行中、成功 |
| 警告色 | #E6A23C | 待处理、警告 |
| 危险色 | #F56C6C | 预警、错误 |
| 信息色 | #909399 | 次要信息 |
| 背景色 | #F5F7FA | 页面背景 |
| 卡片色 | #FFFFFF | Widget背景 |

### 6.2 间距规范

- Widget之间间距：20px
- Widget内边距：20px
- 移动端间距：12px
- 移动端内边距：16px

### 6.3 字体规范

| 用途 | 字号 | 字重 |
|------|------|------|
| 页面标题 | 24px | 600 |
| Widget标题 | 16px | 600 |
| 正文 | 14px | 400 |
| 次要文字 | 12px | 400 |
| 数据大数字 | 24px | 600 |

### 6.4 圆角规范

- Widget卡片：12px
- 按钮：4px
- 标签：4px
- 小卡片（链状态）：8px

---

## 7. 交互规范

### 7.1 加载状态

- Widget数据加载：显示骨架屏或loading
- 页面初始化：全屏loading
- 按钮操作：按钮loading状态

### 7.2 空状态

- 无通知：显示空状态插画 + 提示文字
- Widget无数据：显示"暂无数据"

### 7.3 错误处理

- API调用失败：Toast提示
- 数据加载失败：显示重试按钮
- 操作失败：明确的错误提示

---

## 8. 未来扩展计划

### 8.1 短期计划（1-2个月）

- [ ] 微信机器人Widget开发完成
- [ ] Widget拖拽排序
- [ ] 通知中心过滤功能

### 8.2 中期计划（3-6个月）

- [ ] Telegram机器人Widget
- [ ] 数据分析Widget
- [ ] 自定义Widget布局（大中小）

### 8.3 长期计划（6个月+）

- [ ] Widget市场（第三方开发）
- [ ] 多套布局方案（工作/监控/分析）
- [ ] 跨平台同步（Web/Mobile/Desktop）

---
---

# 第二部分：技术实现方案

> 本部分描述前后端技术架构、数据库设计、API定义

---

## 1. 技术栈

### 1.1 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.x | 前端框架 |
| Pinia | 2.x | 状态管理 |
| Element Plus | 2.x | UI组件库 |
| Vue Router | 4.x | 路由管理 |

### 1.2 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.x | 后端框架 |
| MyBatis | 3.x | ORM框架 |
| MySQL | 8.0 | 数据库 |

---

## 2. 前端架构

### 2.1 目录结构

```
src/
├── views/
│   ├── index.vue                           # 首页主文件（重构）
│   └── dashboard/                          # 工作台模块（新建）
│       ├── components/
│       │   ├── WelcomeHeader.vue           # 欢迎头部
│       │   ├── NotificationCenter.vue      # 通知中心
│       │   └── WidgetConfigDialog.vue      # Widget配置弹窗
│       └── widgets/                        # Widget组件目录
│           ├── TokenMonitorWidget.vue      # Token监控Widget
│           ├── WeChatBotWidget.vue         # 微信机器人Widget
│           └── TwitterPushWidget.vue       # Twitter推送Widget
│
├── store/
│   └── modules/
│       ├── dashboard.js                    # 工作台状态管理（新）
│       └── notification.js                 # 通知状态管理（新）
│
├── api/
│   └── dashboard/
│       ├── config.js                       # Widget配置API（新）
│       ├── notification.js                 # 通知API（新）
│       └── stats.js                        # 统计数据API（新）
│
└── config/
    └── widgets.js                          # Widget注册配置（新）
```

### 2.2 Widget注册机制

```javascript
// src/config/widgets.js
export const WIDGET_REGISTRY = {
  'token-monitor': {
    id: 'token-monitor',
    name: 'Token监控',
    icon: '📊',
    component: 'TokenMonitorWidget',
    category: 'crypto',
    description: '实时监控Token发射和价格预警',
    defaultEnabled: true,
    sortOrder: 1
  },
  'wechat-bot': {
    id: 'wechat-bot',
    name: '微信机器人',
    icon: '🤖',
    component: 'WeChatBotWidget',
    category: 'bot',
    description: '微信群管理和自动回复',
    defaultEnabled: true,
    sortOrder: 2,
    comingSoon: true  // 标记为即将上线
  },
  'twitter-push': {
    id: 'twitter-push',
    name: 'Twitter推送',
    icon: '🐦',
    component: 'TwitterPushWidget',
    category: 'social',
    description: 'Twitter推送状态监控',
    defaultEnabled: true,
    sortOrder: 3
  }
}
```

**添加新Widget的步骤**：
1. 在 `widgets.js` 中注册
2. 创建 Widget 组件文件
3. 实现业务逻辑

### 2.3 状态管理架构

#### Dashboard Store（Widget配置管理）

```javascript
State:
- userConfig: []          // 用户Widget配置列表
- loading: false          // 加载状态

Getters:
- enabledWidgets          // 启用的Widget列表（已排序）
- allWidgets              // 所有Widget列表（包含禁用的）
- pendingCount            // 待处理事项数量

Actions:
- loadUserConfig()        // 加载用户配置
- updateWidgetConfig()    // 更新Widget配置
- toggleWidget()          // 切换Widget启用状态
- resetToDefault()        // 重置为默认配置
```

#### Notification Store（通知管理）

```javascript
State:
- list: []                // 通知列表
- unreadCount: 0          // 未读数量
- loading: false          // 加载状态

Getters:
- recentNotifications     // 最近5条通知
- unreadNotifications     // 未读通知

Actions:
- loadNotifications()     // 加载通知列表
- addNotification()       // 添加通知
- markAsRead()            // 标记为已读
- markAllAsRead()         // 全部标记为已读
```

### 2.4 通知系统数据流

```
业务模块（Token监控/Twitter/系统）
    ↓ 产生事件
notification.addNotification({
  module: 'token-monitor',
  type: 'alert',
  content: '...',
  actionUrl: '...'
})
    ↓ 存储到Pinia Store
    ↓ 触发UI更新
NotificationCenter组件实时显示
    ↓ 用户点击
标记为已读 + 跳转到详情页
```

---

## 3. 数据库设计

### 3.1 用户Widget配置表

```sql
CREATE TABLE `user_dashboard_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `widget_id` VARCHAR(50) NOT NULL COMMENT 'Widget标识（token-monitor/wechat-bot等）',
  `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `config_json` JSON DEFAULT NULL COMMENT 'Widget个性化配置（JSON格式）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_widget` (`user_id`, `widget_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户工作台配置表';
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 用户ID，关联sys_user表 |
| widget_id | VARCHAR(50) | Widget标识（token-monitor/wechat-bot/twitter-push） |
| enabled | TINYINT(1) | 是否启用：0-禁用，1-启用 |
| sort_order | INT | 排序顺序（数字越小越靠前） |
| config_json | JSON | Widget个性化配置（预留，如刷新频率、显示项等） |

**示例数据**：
```json
{
  "user_id": 1,
  "widget_id": "token-monitor",
  "enabled": 1,
  "sort_order": 1,
  "config_json": {
    "refreshInterval": 30,
    "showCharts": true
  }
}
```

### 3.2 系统通知表

```sql
CREATE TABLE `system_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID（0表示全部用户）',
  `module` VARCHAR(50) NOT NULL COMMENT '来源模块（token-monitor/twitter/system）',
  `module_name` VARCHAR(50) NOT NULL COMMENT '模块名称（Token监控/Twitter/系统）',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型（alert/info/success/warning/error）',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `action_url` VARCHAR(255) DEFAULT NULL COMMENT '点击跳转URL',
  `extra_data` JSON DEFAULT NULL COMMENT '额外数据（JSON格式）',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';
```

**字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| user_id | BIGINT | 接收用户ID（0表示全部用户） |
| module | VARCHAR(50) | 来源模块（token-monitor/twitter/system/wechat-bot） |
| module_name | VARCHAR(50) | 模块中文名（Token监控/Twitter/系统/微信机器人） |
| type | VARCHAR(20) | 通知类型（alert/info/success/warning/error） |
| title | VARCHAR(200) | 通知标题（如"DEAR触发涨幅预警"） |
| content | TEXT | 通知内容（详细描述） |
| action_url | VARCHAR(255) | 点击跳转URL（如 /crypto/token-monitor?ca=xxx） |
| extra_data | JSON | 额外数据（如Token详情、预警参数等） |
| is_read | TINYINT(1) | 是否已读：0-未读，1-已读 |

**示例数据**：
```json
{
  "id": 1,
  "user_id": 1,
  "module": "token-monitor",
  "module_name": "Token监控",
  "type": "alert",
  "title": "DEAR触发涨幅预警",
  "content": "Token DEAR 在过去5分钟内涨幅达到 +15.2%，超过设定阈值 10%",
  "action_url": "/crypto/token-monitor?ca=HM15KRP...",
  "extra_data": {
    "ca": "HM15KRP...",
    "symbol": "DEAR",
    "change": 15.2,
    "threshold": 10
  },
  "is_read": 0,
  "create_time": "2025-10-20 16:30:00"
}
```

---

## 4. API接口定义

### 4.1 Widget配置API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dashboard/config` | 获取用户Widget配置 |
| POST | `/dashboard/config` | 更新Widget配置 |
| POST | `/dashboard/config/batch` | 批量更新配置 |
| POST | `/dashboard/config/reset` | 重置为默认 |

#### GET /dashboard/config

**请求**：无参数

**响应**：
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": [
    {
      "widgetId": "token-monitor",
      "enabled": true,
      "sortOrder": 1
    },
    {
      "widgetId": "wechat-bot",
      "enabled": true,
      "sortOrder": 2
    }
  ]
}
```

#### POST /dashboard/config

**请求**：
```json
{
  "widgetId": "token-monitor",
  "enabled": false,
  "sortOrder": 1
}
```

**响应**：
```json
{
  "code": 200,
  "msg": "更新成功"
}
```

### 4.2 通知API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dashboard/notifications` | 获取通知列表 |
| PUT | `/dashboard/notifications/{id}/read` | 标记为已读 |
| PUT | `/dashboard/notifications/read-all` | 全部标记为已读 |

#### GET /dashboard/notifications

**请求参数**：
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认50）

**响应**：
```json
{
  "code": 200,
  "rows": [
    {
      "id": 1,
      "module": "token-monitor",
      "moduleName": "Token监控",
      "type": "alert",
      "title": "DEAR触发涨幅预警",
      "content": "Token DEAR 在过去5分钟内涨幅达到 +15.2%",
      "actionUrl": "/crypto/token-monitor?ca=xxx",
      "isRead": false,
      "createTime": "2025-10-20 16:30:00"
    }
  ],
  "total": 50
}
```

### 4.3 统计API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/dashboard/stats/token-monitor` | Token监控统计 |
| GET | `/dashboard/stats/twitter-push` | Twitter推送统计 |

#### GET /dashboard/stats/token-monitor

**响应**：
```json
{
  "code": 200,
  "data": {
    "sol": {
      "monitorCount": 245
    },
    "bsc": {
      "monitorCount": 89
    },
    "today": {
      "newTokens": 120,
      "alerts": 15,
      "pending": 3
    }
  }
}
```

---

## 5. 后端实现架构

### 5.1 Controller层

```
DashboardController
├── getUserConfig()          // 获取Widget配置
├── updateConfig()           // 更新配置
├── batchUpdateConfig()      // 批量更新
├── resetConfig()            // 重置
├── getNotifications()       // 获取通知
├── markAsRead()             // 标记已读
├── markAllAsRead()          // 全部已读
└── getTokenMonitorStats()   // 统计数据
```

### 5.2 Service层

```
IDashboardService
├── getUserConfig()
├── saveOrUpdateConfig()
├── batchUpdateConfig()
├── resetToDefault()
└── getTokenMonitorStats()

INotificationService
├── selectNotificationList()
├── markAsRead()
└── markAllAsRead()
```

### 5.3 Mapper层

```
DashboardMapper.xml
├── selectUserConfig
├── insertConfig
├── updateConfig
├── deleteConfig
└── selectStats

NotificationMapper.xml
├── selectNotificationList
├── updateReadStatus
└── updateAllReadStatus
```

---

## 6. 性能优化策略

### 6.1 前端优化

- **Widget懒加载**：使用 `defineAsyncComponent` 按需加载
- **通知列表分页**：首屏只显示5条，查看全部时分页加载
- **统计数据缓存**：30秒内复用缓存，减少API调用
- **防抖节流**：配置保存、搜索等操作加防抖

### 6.2 后端优化

- **数据库索引**：user_id、widget_id、is_read等字段建索引
- **批量查询**：Widget配置和通知列表使用IN查询
- **缓存**：统计数据使用Redis缓存30秒
- **异步处理**：通知创建使用异步线程池

---

## 7. 开发计划与里程碑

### Phase 1: 基础框架（1小时）
- [ ] 创建数据库表
- [ ] 创建目录结构
- [ ] 创建Widget注册配置
- [ ] 创建Store

### Phase 2: 核心组件（1.5小时）
- [ ] WelcomeHeader组件
- [ ] NotificationCenter组件
- [ ] WidgetConfigDialog组件

### Phase 3: Widget实现（1.5小时）
- [ ] TokenMonitorWidget
- [ ] WeChatBotWidget（占位）
- [ ] TwitterPushWidget

### Phase 4: 后端实现（1小时）
- [ ] Entity/Mapper/Service
- [ ] Controller
- [ ] API测试

### Phase 5: 联调测试（1小时）
- [ ] 前后端联调
- [ ] 响应式测试
- [ ] Bug修复

**总计：约6小时**

---
---

# 第三部分：代码示例参考

> 本部分提供关键代码实现示例，实际开发时可参考修改

---

## 1. 首页主文件示例

**文件**：`src/views/index.vue`

```vue
<template>
  <div class="dashboard-container">
    <WelcomeHeader />
    
    <div class="widget-grid" v-loading="loading">
      <component
        v-for="widget in enabledWidgets"
        :key="widget.id"
        :is="getWidgetComponent(widget.id)"
        class="widget-item"
      />
    </div>
    
    <NotificationCenter />
    
    <WidgetConfigDialog
      v-model="showConfigDialog"
      @refresh="loadDashboardConfig"
    />
  </div>
</template>

<script setup name="Dashboard">
import { ref, computed, onMounted, defineAsyncComponent } from 'vue'
import { useDashboardStore } from '@/store/modules/dashboard'
import { useNotificationStore } from '@/store/modules/notification'
import WelcomeHeader from './dashboard/components/WelcomeHeader.vue'
import NotificationCenter from './dashboard/components/NotificationCenter.vue'
import WidgetConfigDialog from './dashboard/components/WidgetConfigDialog.vue'

const dashboardStore = useDashboardStore()
const notificationStore = useNotificationStore()

const loading = ref(false)
const showConfigDialog = ref(false)

// Widget组件映射
const widgetComponents = {
  'token-monitor': defineAsyncComponent(() => 
    import('./dashboard/widgets/TokenMonitorWidget.vue')
  ),
  'wechat-bot': defineAsyncComponent(() => 
    import('./dashboard/widgets/WeChatBotWidget.vue')
  ),
  'twitter-push': defineAsyncComponent(() => 
    import('./dashboard/widgets/TwitterPushWidget.vue')
  )
}

const getWidgetComponent = (widgetId) => widgetComponents[widgetId]
const enabledWidgets = computed(() => dashboardStore.enabledWidgets)

const loadDashboardConfig = async () => {
  loading.value = true
  try {
    await dashboardStore.loadUserConfig()
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadDashboardConfig()
  await notificationStore.loadNotifications()
  
  // 每30秒刷新通知
  setInterval(() => {
    notificationStore.loadNotifications()
  }, 30000)
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.widget-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin: 20px 0;
}

@media (max-width: 1200px) {
  .widget-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;
  }
  
  .widget-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
```

---

## 2. Dashboard Store示例

**文件**：`src/store/modules/dashboard.js`

```javascript
import { defineStore } from 'pinia'
import { getUserDashboardConfig, updateUserDashboardConfig } from '@/api/dashboard/config'
import { WIDGET_REGISTRY } from '@/config/widgets'

export const useDashboardStore = defineStore('dashboard', {
  state: () => ({
    userConfig: [],
    loading: false
  }),
  
  getters: {
    enabledWidgets: (state) => {
      return state.userConfig
        .filter(item => item.enabled)
        .sort((a, b) => a.sortOrder - b.sortOrder)
        .map(item => ({
          ...item,
          ...WIDGET_REGISTRY[item.widgetId]
        }))
    }
  },
  
  actions: {
    async loadUserConfig() {
      this.loading = true
      try {
        const response = await getUserDashboardConfig()
        this.userConfig = response.data || []
        
        if (this.userConfig.length === 0) {
          this.userConfig = Object.keys(WIDGET_REGISTRY).map(widgetId => ({
            widgetId,
            enabled: WIDGET_REGISTRY[widgetId].defaultEnabled,
            sortOrder: WIDGET_REGISTRY[widgetId].sortOrder
          }))
        }
      } catch (error) {
        console.error('加载配置失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    async updateWidgetConfig(widgetId, config) {
      try {
        await updateUserDashboardConfig({ widgetId, ...config })
        
        const index = this.userConfig.findIndex(c => c.widgetId === widgetId)
        if (index !== -1) {
          this.userConfig[index] = { ...this.userConfig[index], ...config }
        }
        return true
      } catch (error) {
        console.error('更新配置失败:', error)
        return false
      }
    }
  }
})
```

---

## 3. TokenMonitorWidget示例

**文件**：`src/views/dashboard/widgets/TokenMonitorWidget.vue`

```vue
<template>
  <el-card class="widget-card" shadow="hover">
    <template #header>
      <div class="widget-header">
        <span class="widget-title">
          <span class="icon">📊</span>
          Token监控
        </span>
        <el-tag type="success" size="small">运行中</el-tag>
      </div>
    </template>
    
    <div v-loading="loading" class="widget-content">
      <div class="chain-status">
        <div class="chain-item" @click="handleChainClick('sol')">
          <div class="chain-icon">🟢</div>
          <div class="chain-info">
            <div class="chain-name">SOL链</div>
            <div class="chain-value">{{ stats.sol.monitorCount || 0 }} 个监控中</div>
          </div>
        </div>
        <div class="chain-item" @click="handleChainClick('bsc')">
          <div class="chain-icon">🟡</div>
          <div class="chain-info">
            <div class="chain-name">BSC链</div>
            <div class="chain-value">{{ stats.bsc.monitorCount || 0 }} 个监控中</div>
          </div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="today-stats">
        <div class="stat-item">
          <div class="stat-label">新增Token</div>
          <div class="stat-value">{{ stats.today.newTokens || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">触发预警</div>
          <div class="stat-value alert">{{ stats.today.alerts || 0 }}次</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">待处理</div>
          <div class="stat-value pending">{{ stats.today.pending || 0 }}个</div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <div class="widget-footer">
        <el-button text @click="handleViewDetail">查看详情</el-button>
        <el-button text @click="handleAddMonitor">新增监控</el-button>
        <el-button text @click="handleViewReport">查看报表</el-button>
      </div>
    </template>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTokenMonitorStats } from '@/api/dashboard/stats'

const router = useRouter()
const loading = ref(false)
const stats = ref({
  sol: { monitorCount: 0 },
  bsc: { monitorCount: 0 },
  today: { newTokens: 0, alerts: 0, pending: 0 }
})

const loadStats = async () => {
  loading.value = true
  try {
    const response = await getTokenMonitorStats()
    stats.value = response.data
  } catch (error) {
    console.error('加载统计失败:', error)
  } finally {
    loading.value = false
  }
}

const handleChainClick = (chain) => {
  router.push(`/crypto/token-monitor?chain=${chain}`)
}

const handleViewDetail = () => router.push('/crypto/token-monitor')
const handleAddMonitor = () => router.push('/crypto/token-monitor')
const handleViewReport = () => router.push('/crypto/reports')

onMounted(() => {
  loadStats()
  setInterval(loadStats, 30000)
})
</script>

<style scoped lang="scss">
.widget-card {
  height: 100%;
}

.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .widget-title {
    font-size: 16px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.chain-status {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  
  .chain-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    background-color: #F5F7FA;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background-color: #E6F7FF;
      transform: translateY(-2px);
    }
  }
}

.today-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  
  .stat-item {
    text-align: center;
    
    .stat-value {
      font-size: 24px;
      font-weight: 600;
      
      &.alert { color: #F56C6C; }
      &.pending { color: #E6A23C; }
    }
  }
}
</style>
```

---

## 4. WeChatBotWidget占位示例

**文件**：`src/views/dashboard/widgets/WeChatBotWidget.vue`

```vue
<template>
  <el-card class="widget-card coming-soon" shadow="hover">
    <template #header>
      <div class="widget-header">
        <span class="widget-title">
          <span class="icon">🤖</span>
          微信机器人
        </span>
        <el-tag type="info" size="small">即将上线</el-tag>
      </div>
    </template>
    
    <div class="widget-content">
      <div class="coming-soon-content">
        <el-icon class="construction-icon" :size="48">
          <Tools />
        </el-icon>
        <h3>⚙️ 功能开发中...</h3>
        <p class="desc">我们正在努力开发这个功能，敬请期待！</p>
      </div>
      
      <el-divider />
      
      <div class="planned-features">
        <h4>计划功能：</h4>
        <ul>
          <li>✅ 自动回复消息</li>
          <li>✅ 群消息管理</li>
          <li>✅ Token推送到微信群</li>
          <li>✅ 定时任务</li>
        </ul>
      </div>
    </div>
    
    <template #footer>
      <div class="widget-footer">
        <el-button disabled>开始配置</el-button>
        <el-button text>查看文档</el-button>
      </div>
    </template>
  </el-card>
</template>

<script setup>
import { Tools } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
.coming-soon {
  .coming-soon-content {
    text-align: center;
    padding: 20px 0;
    
    .construction-icon {
      color: #909399;
      margin-bottom: 16px;
    }
    
    h3 {
      margin: 0 0 8px 0;
      color: #606266;
    }
  }
  
  .planned-features {
    text-align: left;
    padding: 0 20px;
    
    ul {
      margin: 0;
      padding-left: 20px;
      
      li {
        margin-bottom: 8px;
        color: #606266;
      }
    }
  }
}
</style>
```

---

## 5. API接口示例

**文件**：`src/api/dashboard/config.js`

```javascript
import request from '@/utils/request'

export function getUserDashboardConfig() {
  return request({
    url: '/dashboard/config',
    method: 'get'
  })
}

export function updateUserDashboardConfig(data) {
  return request({
    url: '/dashboard/config',
    method: 'post',
    data: data
  })
}

export function resetUserDashboardConfig() {
  return request({
    url: '/dashboard/config/reset',
    method: 'post'
  })
}
```

**文件**：`src/api/dashboard/notification.js`

```javascript
import request from '@/utils/request'

export function getNotificationList(params) {
  return request({
    url: '/dashboard/notifications',
    method: 'get',
    params: params
  })
}

export function markAsRead(id) {
  return request({
    url: `/dashboard/notifications/${id}/read`,
    method: 'put'
  })
}

export function markAllAsRead() {
  return request({
    url: '/dashboard/notifications/read-all',
    method: 'put'
  })
}
```

---

## 6. 后端Controller示例

**文件**：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/dashboard/DashboardController.java`

```java
package com.ruoyi.web.controller.dashboard;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.UserDashboardConfig;
import com.ruoyi.system.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController extends BaseController {
    
    @Autowired
    private IDashboardService dashboardService;
    
    @GetMapping("/config")
    public AjaxResult getUserConfig() {
        Long userId = getUserId();
        List<UserDashboardConfig> list = dashboardService.getUserConfig(userId);
        return success(list);
    }
    
    @PostMapping("/config")
    public AjaxResult updateConfig(@RequestBody UserDashboardConfig config) {
        config.setUserId(getUserId());
        return toAjax(dashboardService.saveOrUpdateConfig(config));
    }
    
    @PostMapping("/config/reset")
    public AjaxResult resetConfig() {
        Long userId = getUserId();
        return toAjax(dashboardService.resetToDefault(userId));
    }
    
    @GetMapping("/stats/token-monitor")
    public AjaxResult getTokenMonitorStats() {
        Map<String, Object> stats = dashboardService.getTokenMonitorStats(getUserId());
        return success(stats);
    }
}
```

---

**文档结束**

> 更多代码示例请参考实际项目的 README.md 或在线文档
