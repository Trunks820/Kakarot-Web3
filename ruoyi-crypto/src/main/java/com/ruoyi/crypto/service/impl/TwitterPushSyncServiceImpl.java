package com.ruoyi.crypto.service.impl;

import com.ruoyi.crypto.config.TwitterApiProperties;
import com.ruoyi.crypto.domain.TwitterAccountManage;
import com.ruoyi.crypto.domain.TwitterPushSyncStatus;
import com.ruoyi.crypto.mapper.TwitterPushSyncStatusMapper;
import com.ruoyi.crypto.service.ITwitterPushSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import okhttp3.*;
import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Twitter推送同步服务实现
 *
 * @author ruoyi
 */
@Service
public class TwitterPushSyncServiceImpl implements ITwitterPushSyncService {

    private static final Logger log = LoggerFactory.getLogger(TwitterPushSyncServiceImpl.class);

    @Resource
    private TwitterApiProperties twitterApiProperties;

    @Resource
    private TwitterPushSyncStatusMapper syncStatusMapper;

    private final OkHttpClient httpClient;

    // API路径映射
    private static final Map<String, String> API_ENDPOINTS = new HashMap<>();
    static {
        API_ENDPOINTS.put("follow", "/api/v1/user/follow/push");
        API_ENDPOINTS.put("tweet", "/api/v1/user/tweet/push");
        API_ENDPOINTS.put("retweet", "/api/v1/user/retweet/push");
        API_ENDPOINTS.put("reply", "/api/v1/user/reply/push");
        API_ENDPOINTS.put("avatar", "/api/v1/user/avatar/push");
    }

    public TwitterPushSyncServiceImpl() {
        // 创建OkHttpClient，设置超时时间
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Boolean> asyncSyncPushConfig(TwitterAccountManage twitterAccount) {
        log.info("开始异步同步Twitter推送配置: {}", twitterAccount.getTwitterUrl());
        
        if (twitterAccount.getTwitterUserId() == null || twitterAccount.getTwitterUserId().isEmpty()) {
            log.warn("Twitter用户ID为空，跳过同步: {}", twitterAccount.getTwitterUrl());
            return CompletableFuture.completedFuture(false);
        }

        String twitterUserId = twitterAccount.getTwitterUserId();
        boolean allSuccess = true;
        int successCount = 0;
        int totalCount = 0;

        // 推送类型配置映射
        Map<String, Integer> pushConfigs = new HashMap<>();
        pushConfigs.put("follow", twitterAccount.getEnableFollowPush());
        pushConfigs.put("tweet", twitterAccount.getEnableTweetPush());
        pushConfigs.put("retweet", twitterAccount.getEnableRetweetPush());
        pushConfigs.put("reply", twitterAccount.getEnableReplyPush());
        pushConfigs.put("avatar", twitterAccount.getEnableAvatarPush());

        // 遍历每种推送类型
        for (Map.Entry<String, Integer> entry : pushConfigs.entrySet()) {
            String pushType = entry.getKey();
            Integer enableValue = entry.getValue();
            
            if (enableValue != null) { // 只处理有配置的推送类型
                totalCount++;
                boolean enable = enableValue == 1;
                
                try {
                    boolean success = syncSinglePushType(twitterUserId, pushType, enable);
                    
                    // 记录同步状态
                    TwitterPushSyncStatus syncStatus = new TwitterPushSyncStatus();
                    syncStatus.setAccountId(twitterAccount.getId());
                    syncStatus.setPushType(pushType);
                    syncStatus.setCurrentStatus(enable ? 1 : 0);
                    syncStatus.setLastSyncTime(new Date());
                    syncStatus.setApiResponse(success ? "SUCCESS" : "FAILED");
                    
                    upsertSyncStatus(syncStatus);
                    
                    if (success) {
                        successCount++;
                        log.info("推送类型 {} 同步成功: {} -> {}", pushType, twitterUserId, enable);
                    } else {
                        allSuccess = false;
                        log.error("推送类型 {} 同步失败: {} -> {}", pushType, twitterUserId, enable);
                    }
                    
                } catch (Exception e) {
                    allSuccess = false;
                    log.error("推送类型 {} 同步异常: {} -> {}", pushType, twitterUserId, enable, e);
                    
                    // 记录异常状态
                    TwitterPushSyncStatus syncStatus = new TwitterPushSyncStatus();
                    syncStatus.setAccountId(twitterAccount.getId());
                    syncStatus.setPushType(pushType);
                    syncStatus.setCurrentStatus(enable ? 1 : 0);
                    syncStatus.setLastSyncTime(new Date());
                    syncStatus.setApiResponse("ERROR: " + e.getMessage());
                    
                    upsertSyncStatus(syncStatus);
                }
                
                // 请求间隔，避免API限流
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("Twitter推送配置异步同步完成: {}, 成功: {}/{}", 
                twitterAccount.getTwitterUrl(), successCount, totalCount);
        
        return CompletableFuture.completedFuture(allSuccess);
    }

    @Override
    public boolean syncSinglePushType(String twitterUserId, String pushType, boolean enable) {
        String endpoint = API_ENDPOINTS.get(pushType);
        if (endpoint == null) {
            log.error("不支持的推送类型: {}", pushType);
            return false;
        }

        String url = twitterApiProperties.getBaseUrl().replace("/api/v1", "") + endpoint;
        
        // 构建请求体
        String requestBodyJson = String.format("{\"user_id\":\"%s\",\"push_status\":%s}", 
                twitterUserId, enable);

        try {
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, requestBodyJson);
            
            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("authorization", twitterApiProperties.getAuthorization())
                    .addHeader("sec-ch-ua", "\"Not)A;Brand\";v=\"8\", \"Chromium\";v=\"138\", \"Google Chrome\";v=\"138\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("origin", "https://alpha.apidance.pro")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("referer", "https://alpha.apidance.pro/")
                    .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .addHeader("priority", "u=1, i")
                    .build();

            Response response = httpClient.newCall(request).execute();
            
            String responseBody = "";
            if (response.body() != null) {
                try {
                    responseBody = response.body().string();
                } catch (Exception e) {
                    responseBody = "Response body parse error: " + e.getMessage();
                }
            }
            
            log.debug("Twitter API调用: {} -> {}, 状态码: {}, 响应: {}", 
                    pushType, enable, response.code(), responseBody);

            // 判断是否成功（通常200表示成功）
            if (response.isSuccessful()) {
                return true;
            } else {
                log.warn("Twitter API调用失败: {} -> {}, 状态码: {}, 响应: {}", 
                        pushType, enable, response.code(), responseBody);
                return false;
            }

        } catch (IOException e) {
            log.error("Twitter API调用异常: {} -> {}", pushType, enable, e);
            return false;
        }
    }

    @Override
    public List<TwitterPushSyncStatus> selectSyncStatusList(TwitterPushSyncStatus syncStatus) {
        return syncStatusMapper.selectSyncStatusList(syncStatus);
    }

    @Override
    public List<TwitterPushSyncStatus> selectSyncStatusByAccountId(Long accountId) {
        return syncStatusMapper.selectByAccountId(accountId);
    }

    @Override
    public boolean upsertSyncStatus(TwitterPushSyncStatus syncStatus) {
        try {
            int result = syncStatusMapper.upsertSyncStatus(syncStatus);
            return result > 0;
        } catch (Exception e) {
            log.error("更新同步状态失败", e);
            return false;
        }
    }

    @Override
    public int batchUpdateSyncStatus(List<TwitterPushSyncStatus> syncStatusList) {
        if (syncStatusList == null || syncStatusList.isEmpty()) {
            return 0;
        }
        
        try {
            return syncStatusMapper.batchUpdateSyncStatus(syncStatusList);
        } catch (Exception e) {
            log.error("批量更新同步状态失败", e);
            return 0;
        }
    }
}
