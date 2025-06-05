package com.ruoyi.crypto.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * API提供商配置聚合类
 * @author kakarot
 * @date 2025-06-05
 */
@Data
public class ApiProviderConfig {
    
    /** 提供商名称 */
    private String provider;
    
    /** headers配置 */
    private Map<String, String> headers = new HashMap<>();
    
    /** 参数配置 */
    private Map<String, String> params = new HashMap<>();
    
    /** URL配置（从yml读取） */
    private Map<String, String> urls = new HashMap<>();

//    public String getProvider() {
//        return provider;
//    }
//
//    public void setProvider(String provider) {
//        this.provider = provider;
//    }
//
//    public Map<String, String> getHeaders() {
//        return headers;
//    }
//
//    public void setHeaders(Map<String, String> headers) {
//        this.headers = headers;
//    }
//
//    public Map<String, String> getParams() {
//        return params;
//    }
//
//    public void setParams(Map<String, String> params) {
//        this.params = params;
//    }
//
//    public Map<String, String> getUrls() {
//        return urls;
//    }
//
//    public void setUrls(Map<String, String> urls) {
//        this.urls = urls;
//    }
//
//    /**
//     * 获取指定的header值
//     */
//    public String getHeader(String key) {
//        return headers.get(key);
//    }
//
//    /**
//     * 获取指定的参数值
//     */
//    public String getParam(String key) {
//        return params.get(key);
//    }
//
//    /**
//     * 获取指定的URL
//     */
//    public String getUrl(String key) {
//        return urls.get(key);
//    }
}
