package com.ruoyi.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;


@Data
@Component
@ConfigurationProperties(prefix = "crypto.gmgn")
public class GmgnProperties {

    private String tokenInfoUrl;
    private String tokenWalletUrl;
    private String tokenHoldersUrl;
    private String tokenSecurityUrl;
    private String deviceId;
    private String clientId;
    private String fromApp;
    private String appVer;
    private String tzName;
    private String tzOffset;
    private String appLang;
    private String fpDid;
    private String os;
    private Map<String, String> headers;
}
