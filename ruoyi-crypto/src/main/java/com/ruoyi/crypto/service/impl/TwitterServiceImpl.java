package com.ruoyi.crypto.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.crypto.config.TwitterApiProperties;
import com.ruoyi.crypto.domain.TwitterAccountManage;
import com.ruoyi.crypto.domain.dto.TwitterApiResponse;
import com.ruoyi.crypto.mapper.TwitterAccountManageMapper;
import com.ruoyi.crypto.service.ITwitterService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Twitter服务实现
 *
 * @author ruoyi
 */
@Service
public class TwitterServiceImpl implements ITwitterService {

    private static final Logger log = LoggerFactory.getLogger(TwitterServiceImpl.class);

    @Resource
    private TwitterApiProperties twitterApiProperties;

    @Resource
    private TwitterAccountManageMapper twitterAccountMapper;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public TwitterServiceImpl() {
        // 创建OkHttpClient，设置超时时间
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean followTwitter(String twitterUrl) {
        try {
            log.info("开始关注Twitter账号: {}", twitterUrl);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("group_id", 0);
            requestBody.put("twitter_url", twitterUrl);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 构建请求
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonBody);

            Request request = new Request.Builder()
                    .url(twitterApiProperties.getBaseUrl() + twitterApiProperties.getFollowUrl())
                    .post(body)
                    .addHeader("User-Agent", twitterApiProperties.getUserAgent())
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("authorization", twitterApiProperties.getAuthorization())
                    .addHeader("sec-ch-ua", "\"Chromium\";v=\"140\", \"Not=A?Brand\";v=\"24\", \"Google Chrome\";v=\"140\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("origin", "https://alpha.apidance.pro")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("referer", "https://alpha.apidance.pro/")
                    .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .addHeader("priority", "u=1, i")
                    .addHeader("Cookie", twitterApiProperties.getCookie())
                    .build();

            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    // 注意：body()只能读取一次，所以先读取字符串
                    String responseBody = response.body().string();
                    log.info("关注Twitter响应: {}", responseBody);

                    // 解析响应
                    boolean success = false;
                    String jsonPart = extractJson(responseBody);
                    
                    try {
                        TwitterApiResponse<?> apiResponse = objectMapper.readValue(jsonPart, TwitterApiResponse.class);

                        if (apiResponse.isSuccess()) {
                            log.info("关注Twitter成功: {}", twitterUrl);
                            success = true;
                        } else if ("user already exist".equals(apiResponse.getMsg())) {
                            // 用户已存在，认为是成功的（已经关注过了）
                            log.info("Twitter账号已存在（已关注）: {}", twitterUrl);
                            success = true;
                        } else {
                            log.warn("关注Twitter失败: {}, 错误码: {}, 错误信息: {}", twitterUrl, apiResponse.getCode(), apiResponse.getMsg());
                        }
                    } catch (Exception e) {
                        log.error("解析关注Twitter响应失败: {}, JSON内容: {}", twitterUrl, jsonPart, e);
                        // 如果响应包含 "code":1，认为成功
                        if (jsonPart.contains("\"code\":1")) {
                            log.info("根据响应内容判断关注Twitter成功: {}", twitterUrl);
                            success = true;
                        } else if (jsonPart.contains("user already exist")) {
                            log.info("根据响应内容判断Twitter账号已存在: {}", twitterUrl);
                            success = true;
                        }
                    }
                    
                    // 如果关注成功，更新数据库
                    if (success) {
                        // 先确保账号存在
                        getOrCreateAccount(twitterUrl);
                        // 更新关注状态
                        twitterAccountMapper.updateFollowStatus(twitterUrl);
                    }
                    return success;
                } else {
                    log.error("关注Twitter请求失败: {}, HTTP状态码: {}", twitterUrl, response.code());
                    return false;
                }
            }

        } catch (IOException e) {
            log.error("关注Twitter异常: {}", twitterUrl, e);
            return false;
        }
    }

    @Override
    public boolean unfollowTwitter(String twitterUrl) {
        try {
            log.info("开始取消关注Twitter账号: {}", twitterUrl);

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("twitter_url", twitterUrl);

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 构建请求
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, jsonBody);

            Request request = new Request.Builder()
                    .url(twitterApiProperties.getBaseUrl() + twitterApiProperties.getUnfollowUrl())
                    .post(body)
                    .addHeader("User-Agent", twitterApiProperties.getUserAgent())
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Encoding", "gzip, deflate, br, zstd")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("authorization", twitterApiProperties.getAuthorization())
                    .addHeader("sec-ch-ua", "\"Chromium\";v=\"140\", \"Not=A?Brand\";v=\"24\", \"Google Chrome\";v=\"140\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("origin", "https://alpha.apidance.pro")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("referer", "https://alpha.apidance.pro/")
                    .addHeader("accept-language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .addHeader("priority", "u=1, i")
                    .addHeader("Cookie", twitterApiProperties.getCookie())
                    .build();

            // 执行请求
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    log.info("取消关注Twitter响应: {}", responseBody);

                    // 解析响应
                    boolean success = false;
                    String jsonPart = extractJson(responseBody);
                    
                    try {
                        TwitterApiResponse<?> apiResponse = objectMapper.readValue(jsonPart, TwitterApiResponse.class);

                        if (apiResponse.isSuccess()) {
                            log.info("取消关注Twitter成功: {}", twitterUrl);
                            success = true;
                        } else {
                            log.warn("取消关注Twitter失败: {}, 错误码: {}, 错误信息: {}", twitterUrl, apiResponse.getCode(), apiResponse.getMsg());
                        }
                    } catch (Exception e) {
                        log.error("解析取消关注Twitter响应失败: {}, JSON内容: {}", twitterUrl, jsonPart, e);
                        // 如果响应包含 "code":1，认为成功
                        if (jsonPart.contains("\"code\":1")) {
                            log.info("根据响应内容判断取消关注Twitter成功: {}", twitterUrl);
                            success = true;
                        }
                    }
                    
                    // 如果取消关注成功，更新数据库
                    if (success) {
                        // 先确保账号存在
                        getOrCreateAccount(twitterUrl);
                        // 更新取消关注状态
                        twitterAccountMapper.updateUnfollowStatus(twitterUrl);
                    }
                    return success;
                } else {
                    log.error("取消关注Twitter请求失败: {}, HTTP状态码: {}", twitterUrl, response.code());
                    return false;
                }
            }

        } catch (IOException e) {
            log.error("取消关注Twitter异常: {}", twitterUrl, e);
            return false;
        }
    }

    @Override
    public int batchFollow(List<String> twitterUrls) {
        if (twitterUrls == null || twitterUrls.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String twitterUrl : twitterUrls) {
            if (followTwitter(twitterUrl)) {
                successCount++;
            }
            // 添加延迟，避免请求过快
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("批量关注被中断");
                break;
            }
        }

        log.info("批量关注完成，成功: {}/{}", successCount, twitterUrls.size());
        return successCount;
    }

    @Override
    public int batchUnfollow(List<String> twitterUrls) {
        if (twitterUrls == null || twitterUrls.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String twitterUrl : twitterUrls) {
            if (unfollowTwitter(twitterUrl)) {
                successCount++;
            }
            // 添加延迟，避免请求过快
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("批量取消关注被中断");
                break;
            }
        }

        log.info("批量取消关注完成，成功: {}/{}", successCount, twitterUrls.size());
        return successCount;
    }

    @Override
    public TwitterAccountManage getOrCreateAccount(String twitterUrl) {
        if (twitterUrl == null || twitterUrl.isEmpty()) {
            return null;
        }

        // 先尝试查询
        TwitterAccountManage account = twitterAccountMapper.selectByTwitterUrl(twitterUrl);
        if (account != null) {
            return account;
        }

        // 如果不存在，则创建
        account = new TwitterAccountManage();
        account.setTwitterUrl(twitterUrl);
        
        // 从URL提取用户名并判断类型
        if (twitterUrl.contains("/status/")) {
            account.setTwitterType("tweet");
            account.setTwitterUsername(null);
        } else if (twitterUrl.contains("/communities/")) {
            account.setTwitterType("community");
            account.setTwitterUsername(null);
        } else {
            account.setTwitterType("profile");
            // 提取用户名（例如：https://x.com/latinaliquid -> @latinaliquid）
            try {
                String username = twitterUrl.replaceAll("https?://", "")
                        .replaceAll("(www\\.)?(x\\.com|twitter\\.com)/", "")
                        .split("/")[0]
                        .split("\\?")[0];
                account.setTwitterUsername("@" + username);
            } catch (Exception e) {
                log.warn("解析Twitter用户名失败: {}", twitterUrl, e);
            }
        }
        
        account.setIsFollowing(0);
        account.setRelatedTokenCount(0);
        account.setFollowCount(0);
        account.setPushStatus("1");
        account.setEnableFollowPush(0);
        account.setEnableTweetPush(0);
        account.setEnableRetweetPush(0);
        account.setEnableReplyPush(0);
        account.setEnableAvatarPush(0);
        account.setCreateBy("system");

        try {
            twitterAccountMapper.insertTwitterAccount(account);
            log.info("创建Twitter账号成功: {}", twitterUrl);
            return account;
        } catch (Exception e) {
            log.error("创建Twitter账号失败: {}", twitterUrl, e);
            // 可能是并发导致的重复插入，重新查询
            return twitterAccountMapper.selectByTwitterUrl(twitterUrl);
        }
    }

    @Override
    public Map<String, TwitterAccountManage> batchGetAccounts(List<String> twitterUrls) {
        if (twitterUrls == null || twitterUrls.isEmpty()) {
            return new HashMap<>();
        }

        // 批量查询已存在的账号
        List<TwitterAccountManage> accounts = twitterAccountMapper.selectByTwitterUrls(twitterUrls);
        Map<String, TwitterAccountManage> accountMap = accounts.stream()
                .collect(Collectors.toMap(TwitterAccountManage::getTwitterUrl, account -> account));

        // 找出不存在的Twitter URL，创建它们
        List<String> missingUrls = twitterUrls.stream()
                .filter(url -> !accountMap.containsKey(url))
                .collect(Collectors.toList());

        for (String url : missingUrls) {
            TwitterAccountManage newAccount = getOrCreateAccount(url);
            if (newAccount != null) {
                accountMap.put(url, newAccount);
            }
        }

        return accountMap;
    }

    @Override
    public boolean updatePushConfig(TwitterAccountManage twitterAccount) {
        if (twitterAccount == null || twitterAccount.getTwitterUrl() == null) {
            return false;
        }

        try {
            // 确保账号存在
            TwitterAccountManage existing = getOrCreateAccount(twitterAccount.getTwitterUrl());
            if (existing == null) {
                log.error("Twitter账号不存在: {}", twitterAccount.getTwitterUrl());
                return false;
            }

            // 更新推送配置
            int result = twitterAccountMapper.updatePushConfig(twitterAccount);
            if (result > 0) {
                log.info("更新Twitter推送配置成功: {}", twitterAccount.getTwitterUrl());
                return true;
            } else {
                log.warn("更新Twitter推送配置失败: {}", twitterAccount.getTwitterUrl());
                return false;
            }
        } catch (Exception e) {
            log.error("更新Twitter推送配置异常: {}", twitterAccount.getTwitterUrl(), e);
            return false;
        }
    }

    /**
     * 从响应内容中提取JSON部分（处理gzip压缩导致的乱码）
     * 
     * @param responseBody 响应内容
     * @return JSON字符串
     */
    private String extractJson(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "{}";
        }
        
        // 尝试找到JSON的开始位置（{ 或 [）
        int jsonStart = -1;
        for (int i = 0; i < responseBody.length(); i++) {
            char c = responseBody.charAt(i);
            if (c == '{' || c == '[') {
                jsonStart = i;
                break;
            }
        }
        
        if (jsonStart == -1) {
            log.warn("响应中未找到JSON内容: {}", responseBody);
            return "{}";
        }
        
        // 从找到的位置开始截取
        return responseBody.substring(jsonStart);
    }
}

