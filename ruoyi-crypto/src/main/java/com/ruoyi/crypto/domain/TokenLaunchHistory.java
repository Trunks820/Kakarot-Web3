package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
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
    @ApiModelProperty("发射时间")
    private Date launchTime;

    /** 历史最高市值 */
    @Excel(name = "历史最高市值")
    @ApiModelProperty("历史最高市值")
    private Long highestMarketCap;

    /** TG消息ID */
    @ApiModelProperty("Telegram消息ID")
    private String tgMsgId;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("入库时间")
    private Date createdAt;
}