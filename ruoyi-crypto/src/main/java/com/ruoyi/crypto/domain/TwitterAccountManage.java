package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Twitter账号管理对象 twitter_account_manage
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "Twitter账号管理", description = "Twitter账号管理信息")
public class TwitterAccountManage extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @ApiModelProperty("主键ID")
    private Long id;

    /** Twitter链接 */
    @Excel(name = "Twitter链接")
    @ApiModelProperty("Twitter链接")
    private String twitterUrl;

    /** Twitter用户名 */
    @Excel(name = "Twitter用户名")
    @ApiModelProperty("Twitter用户名")
    private String twitterUsername;

    /** Twitter类型 */
    @Excel(name = "Twitter类型", readConverterExp = "profile=主页,tweet=推文,community=社区")
    @ApiModelProperty("Twitter类型(profile:主页, tweet:推文, community:社区)")
    private String twitterType;

    /** 是否已关注 */
    @Excel(name = "是否已关注", readConverterExp = "0=未关注,1=已关注")
    @ApiModelProperty("是否已关注(0:未关注 1:已关注)")
    private Integer isFollowing;

    /** 关注时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "关注时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("关注时间")
    private Date followTime;

    /** 取消关注时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "取消关注时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("取消关注时间")
    private Date unfollowTime;

    /** 关注推送开关 */
    @Excel(name = "关注推送", readConverterExp = "0=关闭,1=开启")
    @ApiModelProperty("关注推送开关")
    private Integer enableFollowPush;

    /** 推文推送开关 */
    @Excel(name = "推文推送", readConverterExp = "0=关闭,1=开启")
    @ApiModelProperty("推文推送开关")
    private Integer enableTweetPush;

    /** 转发推送开关 */
    @Excel(name = "转发推送", readConverterExp = "0=关闭,1=开启")
    @ApiModelProperty("转发推送开关")
    private Integer enableRetweetPush;

    /** 回复推送开关 */
    @Excel(name = "回复推送", readConverterExp = "0=关闭,1=开启")
    @ApiModelProperty("回复推送开关")
    private Integer enableReplyPush;

    /** 头像更换推送开关 */
    @Excel(name = "头像更换推送", readConverterExp = "0=关闭,1=开启")
    @ApiModelProperty("头像更换推送开关")
    private Integer enableAvatarPush;

    /** 通知方式 */
    @Excel(name = "通知方式")
    @ApiModelProperty("通知方式(telegram,wechat)")
    private String notifyMethods;

    /** 推送状态 */
    @Excel(name = "推送状态", readConverterExp = "0=停用,1=启用")
    @ApiModelProperty("推送状态(0:停用 1:启用)")
    private String pushStatus;

    /** 关联Token数量 */
    @Excel(name = "关联Token数量")
    @ApiModelProperty("关联Token数量")
    private Integer relatedTokenCount;

    /** 累计关注次数 */
    @Excel(name = "累计关注次数")
    @ApiModelProperty("累计关注次数")
    private Integer followCount;

    /** 最后检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("最后检查时间")
    private Date lastCheckTime;
}

