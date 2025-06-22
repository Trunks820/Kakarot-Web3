package com.ruoyi.crypto.service;

import cn.hutool.json.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;

public interface CryptoApiService {


    AjaxResult getTokenInfo(String address);

    AjaxResult getTokenSecurityInfo(String address);

    AjaxResult getTopCoin(String coin);

    AjaxResult getWalletActivity(String address, String chainType);

    AjaxResult WalletUnfollow(String address, String chainType);

    AjaxResult WalletFollow(String address, String chainType);

}
