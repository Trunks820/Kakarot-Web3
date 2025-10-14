package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Twitter推送同步状态对象 twitter_push_sync_status
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "Twitter推送同步状态", description = "Twitter推送同步状态信息")
public class TwitterPushSyncStatus {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    private Long id;

    /** 关联twitter_account_manage.id */
    @Excel(name = "账号ID")
    @ApiModelProperty("关联twitter_account_manage.id")
    private Long accountId;

    /** 推送类型 */
    @Excel(name = "推送类型", readConverterExp = "follow=关注推送,tweet=推文推送,retweet=转发推送,reply=回复推送,avatar=头像推送")
    @ApiModelProperty("推送类型：follow/tweet/retweet/reply/avatar")
    private String pushType;

    /** 当前同步的状态 */
    @Excel(name = "同步状态", readConverterExp = "0=关闭,1=开启")
    @ApiModelProperty("当前同步的状态：0-关闭，1-开启")
    private Integer currentStatus;

    /** 最后同步时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后同步时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("最后同步时间")
    private Date lastSyncTime;

    /** API响应信息 */
    @Excel(name = "API响应信息")
    @ApiModelProperty("API响应信息（用于调试）")
    private String apiResponse;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("更新时间")
    private Date updateTime;

    // 扩展字段：关联的Twitter账号信息（用于查询时关联显示）
    @ApiModelProperty("Twitter链接")
    private String twitterUrl;

    @ApiModelProperty("Twitter用户名")
    private String twitterUsername;

    @ApiModelProperty("Twitter用户ID")
    private String twitterUserId;
}
