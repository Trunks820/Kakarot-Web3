package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Token监控预警日志对象 token_monitor_alert_log
 * 
 * @author ruoyi
 * @date 2025-01-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "Token监控预警日志", description = "Token监控预警历史记录")
public class TokenMonitorAlertLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    private Long id;

    /** 代币合约地址 */
    @Excel(name = "合约地址")
    @ApiModelProperty("代币合约地址")
    private String ca;

    /** 代币名称 */
    @Excel(name = "Token名称")
    @ApiModelProperty("代币名称")
    private String tokenName;

    /** 代币符号 */
    @Excel(name = "Token符号")
    @ApiModelProperty("代币符号")
    private String tokenSymbol;

    /** 触发时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "触发时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("触发时间")
    private Date triggerTime;

    /** 触发事件详情(JSON格式) */
    @ApiModelProperty("触发事件详情(JSON格式)")
    private String triggerEvents;

    /** 统计数据快照(JSON格式) */
    @ApiModelProperty("统计数据快照(JSON格式)")
    private String statsData;

    /** 通知方式 */
    @Excel(name = "通知方式")
    @ApiModelProperty("通知方式")
    private String notifyMethods;

    /** 通知状态 */
    @Excel(name = "通知状态")
    @ApiModelProperty("通知状态(pending:待发送, success:成功, failed:失败)")
    private String notifyStatus;

    /** 市值(USD) */
    @Excel(name = "市值")
    @ApiModelProperty("市值(USD)")
    private BigDecimal marketCap;

    /** 链类型 */
    @Excel(name = "链类型")
    @ApiModelProperty("链类型(sol/bsc/eth等)")
    private String chainType;
}

