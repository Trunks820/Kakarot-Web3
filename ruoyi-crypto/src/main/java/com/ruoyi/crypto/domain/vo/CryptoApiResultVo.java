package com.ruoyi.crypto.domain.vo;

import com.ruoyi.crypto.domain.*;
import lombok.Data;
import java.util.List;

@Data
public class CryptoApiResultVo {
    private String name;
    private String symbol;
    private String address;
    private String chainId;
    private String logoUrl;
    private Double price;
    private Double change24h;
    private Long marketCap;
    private Long volume24h;
    private Double high24h;
    private Double low24h;
    private Integer holderCount;
    private Double liquidity;
    private Boolean hasRenounced;
    private Integer queryCount;
    private Integer todayQueries;
    private Integer monitorCount;
    private Long createTime;
    private CryptoPairInfo pairInfo;               // 交易对信息
    private CryptoRealtimeData realtimeData;       // 实时交易数据
    private CryptoSecurityData cryptoSecurityData; //安全信息
    private CryptoWalletData cryptoWalletData;     //钱包分析
    private List<CryptoSocialLink> socialLinks;    // 社交和官网链接

}
