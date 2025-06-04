package com.ruoyi.crypto.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "机器人用户视图对象", description = "机器人用户视图对象")
public class CryptoUserVO {
    /** 用户id */
    @ApiModelProperty("用户id")
    private String userId;
    /** 用户名 */
    @ApiModelProperty("用户名")
    private String userName;
    /** 群聊id */
    @ApiModelProperty("群聊id")
    private String groupId;
    /** 群聊名 */
    @ApiModelProperty("群聊名")
    private String groupName;
    /** 查询总数 */
    @ApiModelProperty("查询总数")
    private Integer count;
    /** 成功数 */
    @ApiModelProperty("成功数")
    private Integer success;

}
