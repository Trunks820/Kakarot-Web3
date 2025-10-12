package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.TwitterAccountManage;

import java.util.List;
import java.util.Map;

/**
 * Twitter服务接口
 *
 * @author ruoyi
 */
public interface ITwitterService {

    /**
     * 关注Twitter账号
     *
     * @param twitterUrl Twitter链接
     * @return 是否成功
     */
    boolean followTwitter(String twitterUrl);

    /**
     * 取消关注Twitter账号
     *
     * @param twitterUrl Twitter链接
     * @return 是否成功
     */
    boolean unfollowTwitter(String twitterUrl);

    /**
     * 批量关注Twitter账号
     *
     * @param twitterUrls Twitter链接列表
     * @return 成功数量
     */
    int batchFollow(List<String> twitterUrls);

    /**
     * 批量取消关注Twitter账号
     *
     * @param twitterUrls Twitter链接列表
     * @return 成功数量
     */
    int batchUnfollow(List<String> twitterUrls);

    /**
     * 获取或创建Twitter账号（如果不存在则自动创建）
     *
     * @param twitterUrl Twitter链接
     * @return Twitter账号管理对象
     */
    TwitterAccountManage getOrCreateAccount(String twitterUrl);

    /**
     * 批量获取Twitter账号信息
     *
     * @param twitterUrls Twitter链接列表
     * @return Map<twitterUrl, TwitterAccountManage>
     */
    Map<String, TwitterAccountManage> batchGetAccounts(List<String> twitterUrls);

    /**
     * 更新推送配置
     *
     * @param twitterAccount Twitter账号管理对象
     * @return 是否成功
     */
    boolean updatePushConfig(TwitterAccountManage twitterAccount);
}

