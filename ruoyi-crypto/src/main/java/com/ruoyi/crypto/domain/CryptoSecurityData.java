package com.ruoyi.crypto.domain;

import lombok.Data;

@Data
public class CryptoSecurityData {
    private String holders;
    private Double top10Percent;
    private String ownerAddress;
    private Double creatorTokenBalance;
    private Boolean isMintable;
    private Boolean isFreezable;
    private Boolean isClosable;
    private Double feeRate;
    private Boolean dexFlag;
    private String riskTag;
    private Boolean isHoneypot;
    private String riskLevel;
    private Integer dexscrAd;
    private Integer dexscrUpdateLink;
    private Integer ctoFlag;
}
