package com.ruoyi.crypto.domain;

import lombok.Data;

import java.util.List;

@Data
public class CryptoPairInfo {
    private String dexId;
    private String chainId;
    private String pairAddress;
    private String url;
    private List<String> labels;
    private Long pairCreatedAt;
    private String exchange;
    private Double feeRatio;
}
