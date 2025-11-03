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
 * SOL WebSocket告警记录对象 sol_ws_alert_log
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "SOL告警日志", description = "SOL智能监控告警历史记录")
public class SolAlertLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 自增ID */
    @ApiModelProperty("主键ID")
    private Long id;

    /** 批次ID */
    @Excel(name = "批次ID")
    @ApiModelProperty("批次ID")
    private Integer batchId;

    /** Token CA地址 */
    @Excel(name = "CA地址")
    @ApiModelProperty("Token CA地址")
    private String ca;

    /** Token符号 */
    @Excel(name = "Token符号")
    @ApiModelProperty("Token符号")
    private String tokenSymbol;

    /** Token名称 */
    @Excel(name = "Token名称")
    @ApiModelProperty("Token名称")
    private String tokenName;

    /** Pair地址 */
    @ApiModelProperty("Pair地址")
    private String pairAddress;

    /** 模板配置ID */
    @ApiModelProperty("模板配置ID")
    private Integer templateId;

    /** 模板名称 */
    @Excel(name = "模板名称")
    @ApiModelProperty("模板名称")
    private String templateName;

    /** 触发时价格 */
    @ApiModelProperty("触发时价格")
    private BigDecimal price;

    /** 触发时市值 */
    @Excel(name = "市值")
    @ApiModelProperty("触发时市值(USD)")
    private BigDecimal marketCap;

    /** 价格变化百分比 */
    @ApiModelProperty("价格变化百分比")
    private BigDecimal priceChange;

    /** 1分钟价格变化 */
    @Excel(name = "1分钟涨跌")
    @ApiModelProperty("1分钟价格变化(%)")
    private BigDecimal priceChange1m;

    /** 5分钟价格变化 */
    @Excel(name = "5分钟涨跌")
    @ApiModelProperty("5分钟价格变化(%)")
    private BigDecimal priceChange5m;

    /** 1小时价格变化 */
    @Excel(name = "1小时涨跌")
    @ApiModelProperty("1小时价格变化(%)")
    private BigDecimal priceChange1h;

    /** 1小时总交易量 */
    @ApiModelProperty("1小时总交易量")
    private BigDecimal volume1h;

    /** 1小时买入量 */
    @ApiModelProperty("1小时买入量")
    private BigDecimal buyVolume1h;

    /** 1小时卖出量 */
    @ApiModelProperty("1小时卖出量")
    private BigDecimal sellVolume1h;

    /** 1小时交易次数 */
    @ApiModelProperty("1小时交易次数")
    private Integer txs1h;

    /** 1小时买入次数 */
    @ApiModelProperty("1小时买入次数")
    private Integer buyTxs1h;

    /** 1小时卖出次数 */
    @ApiModelProperty("1小时卖出次数")
    private Integer sellTxs1h;

    /** TOP10持仓比例 */
    @ApiModelProperty("TOP10持仓比例(%)")
    private BigDecimal top10Percent;

    /** 触发原因（JSON数组） */
    @ApiModelProperty("触发原因（JSON数组）")
    private String triggerReasons;

    /** 触发的时间间隔(1m/5m/1h) */
    @ApiModelProperty("触发的时间间隔(1m/5m/1h)")
    private String triggerTimeInterval;

    /** 触发逻辑(any/all) */
    @ApiModelProperty("触发逻辑(any/all)")
    private String triggerLogic;

    /** 告警消息内容 */
    @ApiModelProperty("告警消息内容")
    private String alertMessage;

    /** 是否已推送Telegram(1=是,0=否) */
    @ApiModelProperty("是否已推送Telegram")
    private Integer telegramSent;

    /** Telegram推送是否成功(1=成功,0=失败) */
    @ApiModelProperty("Telegram推送是否成功")
    private Integer telegramSuccess;

    /** Telegram消息ID */
    @ApiModelProperty("Telegram消息ID")
    private String telegramMessageId;

    /** Telegram推送错误信息 */
    @ApiModelProperty("Telegram推送错误信息")
    private String telegramError;

    /** 是否已推送微信(1=是,0=否) */
    @ApiModelProperty("是否已推送微信")
    private Integer wechatSent;

    /** 微信推送是否成功(1=成功,0=失败) */
    @ApiModelProperty("微信推送是否成功")
    private Integer wechatSuccess;

    /** 微信消息ID */
    @ApiModelProperty("微信消息ID")
    private String wechatMessageId;

    /** 微信推送错误信息 */
    @ApiModelProperty("微信推送错误信息")
    private String wechatError;

    /** 处理响应时间（毫秒） */
    @ApiModelProperty("处理响应时间（毫秒）")
    private Integer responseTimeMs;

    /** 告警时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "告警时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("告警时间")
    private Date alertTime;
}

