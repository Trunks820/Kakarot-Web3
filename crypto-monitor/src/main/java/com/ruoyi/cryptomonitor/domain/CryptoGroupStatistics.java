package com.ruoyi.cryptomonitor.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "群组CA统计信息", description = "群组CA统计信息")
public class CryptoGroupStatistics extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty("统计ID")
    private Long id;

    /** 群组ID */
    @Excel(name = "群组ID")
    @ApiModelProperty("群组ID")
    private String groupId;

    /** 总CA查询次数 */
    @Excel(name = "总CA查询次数")
    @ApiModelProperty("总CA查询次数")
    private Long totalCaQueries;

    /** ca数量 */
    @Excel(name = "ca数量")
    @ApiModelProperty("不同CA数量")
    private Long uniqueCaCount;

    /** 成功CA数量（涨幅超过50%） */
    @Excel(name = "成功CA数量")
    @ApiModelProperty("成功CA数量（涨幅超过50%）")
    private Long successfulCaCount;

    /** 胜率(成功CA数/不同CA数) */
    @Excel(name = "胜率")
    @ApiModelProperty("胜率(成功CA数/不同CA数)")
    private BigDecimal winRate;

    /** 最后计算时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后计算时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("最后计算时间")
    private Date lastCalculatedTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

}
