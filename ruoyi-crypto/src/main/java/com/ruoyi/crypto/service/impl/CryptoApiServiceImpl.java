package com.ruoyi.crypto.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.service.CryptoApiService;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

import static com.ruoyi.common.core.domain.AjaxResult.*;

@Service
public class CryptoApiServiceImpl implements CryptoApiService {

    String tokenInfoUrl = "https://api.dexscreener.com/latest/dex/tokens/";
    String tokenSecurityUrl = "https://api.gopluslabs.io/api/v1/solana/token_security?contract_addresses=";

    static {
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", "1081");
        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "1081");
    }

    @Override
    public AjaxResult getTokenInfo(String address) {
        String result = HttpUtil.get(tokenInfoUrl + address);
        if(StringUtils.isEmpty(result)){
            return error("查询ca信息失败！");
        }
        if(!JSONUtil.isJson(result)){
            return error("查询ca信息为空！");
        }
        JSONObject jsonObject = JSONUtil.parseObj(result);
        JSONArray pairs = jsonObject.getJSONArray("pairs");
        if(pairs.isEmpty()){
            return error("未查询到此ca信息");
        }
        if(pairs.size() == 1){
            return success(pairs.get(0));
        }

        JSONObject maxPair = pairs.stream()
                .map(item -> (JSONObject) item)
                .max(Comparator.comparing(pairsJson -> {
                    JSONObject liq = pairsJson.getJSONObject("liquidity");
                    if(liq == null) return 0.0;
                    try {
                        return liq.getDouble("usd", 0.0);
                    }catch (Exception e){
                        return 0.0;
                    }
                })).orElse(null);
        return success(maxPair);
    }

    @Override
    public AjaxResult getTokenSecurityInfo(String address) {
        JSONObject json = new JSONObject();
        boolean isHoneypot = false; //是否貔貅
        String riskTag = "";

        String result = HttpUtil.get(tokenSecurityUrl + address);
        if(StringUtils.isEmpty(result)){
            return error("查询ca信息失败！");
        }
        if(!JSONUtil.isJson(result)){
            return error("查询ca信息为空！");
        }
        JSONObject jsonObject = JSONUtil.parseObj(result);
        String code = jsonObject.getStr("code", "-1");
        String message = jsonObject.getStr("message", "查询失败");
        if(!"1".equals(code)){
            return error(message);
        }
        JSONObject res = jsonObject.getJSONObject("result").getJSONObject(address);
        //持有人
        String holders = res.getStr("holder_count", "0");
        //前十
        JSONArray holdersArray = res.getJSONArray("holders");
        AtomicReference<Double> percent = new AtomicReference<>(0.00);
        if(!JSONUtil.isNull(holdersArray)){
            holdersArray.forEach(item -> {
                JSONObject holder = (JSONObject) item;
                Double percentDouble = Double.parseDouble(holder.getStr("percent"));
                percent.updateAndGet(v -> v + percentDouble);
            });
        }
        //dev信息
        String ownerAddress = "";
        if(!JSONUtil.isNull(res.getJSONArray("creators")) && res.getJSONArray("creators").size() > 0){
            ownerAddress = res.getJSONArray("creators").getJSONObject(0).getStr("address", "");
        }
        //是否可增发
        String mintableStat = "1";
        if(res.getJSONObject("mintable").size() > 0){
            mintableStat = res.getJSONObject("mintable").getStr("status");
        }
        //是否可冻结
        String freezableStat = "1";
        if(res.getJSONObject("freezable").size() > 0){
            freezableStat = res.getJSONObject("freezable").getStr("status");
        }
        //是否可销毁
        String closableStat = "1";
        if(res.getJSONObject("closable").size() > 0){
            closableStat = res.getJSONObject("closable").getStr("status");
        }
        //税率
        String feeRate = "0";
        if(res.getJSONObject("transfer_fee").size() > 0){
            feeRate = res.getJSONObject("transfer_fee").getJSONObject("current_fee_rate").getStr("fee_rate", "0");
        }
        //是否上dex
        boolean dexFlag = false;
        if(!JSONUtil.isNull(res.getJSONArray("dex"))){
            dexFlag = true;
        }

        if ("1".equals(mintableStat)) riskTag += "⚠️ 可增发 ";
        if ("1".equals(freezableStat)) riskTag += "⚠️ 可冻结（黑名单） ";
        if ("1".equals(closableStat)) riskTag += "⚠️ 可销毁 ";
        if (Double.parseDouble(feeRate) >= 0.20) {
            riskTag += "🔥 高税率 ";
            isHoneypot = true; //
        };
        if (!dexFlag) riskTag += "❗未上DEX ";
        if (percent.get() > 0.3) riskTag += "💣 Top10持仓高 ";
        if (isHoneypot) riskTag += "🚨 疑似貔貅 ";

        json.append("holders", holders);
        json.append("top10Percent", percent);
        json.append("ownerAddress", ownerAddress);
        json.append("isMintable", mintableStat);
        json.append("isFreezable", freezableStat);
        json.append("isClosable", closableStat);
        json.append("feeRate", feeRate);
        json.append("dexFlag", dexFlag);
        json.append("riskTag", riskTag);
        json.append("isHoneypot", isHoneypot);
        return success(json);
    }
}
