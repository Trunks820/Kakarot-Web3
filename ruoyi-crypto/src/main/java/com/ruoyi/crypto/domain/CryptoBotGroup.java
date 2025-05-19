package com.ruoyi.crypto.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "机器人群组信息", description = "机器人群组信息")
public class CryptoBotGroup extends BaseEntity {

        private static final long serialVersionUID = 1L;

        /** 主键 */
        @ApiModelProperty("群组ID")
        private Long id;

        /** 平台(wechat/telegram) */
        @Excel(name = "平台")
        @ApiModelProperty("平台(wechat/telegram)")
        private String platform;

        /** 群组ID */
        @Excel(name = "群组ID")
        @ApiModelProperty("平台上的群组ID")
        private String groupId;

        /** 群组名称 */
        @Excel(name = "群组名称")
        @ApiModelProperty("群组名称")
        private String groupName;

        /** 胜率计算阈值(默认0.5) */
        @Excel(name = "胜率计算阈值")
        @ApiModelProperty("胜率计算阈值(默认0.5)")
        private BigDecimal winThreshold;

        /** 是否激活 */
        @Excel(name = "是否激活", readConverterExp = "0=否,1=是")
        @ApiModelProperty("是否激活")
        private Integer isActive;

        /** 删除标志（0代表存在 2代表删除） */
        private String delFlag;

}
