package com.ruoyi.crypto.domain;

import lombok.Data;

@Data
public class CryptoSocialLink {
    private String type;   // 比如 website, twitter, telegram
    private String url;
    private String label;  // 备注显示用
}
