package com.ruoyi.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "crypto.moralis")
public class MoralisProperties {

    private String tokenPairUrl;
    private String pairInfoUrl;
    private String tokenTradeUrl;
    private Map<String, String> headers;
}
