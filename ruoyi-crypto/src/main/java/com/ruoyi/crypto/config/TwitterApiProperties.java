package com.ruoyi.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Twitter API配置
 *
 * @author ruoyi
 */
@Data
@Component
@ConfigurationProperties(prefix = "twitter.api")
public class TwitterApiProperties {

    /**
     * API基础URL
     */
    private String baseUrl = "https://alpha.apidance.pro/api/v1";

    /**
     * 关注接口路径
     */
    private String followUrl = "/user/follow";

    /**
     * 取消关注接口路径
     */
    private String unfollowUrl = "/user/unfollow";

    /**
     * 授权Token
     */
    private String authorization;

    /**
     * Cookie
     */
    private String cookie;

    /**
     * User-Agent
     */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36";
}

