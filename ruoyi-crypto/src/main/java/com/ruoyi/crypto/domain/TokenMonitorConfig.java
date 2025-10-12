package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Token监控配置对象 token_monitor_config
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "Token监控配置", description = "Token监控配置信息")
public class TokenMonitorConfig extends BaseEntity {
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

    /** 监控模式 */
    @Excel(name = "监控模式", readConverterExp = "timer=定时提醒,condition=价格触发,event=事件监控")
    @ApiModelProperty("监控模式(timer:定时提醒, condition:价格触发, event:事件监控)")
    private String alertMode;

    /** 定时提醒间隔 */
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

    /** 事件类型 */
    @Excel(name = "事件类型")
    @ApiModelProperty("事件类型(largeTransaction:大额交易监控, holdingChange:持仓异动监控)")
    private String eventType;

    /** 事件配置 */
    @ApiModelProperty("事件配置(JSON格式)")
    private String eventConfig;

    /** 通知方式 */
    @Excel(name = "通知方式")
    @ApiModelProperty("通知方式(telegram,wechat)")
    private String notifyMethods;

    /** Telegram名称 */
    @Excel(name = "Telegram名称")
    @ApiModelProperty("Telegram名称")
    private String telegramName;

    /** 微信名称 */
    @Excel(name = "微信名称")
    @ApiModelProperty("微信名称")
    private String wechatName;

    /** 监控状态 */
    @Excel(name = "监控状态", readConverterExp = "0=停用,1=启用")
    @ApiModelProperty("监控状态(0:停用 1:启用)")
    private String status;

    /** 上次通知时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上次通知时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("上次通知时间")
    private Date lastNotificationTime;

    /** 累计通知次数 */
    @Excel(name = "累计通知次数")
    @ApiModelProperty("累计通知次数")
    private Integer notificationCount;
}

