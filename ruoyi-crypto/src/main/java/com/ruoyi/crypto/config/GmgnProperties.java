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
    private String client_id;
    private String from_app;
    private String app_ver;
    private String tz_name;
    private String tz_offset;
    private String app_lang;
    private String fp_did;
    private String os;
    private Map<String, String> headers;
}
