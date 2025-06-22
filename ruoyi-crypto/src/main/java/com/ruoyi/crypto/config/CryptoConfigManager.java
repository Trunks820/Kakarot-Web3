package com.ruoyi.crypto.config;

import com.ruoyi.crypto.domain.CryptoDynamicConfig;
import com.ruoyi.crypto.mapper.CryptoDynamicConfigMapper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CryptoConfigManager {

    @Resource
    private CryptoDynamicConfigMapper configMapper;
    
    @Resource
    private GmgnProperties gmgnProperties;
    
    @Resource
    private MoralisProperties moralisProperties;
    
    @Resource
    private AxiomProperties axiomProperties;
    
    @Resource
    private DexProperties dexProperties;
    
    @Resource
    private GoPlusProperties goPlusProperties;

    // 缓存所有配置
    private volatile Map<String, ApiProviderConfig> configCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            loadAllConfigs();
        } catch (Exception e) {
            // 如果数据库还没有创建表，先忽略错误，避免启动失败
            System.err.println("Warning: Failed to load dynamic config from database: " + e.getMessage());
        }
    }

    /**
     * 获取指定提供商的完整配置
     */
    public ApiProviderConfig getProviderConfig(String provider) {
        return configCache.get(provider);
    }

    /**
     * 获取指定提供商的headers
     */
    public Map<String, String> getHeaders(String provider) {
        ApiProviderConfig config = configCache.get(provider);
        return config != null ? config.getHeaders() : new HashMap<>();
    }

    /**
     * 获取指定提供商的参数
     */
    public Map<String, String> getParams(String provider) {
        ApiProviderConfig config = configCache.get(provider);
        return config != null ? config.getParams() : new HashMap<>();
    }

    /**
     * 重新加载所有配置
     */
    public void reloadConfigs() {
        loadAllConfigs();
    }

    private void loadAllConfigs() {
        // 从数据库加载动态配置
        List<CryptoDynamicConfig> dbConfigs = configMapper.selectAllEnabled();

        Map<String, ApiProviderConfig> newCache = new HashMap<>();

        // 按provider分组
        Map<String, List<CryptoDynamicConfig>> groupedConfigs =
                dbConfigs.stream().collect(Collectors.groupingBy(CryptoDynamicConfig::getProvider));

        for (Map.Entry<String, List<CryptoDynamicConfig>> entry : groupedConfigs.entrySet()) {
            String provider = entry.getKey();
            List<CryptoDynamicConfig> configs = entry.getValue();

            ApiProviderConfig providerConfig = new ApiProviderConfig();
            providerConfig.setProvider(provider);

            Map<String, String> headers = new HashMap<>();
            Map<String, String> params = new HashMap<>();

            for (CryptoDynamicConfig config : configs) {
                if ("headers".equals(config.getConfigGroup())) {
                    headers.put(config.getConfigKey(), config.getConfigValue());
                } else if ("params".equals(config.getConfigGroup())) {
                    params.put(config.getConfigKey(), config.getConfigValue());
                }
            }

            providerConfig.setHeaders(headers);
            providerConfig.setParams(params);
            // URLs从yml的Properties中获取
            providerConfig.setUrls(getUrlsFromProperties(provider));

            newCache.put(provider, providerConfig);
        }

        this.configCache = newCache;
    }
    
    /**
     * 从Properties获取URL配置
     */
    private Map<String, String> getUrlsFromProperties(String provider) {
        Map<String, String> urls = new HashMap<>();
        
        switch (provider.toLowerCase()) {
            case "gmgn":
                if (gmgnProperties != null) {
                    urls.put("tokenInfoUrl", gmgnProperties.getTokenInfoUrl());
                    urls.put("tokenWalletUrl", gmgnProperties.getTokenWalletUrl());
                    urls.put("tokenHoldersUrl", gmgnProperties.getTokenHoldersUrl());
                    urls.put("tokenSecurityUrl", gmgnProperties.getTokenSecurityUrl());
                    urls.put("tokenStatUrl", gmgnProperties.getTokenStatUrl());
                    urls.put("tokenSmartTradeUrl", gmgnProperties.getTokenSmartTradeUrl());
                    urls.put("walletUnfollowUrl", gmgnProperties.getWalletUnfollowUrl());
                    urls.put("walletFollowUrl", gmgnProperties.getWalletFollowUrl());
                    urls.put("walletActivityUrl", gmgnProperties.getWalletActivityUrl());
                }
                break;
            case "moralis":
                if (moralisProperties != null) {
                    urls.put("tokenPairUrl", moralisProperties.getTokenPairUrl());
                    urls.put("pairInfoUrl", moralisProperties.getPairInfoUrl());
                    urls.put("tokenTradeUrl", moralisProperties.getTokenTradeUrl());
                }
                break;
            case "axiom":
                if (axiomProperties != null) {
                    urls.put("tokenPairUrl", axiomProperties.getTokenPairUrl());
                    urls.put("pairInfoUrl", axiomProperties.getPairInfoUrl());
                }
                break;
            case "dex":
                if (dexProperties != null) {
                    urls.put("tokenInfoUrl", dexProperties.getTokenInfoUrl());
                }
                break;
            case "goplus":
                if (goPlusProperties != null) {
                    urls.put("tokenSecurityUrl", goPlusProperties.getTokenSecurityUrl());
                }
                break;
        }
        
        return urls;
    }
}