package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 监控配置对象 monitor_config
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
@Data
public class MonitorConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long id;

    /** 配置名称 */
    private String configName;

    /** 配置类型：price/holder/volume/composite */
    private String configType;

    /** 链类型：sol/bsc/eth */
    private String chainType;

    /** 数据源筛选：all/pump/fourmeme */
    private String source;

    /** 市场类型：internal-内盘/external-外盘 */
    private String marketType;

    /** 单笔最小交易金额(USD) */
    private BigDecimal minTransactionUsd;

    /** 累计最小金额(USD) */
    private BigDecimal cumulativeMinAmountUsd;

    /** 时间周期：1m/5m/1h/24h */
    private String timeInterval;

    /** 前十持仓阈值% */
    private BigDecimal topHoldersThreshold;

    /** 事件配置JSON */
    private String eventsConfig;

    /** 触发逻辑：any-任一满足/all-全部满足 */
    private String triggerLogic;

    /** 通知方式：telegram,wechat,email */
    private String notifyMethods;

    /** 配置版本号 */
    private Integer version;

    /** 配置描述 */
    private String description;

    /** 状态：1-启用/0-停用 */
    private Integer status;

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

    /** 删除标志：0-未删除/1-已删除 */
    private Integer delFlag;

    private Integer taskCount;
}

