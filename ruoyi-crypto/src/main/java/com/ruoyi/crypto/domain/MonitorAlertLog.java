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
 * 监控告警日志对象 monitor_alert_log_v2
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "监控告警日志", description = "监控系统V2告警记录")
public class MonitorAlertLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 告警ID */
    @ApiModelProperty("告警ID")
    private Long id;

    /** 关联任务ID */
    @Excel(name = "任务ID")
    @ApiModelProperty("关联任务ID")
    private Long taskId;

    /** 触发的配置ID */
    @Excel(name = "配置ID")
    @ApiModelProperty("触发的配置ID")
    private Long configId;

    /** 告警去重键 */
    @ApiModelProperty("告警去重键")
    private String alertKey;

    /** 任务类型：smart/batch/block */
    @Excel(name = "任务类型")
    @ApiModelProperty("任务类型")
    private String taskType;

    /** 链类型 */
    @Excel(name = "链类型")
    @ApiModelProperty("链类型")
    private String chainType;

    /** Token地址 */
    @Excel(name = "Token地址")
    @ApiModelProperty("Token地址")
    private String ca;

    /** Token名称 */
    @Excel(name = "Token名称")
    @ApiModelProperty("Token名称")
    private String tokenName;

    /** Token符号 */
    @Excel(name = "Token符号")
    @ApiModelProperty("Token符号")
    private String tokenSymbol;

    /** 市值 */
    @Excel(name = "市值")
    @ApiModelProperty("市值")
    private BigDecimal marketCap;

    /** 告警类型：price_change/holder_change/volume_change/block_event */
    @Excel(name = "告警类型")
    @ApiModelProperty("告警类型")
    private String alertType;

    /** 告警标题 */
    @Excel(name = "告警标题")
    @ApiModelProperty("告警标题")
    private String alertTitle;

    /** 告警内容(JSON格式) */
    @ApiModelProperty("告警内容(JSON格式)")
    private String alertContent;

    /** 触发值 */
    @Excel(name = "触发值")
    @ApiModelProperty("触发值")
    private BigDecimal triggerValue;

    /** 通知状态：pending/sent/failed/skipped */
    @Excel(name = "通知状态")
    @ApiModelProperty("通知状态")
    private String notifyStatus;

    /** 通知渠道：telegram,wechat */
    @Excel(name = "通知渠道")
    @ApiModelProperty("通知渠道")
    private String notifyChannels;

    /** 通知时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "通知时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("通知时间")
    private Date notifyTime;

    /** 通知错误信息 */
    @ApiModelProperty("通知错误信息")
    private String notifyError;

    /** 检测延迟(毫秒) */
    @Excel(name = "检测延迟")
    @ApiModelProperty("检测延迟(毫秒)")
    private Integer detectLatencyMs;

    /** 通知延迟(毫秒) */
    @Excel(name = "通知延迟")
    @ApiModelProperty("通知延迟(毫秒)")
    private Integer notifyLatencyMs;

    /** 告警级别 (前端扩展字段，用于筛选) */
    @ApiModelProperty("告警级别：high/medium/low")
    private String level;

    /** 处理状态 (前端扩展字段) */
    @ApiModelProperty("处理状态：unread/read/handled")
    private String status;

    /** 处理备注 */
    @ApiModelProperty("处理备注")
    private String handleRemark;

    /** 处理人 */
    @ApiModelProperty("处理人")
    private String handleBy;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("处理时间")
    private Date handleTime;
}

