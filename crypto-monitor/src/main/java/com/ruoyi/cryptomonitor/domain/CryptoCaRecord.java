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
@ApiModel(value = "CA记录信息", description = "代币记录信息")
public class CryptoCaRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty("记录ID")
    private Long id;

    /** 关联代币id */
    @Excel(name = "关联代币ID")
    @ApiModelProperty("关联代币ID")
    private Long caId;

    /** 首次查询用户ID */
    @Excel(name = "首次查询用户ID")
    @ApiModelProperty("首次查询用户ID")
    private String firstQueryUserId;

    /** 首次查询群组ID */
    @Excel(name = "首次查询群组ID")
    @ApiModelProperty("首次查询群组ID")
    private String firstQueryGroupId;

    /** 首次查询时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "首次查询时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("首次查询时间")
    private Date firstQueryTime;

    /** 首次查询时市值 */
    @Excel(name = "首次查询时市值")
    @ApiModelProperty("首次查询时市值")
    private BigDecimal firstMarketCap;

    /** 首次查询时价格 */
    @Excel(name = "首次查询时价格")
    @ApiModelProperty("首次查询时价格")
    private BigDecimal firstPrice;

    /** 历史最高市值 */
    @Excel(name = "历史最高市值")
    @ApiModelProperty("历史最高市值")
    private BigDecimal highestMarketCap;

    /** 历史最高价格 */
    @Excel(name = "历史最高价格")
    @ApiModelProperty("历史最高价格")
    private BigDecimal highestPrice;

    /** 达到最高价格时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "达到最高价格时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("达到最高价格时间")
    private Date highestTime;

    /** 最大倍数（最高市值/首次查询市值） */
    @Excel(name = "最大倍数")
    @ApiModelProperty("最大倍数（最高市值/首次查询市值）")
    private BigDecimal maxMultiple;

    /** 是否成功（涨幅是否超过50%） */
    @Excel(name = "是否成功", readConverterExp = "0=否,1=是")
    @ApiModelProperty("是否成功（涨幅是否超过50%）")
    private Integer isSuccessful;

    /** 查询次数 */
    @Excel(name = "查询次数")
    @ApiModelProperty("查询次数")
    private Long queryCount;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;
}
