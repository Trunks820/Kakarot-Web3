package com.ruoyi.crypto.domain;

import lombok.Data;

@Data
public class CryptoWalletData {
    /* 聪明钱 */
    private Integer smartWallets;
    /* 新钱包 */
    private Integer freshWallets;
    /* KOL/VC */
    private Integer renownedWallets;
    /* 狙击者 */
    private Integer sniperWallets;
    /* 老鼠仓 */
    private Integer ratTraderWallets;
    /* 鲸鱼 */
    private Integer whaleWallets;
    /* 持仓大户 */
    private Integer topWallets;
    /* 跟单钱包 */
    private Integer followingWallets;
    /* 捆绑交易 */
    private Integer bundlerWallets;
}
