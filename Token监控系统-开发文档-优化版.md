# Token监控系统 - 前端开发需求文档（优化版）

## 📋 项目概述

### 项目背景
本项目是一个Solana链上Token监控系统，后端已完成数据采集模块，实时采集来自Pump.fun和BONK平台的Token发射数据。现需要开发前端页面展示这些数据。

### 技术栈
- **前端框架**: RuoYi-Vue3
- **后端框架**: RuoYi (Spring Boot)
- **数据库**: MySQL 8.0
- **UI组件库**: Element Plus

### 模块说明
- **后端模块**: `ruoyi-crypto`
- **前端模块**: `RuoYi-Vue3`
- **菜单位置**: 独立一级菜单 - "代币监控"

---

## 🗄️ 数据库信息

### 连接信息
```yaml
Host: 47.106.217.116
Port: 3306
Database: crypto_web3
Username: admin
Password: Wy1997@Kakarot
```

### 核心数据表：`token_launch_history`

#### 表结构（实际数据库结构 + RuoYi标准字段）
```sql
-- 数据库表已存在，执行 sql/add_ruoyi_fields_to_token_launch_history.sql 添加标准字段
CREATE TABLE `token_launch_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ca` varchar(64) NOT NULL COMMENT '合约地址',
  `token_name` varchar(128) NULL DEFAULT NULL COMMENT 'Token名称',
  `token_symbol` varchar(32) NULL DEFAULT NULL COMMENT 'Token缩写符号',
  `twitter_url` varchar(255) NULL DEFAULT NULL COMMENT 'Twitter链接',
  `source` varchar(32) NOT NULL COMMENT '数据来源：pump，bonk, 后续可扩展',
  `launch_time` datetime NOT NULL COMMENT '发射时间/毕业时间(北京时间UTC+8)',
  `highest_market_cap` bigint NULL DEFAULT NULL COMMENT '历史最高市值(USD)，初始为空',
  `tg_msg_id` varchar(64) NULL DEFAULT NULL COMMENT 'TG消息ID（pump来源有，bonk来源为空）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ca_source`(`ca` ASC, `source` ASC) USING BTREE,
  INDEX `idx_ca`(`ca` ASC) USING BTREE,
  INDEX `idx_source`(`source` ASC) USING BTREE,
  INDEX `idx_launch_time`(`launch_time` DESC) USING BTREE,
  INDEX `idx_token_name`(`token_name` ASC) USING BTREE,
  INDEX `idx_market_cap`(`highest_market_cap` DESC) USING BTREE,
  INDEX `idx_source_time`(`source` ASC, `launch_time` DESC) USING BTREE,
  INDEX `idx_twitter_url`(`twitter_url` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 152309 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci 
COMMENT = 'Token发射历史数据表' ROW_FORMAT = Dynamic;
```

> **📌 重要提示**：
> - 执行 `sql/add_ruoyi_fields_to_token_launch_history.sql` 脚本添加RuoYi标准字段
> - 脚本会保留所有现有数据（15万+条记录）
> - `created_at` 字段保留，`create_time` 同步其值
> - 现有数据的 `create_by` 自动设置为 `system`（系统采集）

#### 字段说明

| 字段名 | 类型 | 说明 | 备注 |
|--------|------|------|------|
| id | BIGINT | 主键ID | 自增，当前到152309 |
| ca | VARCHAR(64) | 合约地址 | 必填，与source组成唯一索引 |
| token_name | VARCHAR(128) | Token名称 | 可为空 |
| token_symbol | VARCHAR(32) | Token符号 | 可为空 |
| twitter_url | VARCHAR(255) | Twitter链接 | 可为空 |
| source | VARCHAR(32) | 数据来源 | 必填，pump/bonk/可扩展 |
| launch_time | DATETIME | 发射时间 | 必填，北京时间(UTC+8) |
| highest_market_cap | BIGINT | 历史最高市值 | 可为空，单位USD |
| tg_msg_id | VARCHAR(64) | Telegram消息ID | 可为空，pump来源有值 |
| created_at | DATETIME | 入库时间 | 自动填充，数据采集时间 |
| del_flag | CHAR(1) | 删除标志 | RuoYi标准字段，0=存在 2=删除 |
| create_by | VARCHAR(64) | 创建者 | RuoYi标准字段，system=系统采集 |
| create_time | DATETIME | 创建时间 | RuoYi标准字段，同步created_at |
| update_by | VARCHAR(64) | 更新者 | RuoYi标准字段 |
| update_time | DATETIME | 更新时间 | RuoYi标准字段 |
| remark | VARCHAR(500) | 备注 | RuoYi标准字段，可扩展用途 |

#### 索引说明
- **主键索引**: `id`
- **唯一索引**: `uk_ca_source` (ca + source) - 同一来源的ca不重复
- **普通索引**: ca, source, launch_time, token_name, highest_market_cap, twitter_url
- **组合索引**: source + launch_time - 优化按来源和时间查询

---

## 🎯 功能需求

### 1. Token列表页面（核心功能）

#### 1.1 页面布局
```
┌─────────────────────────────────────────────────────────┐
│  🔍 代币监控                                             │
├─────────────────────────────────────────────────────────┤
│  统计卡片区域                                            │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐                 │
│  │ 📊   │ │ 🚀   │ │ 💎   │ │ 📅   │                 │
│  │ 总数 │ │ Pump │ │ BONK │ │ 今日 │                 │
│  │ 1234 │ │  850 │ │  384 │ │  25  │                 │
│  └──────┘ └──────┘ └──────┘ └──────┘                 │
├─────────────────────────────────────────────────────────┤
│  筛选条件区域                                            │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 数据来源: [全部▼] [Pump] [BONK]                 │   │
│  │ 时间范围: [今天] [近7天] [近30天] [自定义▼]     │   │
│  │ 关键词: [__________________] [🔍查询] [重置]   │   │
│  └─────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│  数据表格区域                                            │
│  ┌───────────────────────────────────────────────────┐ │
│  │序号│Token名称│符号│合约地址│来源│发射时间│Twitter│ │
│  ├───────────────────────────────────────────────────┤ │
│  │ 1  │ BTC    │BTC │ 7xK8...│pump│10-01 10:00│ 🔗 │ │
│  │ 2  │ ETH    │ETH │ 9mN3...│bonk│10-01 09:30│ 🔗 │ │
│  │ 3  │ SOL    │SOL │ 4pQ2...│pump│10-01 09:00│ -  │ │
│  │ ...│  ...   │... │  ...   │... │    ...    │... │ │
│  └───────────────────────────────────────────────────┘ │
│  [上一页] 1 2 3 4 5 ... 10 [下一页]  共200条            │
└─────────────────────────────────────────────────────────┘
```

#### 1.2 统计卡片

**四个统计卡片**（顶部显示）
1. **总数**：所有Token总数
2. **Pump**：来自Pump的Token数量
3. **BONK**：来自BONK的Token数量  
4. **今日**：今天新增的Token数量

**卡片样式**
- 带图标的统计卡片
- 数字大字体显示
- 不同颜色区分（蓝色、绿色、橙色、紫色）
- hover效果：轻微上浮

#### 1.3 筛选功能

**数据来源筛选**
- 全部（默认）
- Pump（pump.fun来源）
- BONK（letsbonk.fun来源）

**时间范围筛选**
- 今天（当天0点到当前时间，默认）
- 近7天
- 近30天
- 自定义时间范围（日期时间选择器）

**关键词搜索**
- 支持搜索：Token名称、Token符号、合约地址
- 模糊匹配
- 支持回车查询

#### 1.4 数据表格

**表格列定义**

| 列名 | 宽度 | 说明 | 显示效果 |
|------|------|------|----------|
| 序号 | 60px | 行号 | 1, 2, 3... |
| Token名称 | 150px | token_name | 文本显示，空值显示"-" |
| 符号 | 100px | token_symbol | 文本显示，空值显示"-" |
| 合约地址 | 180px | ca前8位...后4位 | 7xK8dF3a...bonk，点击复制完整地址 |
| 数据来源 | 100px | source | pump显示蓝色标签，bonk显示绿色标签 |
| 发射时间 | 160px | launch_time | 格式：MM-DD HH:mm |
| Twitter | 100px | twitter_url | 有值显示"🔗查看"链接，无值显示"-" |
| 市值 | 120px | highest_market_cap | 格式化显示，如$1,234，当前为0 |
| 入库时间 | 160px | created_at | 格式：MM-DD HH:mm，鼠标悬停显示完整时间 |

**交互功能**
- 合约地址：点击复制完整地址，复制后Toast提示"已复制到剪贴板"
- Twitter链接：点击跳转到Twitter（新窗口打开）
- 排序：支持按发射时间排序（默认降序）
- 空值处理：所有空值统一显示为"-"

#### 1.5 分页功能
- 每页显示：20条（可选10/20/50/100）
- 分页器位置：表格底部右侧
- 显示：当前页/总页数，总记录数

---

## 🔌 后端API接口定义

### 接口基础信息
- **Base URL**: `/crypto/token`
- **请求方式**: GET
- **数据格式**: JSON
- **响应格式**: RuoYi标准响应格式

### 1. Token列表查询

**接口地址**: `GET /crypto/token/list`

**请求参数**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| source | String | 否 | 数据来源 | all/pump/bonk，默认all |
| beginTime | String | 否 | 开始时间 | 2025-10-01 00:00:00 |
| endTime | String | 否 | 结束时间 | 2025-10-01 23:59:59 |
| keyword | String | 否 | 搜索关键词 | BTC |
| pageNum | Integer | 否 | 页码 | 1，默认1 |
| pageSize | Integer | 否 | 每页数量 | 20，默认20 |

**请求示例**
```
GET /crypto/token/list?source=pump&pageNum=1&pageSize=20&beginTime=2025-10-01 00:00:00&endTime=2025-10-01 23:59:59
```

**响应示例**
```json
{
  "code": 200,
  "msg": "查询成功",
  "rows": [
    {
      "id": 1,
      "ca": "7xK8dF3a1234567890abcdefghijk",
      "tokenName": "Bitcoin",
      "tokenSymbol": "BTC",
      "twitterUrl": "https://x.com/bitcoin",
      "source": "pump",
      "launchTime": "2025-10-01 10:00:00",
      "highestMarketCap": 0,
      "tgMsgId": "pump_123456",
      "createdAt": "2025-10-01 10:00:05"
    },
    {
      "id": 2,
      "ca": "9mN3kL2b9876543210zyxwvutsrqp",
      "tokenName": "Ethereum",
      "tokenSymbol": "ETH",
      "twitterUrl": null,
      "source": "bonk",
      "launchTime": "2025-10-01 09:30:00",
      "highestMarketCap": 0,
      "tgMsgId": "bonk_abcdef",
      "createdAt": "2025-10-01 09:30:03"
    }
  ],
  "total": 200
}
```

---

### 2. Token详情查询（可选）

**接口地址**: `GET /crypto/token/{ca}`

**路径参数**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| ca | String | 是 | 合约地址 | 7xK8dF3a1234567890abcdefghijk |

**请求示例**
```
GET /crypto/token/7xK8dF3a1234567890abcdefghijk
```

**响应示例**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "id": 1,
    "ca": "7xK8dF3a1234567890abcdefghijk",
    "tokenName": "Bitcoin",
    "tokenSymbol": "BTC",
    "twitterUrl": "https://x.com/bitcoin",
    "source": "pump",
    "launchTime": "2025-10-01 10:00:00",
    "highestMarketCap": 0,
    "tgMsgId": "pump_123456",
    "createdAt": "2025-10-01 10:00:05"
  }
}
```

---

### 3. 统计数据查询

**接口地址**: `GET /crypto/token/stats`

**请求参数**: 无

**请求示例**
```
GET /crypto/token/stats
```

**响应示例**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 1234,
    "pumpCount": 850,
    "bonkCount": 384,
    "todayCount": 25
  }
}
```

---

## 💻 后端开发指南（Java/Spring Boot）

### 文件结构
```
ruoyi-crypto/
├── src/main/java/com/ruoyi/crypto/
│   ├── domain/
│   │   └── TokenLaunchHistory.java          # 实体类
│   ├── mapper/
│   │   └── TokenLaunchHistoryMapper.java    # Mapper接口（已存在）
│   ├── service/
│   │   ├── ITokenLaunchHistoryService.java  # Service接口
│   │   └── impl/
│   │       └── TokenLaunchHistoryServiceImpl.java  # Service实现
│   └── controller/
│       └── TokenLaunchHistoryController.java # Controller
└── src/main/resources/mapper/crypto/
    └── TokenLaunchHistoryMapper.xml          # MyBatis XML
```

---

### 1. 实体类定义

**文件路径**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/domain/TokenLaunchHistory.java`

```java
package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Token发射历史对象 token_launch_history
 * 
 * @author ruoyi
 */
@Data
@ApiModel(value = "Token发射历史", description = "Token发射历史信息")
public class TokenLaunchHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    private Long id;

    /** 合约地址 */
    @Excel(name = "合约地址")
    @ApiModelProperty("合约地址")
    private String ca;

    /** Token名称 */
    @Excel(name = "Token名称")
    @ApiModelProperty("Token名称")
    private String tokenName;

    /** Token符号 */
    @Excel(name = "Token符号")
    @ApiModelProperty("Token符号")
    private String tokenSymbol;

    /** Twitter链接 */
    @Excel(name = "Twitter链接")
    @ApiModelProperty("Twitter链接")
    private String twitterUrl;

    /** 数据来源 */
    @Excel(name = "数据来源", readConverterExp = "pump=Pump.fun,bonk=LetsBonk")
    @ApiModelProperty("数据来源(pump/bonk)")
    private String source;

    /** 发射时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发射时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("发射时间(北京时间UTC+8)")
    private Date launchTime;

    /** 历史最高市值 */
    @Excel(name = "历史最高市值")
    @ApiModelProperty("历史最高市值(USD)")
    private Long highestMarketCap;

    /** TG消息ID */
    @Excel(name = "TG消息ID")
    @ApiModelProperty("Telegram消息ID")
    private String tgMsgId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("入库时间")
    private Date createdAt;
}
```

---

### 2. Mapper接口

**文件路径**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/mapper/TokenLaunchHistoryMapper.java`

```java
package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.TokenLaunchHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Token发射历史Mapper接口
 * 
 * @author ruoyi
 */
public interface TokenLaunchHistoryMapper {
    
    /**
     * 查询Token发射历史列表
     * 
     * @param token Token发射历史
     * @return Token发射历史集合
     */
    List<TokenLaunchHistory> selectTokenList(TokenLaunchHistory token);

    /**
     * 根据合约地址查询Token详情
     * 
     * @param ca 合约地址
     * @return Token发射历史
     */
    TokenLaunchHistory selectTokenByCa(@Param("ca") String ca);

    /**
     * 查询统计数据
     * 
     * @return 统计数据Map
     */
    Map<String, Object> selectTokenStats();
}
```

---

### 3. Mapper XML

**文件路径**: `ruoyi-crypto/src/main/resources/mapper/crypto/TokenLaunchHistoryMapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.crypto.mapper.TokenLaunchHistoryMapper">
    
    <resultMap type="TokenLaunchHistory" id="TokenLaunchHistoryResult">
        <id     property="id"                column="id"                    />
        <result property="ca"                column="ca"                    />
        <result property="tokenName"         column="token_name"            />
        <result property="tokenSymbol"       column="token_symbol"          />
        <result property="twitterUrl"        column="twitter_url"           />
        <result property="source"            column="source"                />
        <result property="launchTime"        column="launch_time"           />
        <result property="highestMarketCap"  column="highest_market_cap"    />
        <result property="tgMsgId"           column="tg_msg_id"             />
        <result property="createdAt"         column="created_at"            />
    </resultMap>

    <sql id="selectTokenVo">
        SELECT id, ca, token_name, token_symbol, twitter_url, source, 
               launch_time, highest_market_cap, tg_msg_id, created_at
        FROM token_launch_history
    </sql>

    <!-- 查询Token列表 -->
    <select id="selectTokenList" parameterType="TokenLaunchHistory" resultMap="TokenLaunchHistoryResult">
        <include refid="selectTokenVo"/>
        <where>  
            <if test="source != null and source != '' and source != 'all'">
                AND source = #{source}
            </if>
            <if test="params.beginTime != null and params.beginTime != ''">
                AND launch_time &gt;= #{params.beginTime}
            </if>
            <if test="params.endTime != null and params.endTime != ''">
                AND launch_time &lt;= #{params.endTime}
            </if>
            <if test="params.keyword != null and params.keyword != ''">
                AND (
                    token_name LIKE concat('%', #{params.keyword}, '%')
                    OR token_symbol LIKE concat('%', #{params.keyword}, '%')
                    OR ca LIKE concat('%', #{params.keyword}, '%')
                )
            </if>
        </where>
        ORDER BY launch_time DESC
    </select>

    <!-- 查询Token详情 -->
    <select id="selectTokenByCa" parameterType="String" resultMap="TokenLaunchHistoryResult">
        <include refid="selectTokenVo"/>
        WHERE ca = #{ca}
    </select>

    <!-- 查询统计数据 -->
    <select id="selectTokenStats" resultType="map">
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN source = 'pump' THEN 1 ELSE 0 END) as pumpCount,
            SUM(CASE WHEN source = 'bonk' THEN 1 ELSE 0 END) as bonkCount,
            SUM(CASE WHEN DATE(launch_time) = CURDATE() THEN 1 ELSE 0 END) as todayCount
        FROM token_launch_history
    </select>

</mapper>
```

---

### 4. Service接口

**文件路径**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/service/ITokenLaunchHistoryService.java`

```java
package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.TokenLaunchHistory;

import java.util.List;
import java.util.Map;

/**
 * Token发射历史Service接口
 * 
 * @author ruoyi
 */
public interface ITokenLaunchHistoryService {
    
    /**
     * 查询Token发射历史列表
     * 
     * @param token Token发射历史
     * @return Token发射历史集合
     */
    List<TokenLaunchHistory> selectTokenList(TokenLaunchHistory token);

    /**
     * 根据合约地址查询Token详情
     * 
     * @param ca 合约地址
     * @return Token发射历史
     */
    TokenLaunchHistory selectTokenByCa(String ca);

    /**
     * 查询统计数据
     * 
     * @return 统计数据Map
     */
    Map<String, Object> selectTokenStats();
}
```

---

### 5. Service实现类

**文件路径**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/service/impl/TokenLaunchHistoryServiceImpl.java`

```java
package com.ruoyi.crypto.service.impl;

import com.ruoyi.crypto.domain.TokenLaunchHistory;
import com.ruoyi.crypto.mapper.TokenLaunchHistoryMapper;
import com.ruoyi.crypto.service.ITokenLaunchHistoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Token发射历史Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class TokenLaunchHistoryServiceImpl implements ITokenLaunchHistoryService {
    
    @Resource
    private TokenLaunchHistoryMapper tokenMapper;

    @Override
    public List<TokenLaunchHistory> selectTokenList(TokenLaunchHistory token) {
        return tokenMapper.selectTokenList(token);
    }

    @Override
    public TokenLaunchHistory selectTokenByCa(String ca) {
        return tokenMapper.selectTokenByCa(ca);
    }

    @Override
    public Map<String, Object> selectTokenStats() {
        return tokenMapper.selectTokenStats();
    }
}
```

---

### 6. Controller控制器

**文件路径**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/controller/TokenLaunchHistoryController.java`

```java
package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.TokenLaunchHistory;
import com.ruoyi.crypto.service.ITokenLaunchHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Token发射历史Controller
 * 
 * @author ruoyi
 */
@Api(tags = "代币监控")
@RestController
@RequestMapping("/crypto/token")
public class TokenLaunchHistoryController extends BaseController {
    
    @Resource
    private ITokenLaunchHistoryService tokenService;

    /**
     * 查询Token发射历史列表
     */
    @ApiOperation("查询Token列表")
    @PreAuthorize("@ss.hasPermi('crypto:token:list')")
    @GetMapping("/list")
    public TableDataInfo list(TokenLaunchHistory token) {
        startPage();
        List<TokenLaunchHistory> list = tokenService.selectTokenList(token);
        return getDataTable(list);
    }

    /**
     * 查询Token详情
     */
    @ApiOperation("查询Token详情")
    @PreAuthorize("@ss.hasPermi('crypto:token:query')")
    @GetMapping("/{ca}")
    public AjaxResult getInfo(
            @ApiParam(value = "合约地址", required = true) 
            @PathVariable("ca") String ca) {
        return success(tokenService.selectTokenByCa(ca));
    }

    /**
     * 查询统计数据
     */
    @ApiOperation("查询统计数据")
    @PreAuthorize("@ss.hasPermi('crypto:token:list')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        Map<String, Object> stats = tokenService.selectTokenStats();
        return success(stats);
    }
}
```

---

## 🎨 前端开发指南（Vue3 + Element Plus）

### 文件结构
```
RuoYi-Vue3/
├── src/
│   ├── api/crypto/
│   │   └── token.js                    # API接口封装
│   ├── views/crypto/
│   │   └── tokenMonitor/
│   │       └── index.vue               # Token监控页面
│   └── router/
│       └── modules/
│           └── crypto.js               # 路由配置（修改）
```

---

### 1. 页面路由配置

**文件路径**: `RuoYi-Vue3/src/router/modules/crypto.js`

**在现有crypto.js文件中添加子路由**：

```javascript
{
  path: 'tokenMonitor',
  component: () => import('@/views/crypto/tokenMonitor/index'),
  name: 'TokenMonitor',
  meta: { title: '代币监控', icon: 'monitor' }
}
```

**完整示例**（在children数组中添加）：
```javascript
import Layout from '@/layout'

export default {
  path: '/crypto',
  component: Layout,
  redirect: '/crypto/caRecord',
  name: 'Crypto',
  meta: {
    title: '加密货币管理',
    icon: 'money'
  },
  children: [
    {
      path: 'caRecord',
      component: () => import('@/views/crypto/caRecord/index'),
      name: 'CryptoCaRecord',
      meta: { title: 'CA记录', icon: 'documentation' }
    },
    // ... 其他路由 ...
    {
      path: 'tokenMonitor',
      component: () => import('@/views/crypto/tokenMonitor/index'),
      name: 'TokenMonitor',
      meta: { title: '代币监控', icon: 'monitor' }
    }
  ]
}
```

---

### 2. API接口封装

**文件路径**: `RuoYi-Vue3/src/api/crypto/token.js`

```javascript
import request from '@/utils/request'

// 查询Token列表
export function listToken(query) {
  return request({
    url: '/crypto/token/list',
    method: 'get',
    params: query
  })
}

// 查询Token详情
export function getToken(ca) {
  return request({
    url: '/crypto/token/' + ca,
    method: 'get'
  })
}

// 查询统计数据
export function getTokenStats() {
  return request({
    url: '/crypto/token/stats',
    method: 'get'
  })
}
```

---

### 3. Token监控页面组件

**文件路径**: `RuoYi-Vue3/src/views/crypto/tokenMonitor/index.vue`

```vue
<template>
  <div class="app-container token-monitor">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row" v-loading="statsLoading">
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card total-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="stats-text">
              <div class="stats-label">总数</div>
              <div class="stats-value">{{ stats.total || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card pump-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stats-text">
              <div class="stats-label">Pump</div>
              <div class="stats-value">{{ stats.pumpCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card bonk-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="stats-text">
              <div class="stats-label">BONK</div>
              <div class="stats-value">{{ stats.bonkCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stats-card today-card">
          <div class="stats-content">
            <div class="stats-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stats-text">
              <div class="stats-label">今日</div>
              <div class="stats-value">{{ stats.todayCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
      <el-form-item label="数据来源" prop="source">
        <el-select v-model="queryParams.source" placeholder="请选择" clearable style="width: 150px">
          <el-option label="全部" value="all" />
          <el-option label="Pump" value="pump" />
          <el-option label="BONK" value="bonk" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="时间范围">
        <el-radio-group v-model="timeRange" @change="handleTimeRangeChange">
          <el-radio-button label="today">今天</el-radio-button>
          <el-radio-button label="week">近7天</el-radio-button>
          <el-radio-button label="month">近30天</el-radio-button>
          <el-radio-button label="custom">自定义</el-radio-button>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item v-if="timeRange === 'custom'" label="">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 380px"
        />
      </el-form-item>
      
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="名称/符号/合约地址"
          clearable
          style="width: 240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table v-loading="loading" :data="tokenList" border stripe>
      <el-table-column type="index" label="序号" width="60" align="center" />
      
      <el-table-column label="Token名称" prop="tokenName" width="150" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.tokenName || '-' }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="符号" prop="tokenSymbol" width="100" align="center">
        <template #default="scope">
          <span>{{ scope.row.tokenSymbol || '-' }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="合约地址" prop="ca" width="180" show-overflow-tooltip>
        <template #default="scope">
          <el-tooltip :content="scope.row.ca" placement="top">
            <el-link type="primary" @click="copyText(scope.row.ca)" :underline="false">
              {{ formatAddress(scope.row.ca) }}
              <el-icon><DocumentCopy /></el-icon>
            </el-link>
          </el-tooltip>
        </template>
      </el-table-column>
      
      <el-table-column label="数据来源" prop="source" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.source === 'pump'" type="primary">Pump</el-tag>
          <el-tag v-else-if="scope.row.source === 'bonk'" type="success">BONK</el-tag>
          <el-tag v-else type="info">{{ scope.row.source }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="发射时间" prop="launchTime" width="160" align="center" sortable>
        <template #default="scope">
          {{ formatTime(scope.row.launchTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="Twitter" prop="twitterUrl" width="100" align="center">
        <template #default="scope">
          <el-link 
            v-if="scope.row.twitterUrl" 
            type="primary" 
            :href="scope.row.twitterUrl" 
            target="_blank"
            :underline="false"
          >
            <el-icon><Link /></el-icon> 查看
          </el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      
      <el-table-column label="市值" prop="highestMarketCap" width="120" align="right">
        <template #default="scope">
          {{ formatMarketCap(scope.row.highestMarketCap) }}
        </template>
      </el-table-column>
      
      <el-table-column label="入库时间" prop="createdAt" width="160" align="center" show-overflow-tooltip>
        <template #default="scope">
          <el-tooltip :content="scope.row.createdAt" placement="top">
            <span>{{ formatTime(scope.row.createdAt) }}</span>
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="TokenMonitor">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { listToken, getTokenStats } from '@/api/crypto/token'
import { ElMessage } from 'element-plus'
import { 
  DataAnalysis, 
  TrendCharts, 
  Coin, 
  Calendar,
  DocumentCopy,
  Link 
} from '@element-plus/icons-vue'

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  source: 'all',
  keyword: null
})

// 数据
const tokenList = ref([])
const total = ref(0)
const loading = ref(false)
const statsLoading = ref(false)
const stats = ref({
  total: 0,
  pumpCount: 0,
  bonkCount: 0,
  todayCount: 0
})
const timeRange = ref('today')
const dateRange = ref([])

// 定时刷新
let refreshInterval = null

// 时间范围变化
const handleTimeRangeChange = (val) => {
  const now = new Date()
  let start, end
  
  if (val === 'today') {
    start = new Date(now.getFullYear(), now.getMonth(), now.getDate())
    end = now
  } else if (val === 'week') {
    start = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
    end = now
  } else if (val === 'month') {
    start = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
    end = now
  } else {
    dateRange.value = []
    return
  }
  
  dateRange.value = [
    formatDateTime(start),
    formatDateTime(end)
  ]
}

// 格式化日期时间
const formatDateTime = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 查询列表
const getList = () => {
  loading.value = true
  const params = { ...queryParams }
  
  // 添加时间范围
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  
  listToken(params).then(response => {
    tokenList.value = response.rows
    total.value = response.total
  }).catch(error => {
    ElMessage.error('查询失败：' + (error.message || '未知错误'))
  }).finally(() => {
    loading.value = false
  })
}

// 获取统计数据
const getStats = () => {
  statsLoading.value = true
  getTokenStats().then(response => {
    stats.value = response.data
  }).catch(error => {
    ElMessage.error('统计数据获取失败：' + (error.message || '未知错误'))
  }).finally(() => {
    statsLoading.value = false
  })
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.source = 'all'
  queryParams.keyword = null
  timeRange.value = 'today'
  handleTimeRangeChange('today')
  handleQuery()
}

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '-'
  if (address.length <= 12) return address
  return `${address.substring(0, 8)}...${address.substring(address.length - 4)}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  // 格式：MM-DD HH:mm
  return time.substring(5, 16)
}

// 格式化市值
const formatMarketCap = (value) => {
  if (!value || value === 0) return '$0'
  return '$' + value.toLocaleString()
}

// 复制文本
const copyText = (text) => {
  if (!navigator.clipboard) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('已复制到剪贴板')
    } catch (err) {
      ElMessage.error('复制失败')
    }
    document.body.removeChild(textarea)
    return
  }
  
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 自动刷新数据（每30秒）
const startAutoRefresh = () => {
  refreshInterval = setInterval(() => {
    getList()
    getStats()
  }, 30000) // 30秒
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

// 初始化
onMounted(() => {
  handleTimeRangeChange('today')
  getList()
  getStats()
  startAutoRefresh()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">
.token-monitor {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stats-card {
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.stats-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stats-icon {
  font-size: 48px;
  margin-right: 20px;
  
  .el-icon {
    width: 48px;
    height: 48px;
  }
}

.stats-text {
  flex: 1;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stats-value {
  font-size: 28px;
  font-weight: bold;
}

.total-card {
  .stats-icon { color: #409EFF; }
  .stats-value { color: #409EFF; }
}

.pump-card {
  .stats-icon { color: #67C23A; }
  .stats-value { color: #67C23A; }
}

.bonk-card {
  .stats-icon { color: #E6A23C; }
  .stats-value { color: #E6A23C; }
}

.today-card {
  .stats-icon { color: #F56C6C; }
  .stats-value { color: #F56C6C; }
}

.el-form {
  margin-top: 20px;
}

.el-table {
  margin-top: 20px;
}
</style>
```

---

## 📝 开发清单

### 后端开发任务
- [ ] 创建实体类 `TokenLaunchHistory.java`
- [ ] Mapper接口已存在，检查是否需要修改
- [ ] 创建Mapper XML `TokenLaunchHistoryMapper.xml`
- [ ] 创建Service接口 `ITokenLaunchHistoryService.java`
- [ ] 创建Service实现类 `TokenLaunchHistoryServiceImpl.java`
- [ ] 创建Controller `TokenLaunchHistoryController.java`
- [ ] 测试所有API接口

### 前端开发任务
- [ ] 创建API接口文件 `src/api/crypto/token.js`
- [ ] 创建监控页面 `src/views/crypto/tokenMonitor/index.vue`
- [ ] 修改路由配置 `src/router/modules/crypto.js`
- [ ] 测试页面功能
- [ ] 优化样式和交互

### 权限配置任务
- [ ] 在系统管理-菜单管理中添加菜单
  - 菜单名称：代币监控
  - 权限标识：crypto:token:list
  - 路由地址：tokenMonitor
  - 组件路径：crypto/tokenMonitor/index
- [ ] 配置按钮权限
  - 查询：crypto:token:list
  - 详情：crypto:token:query

---

## 🧪 测试要点

### 功能测试
1. **列表查询**
   - ✅ 默认查询（今天数据）
   - ✅ 分页功能
   - ✅ 数据显示正确

2. **筛选功能**
   - ✅ 来源筛选：全部/Pump/BONK
   - ✅ 时间筛选：今天/近7天/近30天/自定义
   - ✅ 关键词搜索：名称/符号/地址

3. **交互功能**
   - ✅ 合约地址复制
   - ✅ Twitter链接跳转
   - ✅ 统计数据正确性
   - ✅ 自动刷新（30秒）

4. **边界测试**
   - ✅ 空数据处理
   - ✅ 空值显示为"-"
   - ✅ 长地址省略显示
   - ✅ 错误提示

### 性能测试
- 列表加载速度（建议<1秒）
- 大数据量分页（测试1000+条数据）
- 搜索响应速度
- 自动刷新性能

### 兼容性测试
- Chrome浏览器
- Edge浏览器
- Firefox浏览器

---

## 📞 常见问题

### Q: 为什么市值都是0？
A: 当前阶段只采集发射数据，市值监控功能将在后续版本实现。

### Q: Twitter字段为什么有些是空的？
A: 部分Token在发射时未提供Twitter链接，或者是异常链接被过滤掉了。

### Q: 时间是什么时区？
A: 所有时间都是北京时间（UTC+8）。

### Q: 数据多久更新一次？
A: Pump数据实时采集，BONK数据每30秒轮询一次。前端页面每30秒自动刷新。

### Q: 为什么使用ca而不是address？
A: 为保持与后端数据采集系统的一致性，合约地址字段名保持为ca。

---

## 📄 附录

### 数据示例

**Token记录示例**
```json
{
  "id": 123,
  "ca": "7xK8dF3a1234567890abcdefghijklmnopqrstuvwxyz",
  "tokenName": "Solana Inu",
  "tokenSymbol": "SOLINU",
  "twitterUrl": "https://x.com/solana_inu",
  "source": "pump",
  "launchTime": "2025-10-01 14:30:25",
  "highestMarketCap": 0,
  "tgMsgId": "pump_987654",
  "createdAt": "2025-10-01 14:30:28"
}
```

### 优化建议（后续版本）
1. 添加实时市值更新功能
2. 添加Token详情页面
3. 添加收藏功能
4. 添加导出功能
5. 添加高级筛选（市值范围等）
6. 添加图表展示（发射趋势）
7. 添加WebSocket实时推送

---

## 📋 文档版本

- **版本号**: v2.0（优化版）
- **更新时间**: 2025-10-02
- **更新内容**:
  - ✅ 调整模块为ruoyi-crypto
  - ✅ API路径统一为/crypto/token
  - ✅ 保持ca字段名
  - ✅ 添加前端自动刷新
  - ✅ 完善错误处理
  - ✅ 优化UI交互
  - ✅ 补充权限配置说明

---

**注意事项**：
1. 数据库表 `token_launch_history` 已存在于 `crypto_web3` 库中，已添加RuoYi标准字段
2. 后端代码放在 `ruoyi-crypto` 模块
3. 前端代码放在 `RuoYi-Vue3` 模块
4. API路径统一使用 `/crypto/token`
5. 合约地址字段保持 `ca` 命名
6. 前端菜单作为独立一级菜单"代币监控"

---

## 🚀 开发步骤指南

### 📌 前提条件检查
- [x] 数据库表 `token_launch_history` 已存在且有数据（15万+条）
- [x] 已执行 `sql/add_ruoyi_fields_to_token_launch_history.sql` 添加标准字段
- [x] 项目结构：`ruoyi-crypto`（后端）+ `RuoYi-Vue3`（前端）
- [x] Mapper接口 `TokenLaunchHistoryMapper.java` 已存在

---

### 第一阶段：后端开发（预计2-3小时）

#### Step 1: 创建实体类（10分钟）
📁 **文件**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/domain/TokenLaunchHistory.java`

```bash
# 位置：ruoyi-crypto模块
# 参考文档第310-408行的实体类定义
# 关键点：
# - 继承 BaseEntity
# - 使用 @Data 注解（Lombok）
# - 字段名与数据库对应（驼峰命名）
# - 添加 @Excel 和 @ApiModelProperty 注解
```

**核心字段映射**：
- 数据库 `ca` → Java `ca`
- 数据库 `token_name` → Java `tokenName`
- 数据库 `created_at` → Java `createdAt`

---

#### Step 2: 创建 Mapper XML（20分钟）
📁 **文件**: `ruoyi-crypto/src/main/resources/mapper/crypto/TokenLaunchHistoryMapper.xml`

```bash
# 参考文档第460-550行
# 参考现有文件：CryptoWalletMapper.xml
# 关键SQL：
# 1. selectTokenList - 列表查询（支持筛选、分页、排序）
# 2. selectTokenByCa - 详情查询
# 3. selectTokenStats - 统计查询
```

**SQL编写要点**：
```xml
<!-- 列表查询需要支持的条件 -->
- source 筛选（全部/pump/bonk）
- 时间范围筛选（beginTime/endTime）
- 关键词搜索（token_name/token_symbol/ca）
- 排序：launch_time DESC
- 逻辑删除：del_flag = '0'
```

---

#### Step 3: 创建 Service 接口和实现（15分钟）
📁 **文件1**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/service/ITokenLaunchHistoryService.java`
📁 **文件2**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/service/impl/TokenLaunchHistoryServiceImpl.java`

```bash
# 参考文档第552-625行
# 参考现有文件：CryptoWalletService 和 CryptoWalletServiceImpl
# 三个核心方法：
# - selectTokenList(TokenLaunchHistory token)
# - selectTokenByCa(String ca)
# - selectTokenStats()
```

---

#### Step 4: 创建 Controller（20分钟）
📁 **文件**: `ruoyi-crypto/src/main/java/com/ruoyi/crypto/controller/TokenLaunchHistoryController.java`

```bash
# 参考文档第627-717行
# 参考现有文件：CryptoWalletController.java
# 关键点：
# - @RestController + @RequestMapping("/crypto/token")
# - 继承 BaseController
# - 使用 @PreAuthorize 权限注解
# - 三个接口：/list, /{ca}, /stats
```

**权限字符串定义**：
```java
@PreAuthorize("@ss.hasPermi('crypto:token:list')")   // 列表
@PreAuthorize("@ss.hasPermi('crypto:token:query')")  // 详情
@PreAuthorize("@ss.hasPermi('crypto:token:list')")   // 统计（复用list权限）
```

---

#### Step 5: 测试后端接口（30分钟）

**使用Postman或浏览器测试**：

1. **测试统计接口**（最简单）
```bash
GET http://localhost:8080/crypto/token/stats
# 预期返回：total, pumpCount, bonkCount, todayCount
```

2. **测试列表接口**
```bash
GET http://localhost:8080/crypto/token/list?pageNum=1&pageSize=10
# 预期返回：rows数组 + total总数
```

3. **测试筛选功能**
```bash
# 按来源筛选
GET http://localhost:8080/crypto/token/list?source=pump

# 按时间筛选
GET http://localhost:8080/crypto/token/list?beginTime=2025-10-01 00:00:00&endTime=2025-10-02 23:59:59

# 关键词搜索
GET http://localhost:8080/crypto/token/list?keyword=BTC
```

4. **测试详情接口**
```bash
# 使用数据库中实际存在的ca地址
GET http://localhost:8080/crypto/token/{实际的ca地址}
```

---

### 第二阶段：前端开发（预计3-4小时）

#### Step 6: 创建 API 接口文件（10分钟）
📁 **文件**: `RuoYi-Vue3/src/api/crypto/token.js`

```bash
# 参考文档第742-769行
# 参考现有文件：src/api/crypto/wallet.js
# 三个API函数：
# - listToken(query) - 列表查询
# - getToken(ca) - 详情查询  
# - getTokenStats() - 统计数据
```

---

#### Step 7: 配置前端路由（10分钟）
📁 **文件**: `RuoYi-Vue3/src/router/modules/crypto.js`

```javascript
// 在 children 数组中添加（放在第一个位置）
{
  path: 'token',
  component: () => import('@/views/crypto/token/index'),
  name: 'TokenLaunch',
  meta: { title: 'Token监控', icon: 'chart' }
}
```

**或者创建独立路由文件**：
📁 **文件**: `RuoYi-Vue3/src/router/modules/token.js`（推荐）

```javascript
// 创建独立的一级菜单
export default {
  path: '/token',
  component: Layout,
  redirect: '/token/index',
  meta: { title: '代币监控', icon: 'monitor' },
  children: [
    {
      path: 'index',
      component: () => import('@/views/crypto/token/index'),
      name: 'TokenMonitor',
      meta: { title: 'Token监控', icon: 'chart' }
    }
  ]
}
```

然后在 `src/router/index.js` 中导入：
```javascript
import tokenRouter from './modules/token'

export const constantRoutes = [
  // ...
  tokenRouter  // 添加这一行
]
```

---

#### Step 8: 创建前端页面（2-3小时）
📁 **文件**: `RuoYi-Vue3/src/views/crypto/token/index.vue`

**开发顺序**：

**8.1 先搭建基础框架（30分钟）**
```vue
<template>
  <div class="app-container">
    <!-- 1. 统计卡片区域 -->
    <!-- 2. 查询表单区域 -->
    <!-- 3. 数据表格区域 -->
    <!-- 4. 分页组件 -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { listToken, getTokenStats } from '@/api/crypto/token'

// 定义数据变量
const tokenList = ref([])
const total = ref(0)
const loading = ref(false)
const stats = ref({})
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  source: 'all',
  keyword: null
})

// 核心方法
const getList = () => { }
const getStats = () => { }
const handleQuery = () => { }
const resetQuery = () => { }

// 初始化
onMounted(() => {
  getList()
  getStats()
})
</script>
```

**8.2 实现统计卡片（30分钟）**
- 参考文档第817-862行
- 4个统计卡片：总数、Pump、BONK、今日
- 使用 Element Plus 的 `el-card` + `el-row` + `el-col`
- 添加图标和样式

**8.3 实现筛选表单（30分钟）**
- 参考文档第864-905行
- 数据来源下拉框（全部/Pump/BONK）
- 时间范围单选按钮组 + 日期选择器
- 关键词搜索框
- 查询/重置按钮

**8.4 实现数据表格（1小时）**
- 参考文档第907-996行
- 序号、Token名称、符号、合约地址、来源、发射时间、Twitter、市值
- 合约地址显示：前8位...后4位，点击复制
- 来源显示：标签（pump=蓝色，bonk=绿色）
- Twitter：有值显示链接，无值显示"-"
- 时间格式：MM-DD HH:mm:ss

**8.5 实现工具函数（30分钟）**
```javascript
// 格式化地址
const formatAddress = (address) => {
  if (!address) return '-'
  return `${address.substring(0, 8)}...${address.substring(address.length - 4)}`
}

// 复制文本
const copyText = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return time.substring(5, 19) // MM-DD HH:mm:ss
}

// 格式化市值
const formatMarketCap = (value) => {
  if (!value || value === 0) return '$0'
  return '$' + value.toLocaleString()
}
```

---

#### Step 9: 配置权限（15分钟）

登录RuoYi后台系统配置菜单权限：

1. **访问系统管理 → 菜单管理**
2. **添加一级菜单**：
   - 菜单名称：代币监控
   - 路由地址：token
   - 组件路径：Layout
   - 菜单图标：monitor

3. **添加子菜单**：
   - 菜单名称：Token监控
   - 路由地址：index
   - 组件路径：crypto/token/index
   - 权限字符：crypto:token:list

4. **添加按钮权限**（可选）：
   - 查询：crypto:token:query
   - 导出：crypto:token:export

---

#### Step 10: 测试前端功能（30分钟）

**测试清单**：
- [x] 统计卡片数据正确显示
- [x] 列表数据加载正常
- [x] 分页功能正常
- [x] 来源筛选（全部/Pump/BONK）
- [x] 时间范围筛选（今天/7天/30天/自定义）
- [x] 关键词搜索
- [x] 合约地址复制功能
- [x] Twitter链接跳转
- [x] 表格排序
- [x] 样式美观

---

### 第三阶段：优化和调试（预计1小时）

#### Step 11: 性能优化
1. **后端优化**
   - 确认SQL索引使用情况
   - 大数据量分页性能测试
   - 添加查询缓存（可选）

2. **前端优化**
   - 添加loading状态
   - 防抖处理（搜索框）
   - 懒加载（虚拟滚动，可选）

#### Step 12: 错误处理
```javascript
// API调用添加错误处理
const getList = () => {
  loading.value = true
  listToken(queryParams).then(response => {
    tokenList.value = response.rows
    total.value = response.total
  }).catch(error => {
    ElMessage.error('数据加载失败：' + error.message)
  }).finally(() => {
    loading.value = false
  })
}
```

#### Step 13: 用户体验优化
- 添加空状态提示
- 添加骨架屏
- 添加数据刷新按钮
- 优化移动端适配

---

## 📊 开发进度跟踪

### 后端开发
- [ ] TokenLaunchHistory.java（实体类）
- [ ] TokenLaunchHistoryMapper.xml（MyBatis配置）
- [ ] ITokenLaunchHistoryService.java（Service接口）
- [ ] TokenLaunchHistoryServiceImpl.java（Service实现）
- [ ] TokenLaunchHistoryController.java（Controller）
- [ ] 接口测试（Postman）

### 前端开发
- [ ] token.js（API接口）
- [ ] 路由配置
- [ ] index.vue（页面组件）
  - [ ] 统计卡片
  - [ ] 筛选表单
  - [ ] 数据表格
  - [ ] 分页组件
  - [ ] 工具函数
  - [ ] 样式美化
- [ ] 权限配置
- [ ] 功能测试

### 优化和部署
- [ ] 性能优化
- [ ] 错误处理
- [ ] 用户体验优化
- [ ] 文档整理

---

## 💡 开发建议

### 1. 按顺序开发
严格按照 Step 1 → Step 13 的顺序开发，不要跳步。每完成一步都要测试验证。

### 2. 参考现有代码
- **后端参考**: `CryptoWalletController.java`, `CryptoWalletService.java`
- **前端参考**: `src/views/crypto/wallet/index.vue`
- 复制代码结构，修改业务逻辑

### 3. 遇到问题优先查看
1. 浏览器控制台（F12）查看错误信息
2. IDEA控制台查看后端日志
3. 检查API路径是否正确
4. 检查数据库连接是否正常
5. 检查权限配置是否正确

### 4. 版本控制
建议使用Git管理代码，每完成一个Step提交一次：
```bash
git add .
git commit -m "feat: 完成TokenLaunchHistory实体类"
```

### 5. 代码规范
- Java：遵循阿里巴巴Java开发规范
- Vue：遵循Vue3 + Composition API规范
- 注释：关键代码必须添加注释

---

## ⏱️ 预计开发时间

| 阶段 | 任务 | 预计时间 |
|------|------|---------|
| **后端** | 实体类 + Mapper + Service + Controller | 2-3小时 |
| **前端** | API + 路由 + 页面组件 | 3-4小时 |
| **优化** | 测试 + 优化 + 调试 | 1小时 |
| **总计** | | **6-8小时** |

---

## 🎯 第一步建议

**现在就开始第一步**：创建 `TokenLaunchHistory.java` 实体类

1. 在IDEA中打开 `ruoyi-crypto` 模块
2. 找到 `src/main/java/com/ruoyi/crypto/domain/` 目录
3. 右键 → New → Java Class → 输入 `TokenLaunchHistory`
4. 复制文档第337-408行的代码
5. 导入缺失的依赖（Alt + Enter）
6. 保存文件

**完成后继续第二步**：创建 `TokenLaunchHistoryMapper.xml`

按照这个节奏，一步一步来，6-8小时内可以完成整个功能！💪

---

## 🎉 功能完成总结（2025-01-25）

### ✅ 已完成的核心功能

#### 1. **Token监控页面基础功能**
- ✅ Token列表查询展示
- ✅ 分页功能（每页10/20/50/100条）
- ✅ 数据自动刷新（60秒）
- ✅ 合约地址复制功能
- ✅ 表格列显示/隐藏控制

#### 2. **高级筛选功能**
- ✅ **数据来源筛选**：全部/Pump/BONK
- ✅ **时间范围筛选**：自定义日期时间范围
- ✅ **关键词搜索**：支持Token名称、符号、合约地址模糊搜索
- ✅ **监控状态筛选**：全部/已监控/未监控
- ✅ **Twitter类型筛选**：全部/推特主页/推文/社区/无推特
- ✅ **市值筛选**：全部/≥30万USD/≥50万USD/≥100万USD
- ✅ **关注状态筛选**：全部/已关注/未关注

#### 3. **Twitter集成功能**
- ✅ **Twitter账号识别**：自动区分推特主页、推文、社区链接
- ✅ **单个关注/取消关注**：表格行内快速操作
- ✅ **批量关注**：支持批量关注多个推特主页
- ✅ **批量取消关注**：支持批量取消关注
- ✅ **关注状态同步**：实时显示关注状态（已关注/未关注）
- ✅ **Twitter推送配置**：
  - 关注推送
  - 推文推送
  - 转发推送
  - 回复推送
  - 头像更换推送
  - 通知方式选择（Telegram/微信）
  - 推送状态启用/停用

#### 4. **数据库架构优化**
- ✅ **twitter_account_manage 表**：专门管理Twitter账号
  - 存储Twitter URL、用户名、类型
  - 记录关注状态和时间
  - 存储推送配置（5种推送类型）
  - 支持user_id字段（预留用于精确操作）
  - 同步状态管理（sync_status, retry_count, last_sync_time）
- ✅ **token_monitor_config 表**：专门管理Token监控配置
  - 支持定时提醒模式
  - 支持价格触发模式
  - 支持事件监控模式
- ✅ **数据迁移SQL**：将79,015条Twitter数据从token_launch_history迁移到新表

#### 5. **API接口完整实现**

**Token相关接口**：
- `GET /crypto/token/list` - Token列表查询（支持7种筛选条件）
- `GET /crypto/token/{ca}` - Token详情查询
- `GET /crypto/token/stats` - 统计数据查询

**Twitter相关接口**：
- `POST /crypto/token/follow` - 单个关注
- `POST /crypto/token/unfollow` - 单个取消关注
- `POST /crypto/token/batchFollow` - 批量关注
- `POST /crypto/token/batchUnfollow` - 批量取消关注
- `POST /crypto/token/getTwitterAccounts` - 批量获取Twitter账号信息
- `GET /crypto/token/getPushConfig` - 获取推送配置
- `POST /crypto/token/updatePushConfig` - 更新推送配置

**监控配置接口**（已创建，前端未完全对接）：
- `GET /crypto/tokenMonitor/list` - 监控配置列表
- `POST /crypto/tokenMonitor` - 新增监控配置
- `PUT /crypto/tokenMonitor` - 更新监控配置
- `DELETE /crypto/tokenMonitor/{ids}` - 删除监控配置
- `PUT /crypto/tokenMonitor/batchEnable` - 批量启用监控
- `PUT /crypto/tokenMonitor/batchDisable` - 批量停用监控

#### 6. **Twitter API集成**
- ✅ OkHttp客户端封装
- ✅ 完整的HTTP Header配置
- ✅ JSON响应解析（支持gzip压缩）
- ✅ 错误处理（识别"user already exist"为成功）
- ✅ 批量操作延迟控制（500ms间隔）

#### 7. **前端交互优化**
- ✅ Loading状态显示
- ✅ 按钮智能启用/禁用
- ✅ 友好的提示信息
- ✅ 对话框表单验证
- ✅ 行选择状态管理
- ✅ 时间格式化显示
- ✅ 市值格式化（K/M单位）

#### 8. **Python批量关注脚本**
- ✅ `scripts/twitter_batch_follow.py`
- ✅ 支持批量关注所有推特主页
- ✅ 错误处理和状态标记
- ✅ 进度显示和日志记录

---

### ⚠️ 待完成功能

#### 1. **监控配置功能**（前端未对接）
- ⏳ 单个Token监控配置保存（对话框已创建，API未调用）
- ⏳ 批量启用监控（按钮已创建，API未调用）
- ⏳ 批量取消监控（按钮已创建，API未调用）

**需要做的**：
```javascript
// 在 tokenMonitor/index.vue 中导入监控配置API
import { 
  addMonitorConfig, 
  updateMonitorConfig,
  batchEnableMonitor,
  batchDisableMonitor 
} from '@/api/crypto/monitor'

// 实现handleMonitorSave、handleBatchMonitor、handleBatchCancelMonitor方法
```

#### 2. **Twitter user_id同步**（已预留字段）
- ⏳ 获取已关注列表API（需要 `/user/follows` 接口）
- ⏳ 同步user_id到数据库
- ⏳ 定时任务同步

**说明**：已在数据库表中预留 `twitter_user_id`、`sync_status`、`retry_count`、`last_sync_time` 字段，等待Twitter API接口提供后实现。

#### 3. **Twitter推送配置定时任务**（待开发） ⚠️

**当前状态**：
- ✅ 前端UI已完成（推送配置对话框）
- ✅ 数据库保存已完成（twitter_account_manage表）
- ⚠️ **定时任务未实现**：需要定时扫描enable_xx_push=1的记录并调用Twitter API

**实现方案**：
```
用户配置推送流程（已确定）：
1. 用户在前端配置推送选项（关注/推文/转发/回复/头像）
2. 点击保存 → 更新数据库（enable_follow_push等字段）
3. 定时任务（例如每分钟）扫描数据库
4. 找出enable_xx_push=1且sync_status=0的记录
5. 调用Twitter API配置推送订阅
6. 更新sync_status字段标记同步状态
```

**需要调用的Twitter API**（明天重点任务）：

| API | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 关注推送订阅 | POST | `/api/v1/user/follow/subscribe` | 配置关注推送 |
| 推文推送订阅 | POST | `/api/v1/user/tweet/subscribe` | 配置推文推送 |
| 转发推送订阅 | POST | `/api/v1/user/retweet/subscribe` | 配置转发推送 |
| 回复推送订阅 | POST | `/api/v1/user/reply/subscribe` | 配置回复推送 |
| 头像推送订阅 | POST | `/api/v1/user/avatar/subscribe` | 配置头像推送 |

**请求格式（所有API通用）**：
```json
{
  "user_id": "1958171043547500544",
  "enable": true
}
```

**定时任务实现方案**（使用Python）：

**方案概述**：
```
1. 定时任务（例如每分钟执行一次）
2. 查询数据库：SELECT * FROM twitter_account_manage 
   WHERE sync_status=0 AND twitter_user_id IS NOT NULL AND retry_count<3
3. 遍历每条记录，根据enable_xx_push字段调用对应的API
4. API调用成功 → 更新sync_status=1
5. API调用失败 → 增加retry_count，超过3次则放弃
6. 每次调用间隔500ms，避免API限流
```

**Python脚本结构建议**：

📁 **文件**: `scripts/twitter_push_sync.py`

```python
# 主要逻辑
1. 连接数据库（MySQL）
2. 查询待同步记录（sync_status=0）
3. 遍历记录，根据enable_xx_push字段决定调用哪些API
4. 调用Twitter API（使用requests库）
5. 更新数据库状态（sync_status、last_sync_time）
6. 错误处理：增加retry_count
7. 日志记录

# 需要的库
- pymysql / mysql-connector-python（数据库连接）
- requests（HTTP请求）
- schedule（定时任务调度）
- logging（日志记录）
```

**数据库查询SQL**：
```sql
-- 查询需要同步的记录
SELECT id, twitter_url, twitter_user_id, 
       enable_follow_push, enable_tweet_push, 
       enable_retweet_push, enable_reply_push, 
       enable_avatar_push
FROM twitter_account_manage
WHERE twitter_user_id IS NOT NULL 
  AND twitter_user_id != ''
  AND sync_status = 0
  AND retry_count < 3
  AND del_flag = '0'
ORDER BY create_time ASC
LIMIT 100;
```

**数据库更新SQL**：
```sql
-- 更新同步成功
UPDATE twitter_account_manage 
SET sync_status = 1, last_sync_time = NOW()
WHERE id = ?;

-- 更新同步失败（增加重试次数）
UPDATE twitter_account_manage 
SET retry_count = retry_count + 1, last_sync_time = NOW()
WHERE id = ?;
```

**开发任务清单（明天）**：

- [ ] 创建Python定时任务脚本 `scripts/twitter_push_sync.py`
- [ ] 实现数据库查询逻辑（查询待同步记录）
- [ ] 实现5个API调用函数（关注/推文/转发/回复/头像）
- [ ] 实现同步状态更新逻辑（成功/失败）
- [ ] 添加错误处理和重试机制
- [ ] 添加日志记录
- [ ] 配置定时任务（crontab或schedule库）
- [ ] 测试脚本运行

**测试步骤**：

1. 在前端配置一个推送订阅（保存到数据库，sync_status默认为0）
2. 手动运行Python脚本测试
3. 查看日志确认API调用
4. 检查数据库sync_status是否更新为1
5. 配置定时任务（每分钟执行一次）
6. 验证推送功能是否生效

---

### 📊 代码统计

| 模块 | 新增文件 | 修改文件 | 代码行数 |
|------|---------|---------|---------|
| **后端Java** | 8个 | 3个 | ~1500行 |
| **后端Mapper XML** | 3个 | 1个 | ~400行 |
| **前端Vue** | 0个 | 1个 | ~1200行 |
| **前端API** | 1个 | 1个 | ~200行 |
| **数据库SQL** | 2个 | 0个 | ~150行 |
| **Python脚本** | 1个 | 0个 | ~260行 |
| **配置文件** | 0个 | 1个 | ~20行 |
| **总计** | **15个新增** | **7个修改** | **~3730行** |

---

### 📁 文件清单

#### 新增文件：
1. `sql/twitter_account_manage.sql` - Twitter账号管理表
2. `sql/token_monitor_config.sql` - Token监控配置表
3. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/config/TwitterApiProperties.java`
4. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/domain/dto/TwitterApiResponse.java`
5. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/domain/TwitterAccountManage.java`
6. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/domain/TokenMonitorConfig.java`
7. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/mapper/TwitterAccountManageMapper.java`
8. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/mapper/TokenMonitorConfigMapper.java`
9. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/service/ITwitterService.java`
10. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/service/impl/TwitterServiceImpl.java`
11. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/controller/TokenMonitorConfigController.java`
12. `ruoyi-crypto/src/main/resources/mapper/crypto/TwitterAccountManageMapper.xml`
13. `ruoyi-crypto/src/main/resources/mapper/crypto/TokenMonitorConfigMapper.xml`
14. `RuoYi-Vue3/src/api/crypto/monitor.js`
15. `scripts/twitter_batch_follow.py`

#### 修改文件：
1. `ruoyi-admin/src/main/resources/application.yml` - 添加Twitter API配置
2. `ruoyi-crypto/src/main/java/com/ruoyi/crypto/controller/TokenLaunchHistoryController.java` - 添加Twitter相关接口
3. `ruoyi-crypto/src/main/resources/mapper/crypto/TokenLaunchHistoryMapper.xml` - 添加关注状态查询
4. `RuoYi-Vue3/src/views/crypto/tokenMonitor/index.vue` - 完整重构页面
5. `RuoYi-Vue3/src/api/crypto/token.js` - 添加Twitter相关API
6. `RuoYi-Vue3/src/assets/styles/element-plus-variables.d.css` - 样式优化
7. `README.md` - 项目说明更新

---

### 🚀 部署说明

#### 1. 数据库初始化
```bash
# 依次执行SQL脚本
mysql -h 47.106.217.116 -u admin -p crypto_web3 < sql/twitter_account_manage.sql
mysql -h 47.106.217.116 -u admin -p crypto_web3 < sql/token_monitor_config.sql
```

#### 2. 后端配置
修改 `ruoyi-admin/src/main/resources/application.yml`：
- 更新 `twitter.api.authorization` 为你的Token
- 更新 `twitter.api.cookie` 为你的Cookie

#### 3. 后端编译运行
```bash
cd ruoyi-admin
mvn clean package
mvn spring-boot:run
```

#### 4. 前端运行
```bash
cd RuoYi-Vue3
npm install
npm run dev
```

---

### 🎯 使用指南

#### 快速查询已关注的推特主页（前500条）
```sql
SELECT 
    t.id, t.ca, t.token_name, t.token_symbol, 
    t.twitter_url, t.launch_time, t.highest_market_cap
FROM token_launch_history t
INNER JOIN twitter_account_manage ta 
    ON t.twitter_url = ta.twitter_url 
    AND ta.twitter_type = 'profile'
WHERE t.twitter_url IS NOT NULL 
  AND t.twitter_url != ''
  AND t.twitter_url NOT LIKE '%/status/%'
  AND t.twitter_url NOT LIKE '%/communities/%'
  AND t.twitter_url NOT LIKE '%/search%'
ORDER BY t.launch_time DESC
LIMIT 500;
```

#### 批量关注所有推特主页
```bash
cd scripts
python twitter_batch_follow.py
```

---

### 📝 技术亮点

1. **数据库设计**：三表分离，职责清晰，避免冗余
2. **API封装**：OkHttp + 完整Header配置，支持gzip解压
3. **前端组件化**：Vue3 Composition API，代码复用性高
4. **错误处理**：完善的异常捕获和用户提示
5. **性能优化**：批量操作延迟控制，避免API限流
6. **扩展性**：预留user_id字段，支持未来更精确的操作

---

### 🔗 相关链接

- **项目仓库**：[Kakarot-Web3](https://github.com/your-repo/Kakarot-Web3)
- **Twitter API文档**：https://alpha.apidance.pro/api/v1
- **RuoYi框架文档**：http://doc.ruoyi.vip/

---

**文档更新时间**：2025-01-25  
**当前版本**：v2.1

