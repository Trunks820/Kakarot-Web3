package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.TwitterAccountManage;
import com.ruoyi.crypto.domain.TwitterPushSyncStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Twitter推送同步服务接口
 *
 * @author ruoyi
 */
public interface ITwitterPushSyncService {

    /**
     * 异步同步Twitter推送配置（主要方法）
     * 
     * @param twitterAccount Twitter账号管理对象
     * @return CompletableFuture<Boolean> 异步结果
     */
    CompletableFuture<Boolean> asyncSyncPushConfig(TwitterAccountManage twitterAccount);

    /**
     * 同步单个推送类型
     * 
     * @param twitterUserId Twitter用户ID
     * @param pushType 推送类型（follow/tweet/retweet/reply/avatar）
     * @param enable 是否启用
     * @return 是否成功
     */
    boolean syncSinglePushType(String twitterUserId, String pushType, boolean enable);

    /**
     * 查询推送同步状态列表
     * 
     * @param syncStatus 查询条件
     * @return 同步状态列表
     */
    List<TwitterPushSyncStatus> selectSyncStatusList(TwitterPushSyncStatus syncStatus);

    /**
     * 根据账号ID查询同步状态
     * 
     * @param accountId 账号ID
     * @return 同步状态列表
     */
    List<TwitterPushSyncStatus> selectSyncStatusByAccountId(Long accountId);

    /**
     * 更新或插入同步状态
     * 
     * @param syncStatus 同步状态
     * @return 是否成功
     */
    boolean upsertSyncStatus(TwitterPushSyncStatus syncStatus);

    /**
     * 批量更新同步状态
     * 
     * @param syncStatusList 同步状态列表
     * @return 成功数量
     */
    int batchUpdateSyncStatus(List<TwitterPushSyncStatus> syncStatusList);
}
