package com.ruoyi.crypto.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@ApiModel(value = "钱包信息", description = "钱包信息")
@Data
public class CryptoWallet extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 钱包地址 */
    @Excel(name = "钱包地址")
    @ApiModelProperty("钱包地址")
    @NotBlank(message = "钱包地址不能为空")
    private String walletAddress;

    /** 钱包备注 */
    @Excel(name = "钱包备注")
    @ApiModelProperty("钱包备注")
    private String walletName;

    /** 上次活跃时间 */
    @ApiModelProperty("上次活跃时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastActiveTime;

    /** 监控状态 */
    @ApiModelProperty("监控状态")
    private Integer monitorState;

    @Excel(name = "链类型")
    @ApiModelProperty("链类型")
    private String chainType;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;



}
