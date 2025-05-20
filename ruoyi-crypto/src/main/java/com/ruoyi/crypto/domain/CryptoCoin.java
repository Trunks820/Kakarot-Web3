package com.ruoyi.crypto.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "代币信息", description = "代币信息")
public class CryptoCoin extends BaseEntity {

    private static final long serialVersionUID = 1L;

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

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;
}
