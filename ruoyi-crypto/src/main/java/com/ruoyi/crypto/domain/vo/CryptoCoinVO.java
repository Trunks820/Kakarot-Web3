package com.ruoyi.crypto.domain.vo;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "代币信息视图对象", description = "代币信息聚合视图")
public class CryptoCoinVO extends BaseEntity {
    /** 代币id */
    @ApiModelProperty("代币ID")
    private Long coinId;

    /** 合约地址 */
    @Excel(name = "合约地址")
    @ApiModelProperty("合约地址")
    private String address;

    /** 代币符号 */
    @Excel(name = "代币符号")
    @ApiModelProperty("代币符号")
    private String symbol;

    /** 代币名称 */
    @Excel(name = "代币名称")
    @ApiModelProperty("代币名称")
    private String name;

    /** 代币图标 */
    @Excel(name = "代币图标")
    @ApiModelProperty("代币图标URL")
    private String logoUrl;

    /** 链类型 */
    @Excel(name = "链类型")
    @ApiModelProperty("链类型")
    private String chainType;

    /** 描述 */
    @Excel(name = "描述")
    @ApiModelProperty("代币描述")
    private String description;

    /** 查询次数 */
    @Excel(name = "查询次数")
    @ApiModelProperty("查询次数")
    private Integer queryCount;

    /** 最大倍数 */
    @Excel(name = "最大倍数")
    @ApiModelProperty("最大倍数")
    private Double maxMultiple;

    @Excel(name = "首次查询时间")
    @ApiModelProperty("首次查询时间")
    private String firstQueryTime;

    @Excel(name = "首次查询用户")
    @ApiModelProperty("首次查询用户")
    private String firstQueryUserId;

    @Excel(name = "首次查询群聊")
    @ApiModelProperty("首次查询群聊")
    private String firstQueryGroupId;

    @Excel(name = "首次查询价格")
    @ApiModelProperty("首次查询价格")
    private String firstPrice;

    @Excel(name = "首次查询市值")
    @ApiModelProperty("首次查询市值")
    private String firstMarketCap;

    @Excel(name = "最高价格")
    @ApiModelProperty("最高价格")
    private String highestPrice;

    @Excel(name = "最高市值")
    @ApiModelProperty("最高市值")
    private String highestMarketCap;

    @Excel(name = "达到最高价格时间")
    @ApiModelProperty("达到最高价格时间")
    private String highestTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

}
