package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "监控配置信息", description = "价格监控配置信息")
public class CryptoMonitorConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty("配置ID")
    private Long id;

    /** 代币地址 */
    @Excel(name = "代币地址")
    @ApiModelProperty("代币地址")
    private String coinAddress;

    /** 代币符号 */
    @Excel(name = "代币符号")
    @ApiModelProperty("代币符号")
    private String tokenSymbol;

    /** 代币名称 */
    @Excel(name = "代币名称")
    @ApiModelProperty("代币名称")
    private String tokenName;

    /** 提醒模式 */
    @Excel(name = "提醒模式", readConverterExp = "timer=定时提醒,condition=条件触发")
    @ApiModelProperty("提醒模式(timer:定时提醒, condition:条件触发)")
    private String alertMode;

    /** 定时提醒间隔(分钟) */
    @Excel(name = "定时间隔")
    @ApiModelProperty("定时提醒间隔(分钟)")
    private Integer timerInterval;

    /** 条件类型 */
    @Excel(name = "条件类型")
    @ApiModelProperty("条件类型(priceAbove:价格高于, priceBelow:价格低于, marketCapBelow:市值低于, changeExceeds:涨跌幅超过)")
    private String conditionType;

    /** 条件阈值 */
    @Excel(name = "条件阈值")
    @ApiModelProperty("条件阈值")
    private BigDecimal conditionValue;

    /** 通知方式 */
    @Excel(name = "通知方式")
    @ApiModelProperty("通知方式(wechat,telegram)")
    private String notifyMethods;

    /** 微信名称 */
    @Excel(name = "微信名称")
    @ApiModelProperty("微信名称")
    private String wechatName;

    /** Telegram名称 */
    @Excel(name = "Telegram名称")
    @ApiModelProperty("Telegram名称")
    private String telegramName;

    /** 备注 */
    @Excel(name = "备注")
    @ApiModelProperty("备注")
    private String remark;

    // === 状态控制字段 ===

    /** 状态 */
    @Excel(name = "状态", readConverterExp = "0=停用,1=启用")
    @ApiModelProperty("状态(0:停用, 1:启用)")
    private String status;

    /** 上次通知时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("上次通知时间")
    private Date lastNotificationTime;

    /** 链类型 */
    @Excel(name = "链类型")
    @ApiModelProperty("链类型")
    private String chainType;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;
}
