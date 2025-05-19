package com.ruoyi.cryptomonitor.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel(value = "监控配置信息", description = "价格监控配置信息")
public class CryptoMonitorConfig {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty("配置ID")
    private Long id;

    /** 代币ID */
    @Excel(name = "代币ID")
    @ApiModelProperty("代币ID")
    private Long coinId;

    /** 用户ID */
    @Excel(name = "用户ID")
    @ApiModelProperty("用户ID")
    private String userId;

    /** 最低价格监控 */
    @Excel(name = "最低价格监控")
    @ApiModelProperty("最低价格监控")
    private BigDecimal minPrice;

    /** 最高价格监控 */
    @Excel(name = "最高价格监控")
    @ApiModelProperty("最高价格监控")
    private BigDecimal maxPrice;

    /** 波动百分比 */
    @Excel(name = "波动百分比")
    @ApiModelProperty("波动百分比")
    private BigDecimal percentChange;

    /** 通知类型 */
    @Excel(name = "通知类型")
    @ApiModelProperty("通知类型")
    private Integer notificationType;

    /** 是否激活 */
    @Excel(name = "是否激活", readConverterExp = "0=否,1=是")
    @ApiModelProperty("是否激活")
    private Integer isActive;

    /** 通知目标(群ID或用户ID) */
    @Excel(name = "通知目标")
    @ApiModelProperty("通知目标(群ID或用户ID)")
    private String notificationTarget;

    /** 上次通知时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "上次通知时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("上次通知时间")
    private Date lastNotificationTime;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;





}
