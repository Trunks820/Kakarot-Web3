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
@ApiModel(value = "CA查询记录信息", description = "代币查询记录信息")
public class CryptoCaQueryRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty("记录ID")
    private Long id;

    /** 关联代币id */
    @Excel(name = "关联代币ID")
    @ApiModelProperty("关联代币ID")
    private Long caId;

    /** 查询用户ID */
    @Excel(name = "查询用户ID")
    @ApiModelProperty("查询用户ID")
    private String userId;

    /** 查询群组ID */
    @Excel(name = "查询群组ID")
    @ApiModelProperty("查询群组ID")
    private String groupId;

    /** 查询时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "查询时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("查询时间")
    private Date queryTime;

    /** 查询时市值 */
    @Excel(name = "查询时市值")
    @ApiModelProperty("查询时市值")
    private BigDecimal marketCapAtQuery;

    /** 查询时价格 */
    @Excel(name = "查询时价格")
    @ApiModelProperty("查询时价格")
    private BigDecimal priceAtQuery;

    /** 与首次查询的倍数 */
    @Excel(name = "与首次查询的倍数")
    @ApiModelProperty("与首次查询的倍数")
    private BigDecimal multipleFromFirst;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;
}
