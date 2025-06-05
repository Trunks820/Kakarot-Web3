package com.ruoyi.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "proxy")
public class ProxyProperties {
    private String httpHost;
    private Integer httpPort;
    private String httpsHost;
    private Integer httpsPort;
}
