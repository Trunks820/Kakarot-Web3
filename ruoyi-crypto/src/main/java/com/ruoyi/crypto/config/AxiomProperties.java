package com.ruoyi.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "crypto.axiom")
public class AxiomProperties {

    private String tokenPairUrl;
    private String pairInfoUrl;
    private Map<String, String> headers;
}
