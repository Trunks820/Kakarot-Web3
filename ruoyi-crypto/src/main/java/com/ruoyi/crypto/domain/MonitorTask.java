package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 监控任务对象 monitor_task
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
@Data
public class MonitorTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long id;

    /** 任务名称 */
    private String taskName;

    /** 任务类型：smart-智能/batch-批量/block-区块 */
    private String taskType;

    /** 链类型：sol/bsc/eth */
    private String chainType;

    /** 最小市值(仅smart) */
    private BigDecimal minMarketCap;

    /** 最大市值(仅smart) */
    private BigDecimal maxMarketCap;

    /** Twitter筛选(仅smart): profile-推特主页/tweet-推文/community-社区/none-无推特 */
    private String hasTwitter;

    /** 是否自动同步目标(仅smart) */
    private Integer autoSyncTargets;

    /** 同步间隔分钟数(仅smart) */
    private Integer syncIntervalMinutes;

    /** 目标数量(仅batch) */
    private Integer targetCount;

    /** 目标列表(仅batch) */
    private List<String> targetList;
    
    /** 配置数量（非数据库字段，仅用于列表展示） */
    private Integer configCount;

    /** 批次数量（非数据库字段，仅用于列表展示） */
    private Integer batchCount;

    /** 市场类型(仅block)：internal-内盘/external-外盘 */
    private String marketType;

    /** 单笔最小交易金额(USD)(仅block) */
    private BigDecimal minTransactionUsd;

    /** 累计最小金额(USD)(仅block) */
    private BigDecimal cumulativeMinAmountUsd;

    /** 定时规则(可选) */
    private String scheduleCron;

    /** 状态：1-启用/0-停用/2-暂停 */
    private Integer status;

    /** 最后执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastRunTime;

    /** 下次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextRunTime;

    /** 任务描述 */
    private String description;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 关联的配置列表（非数据库字段） */
    private List<MonitorConfig> configs;

    /** 配置ID列表（用于创建/更新） */
    private List<Long> configIds;

    /** 当前任务活跃的批次epoch版本号 */
    private Integer currentEpoch;


}

