package com.ruoyi.crypto.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.service.CryptoApiService;
import com.ruoyi.crypto.utils.AddressUtils;
import com.ruoyi.crypto.utils.ChainApiUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicReference;
import static com.ruoyi.common.core.domain.AjaxResult.*;

@Service
public class CryptoApiServiceImpl implements CryptoApiService {

    @Resource
    private ChainApiUtils chainApiUtils;

    @Override
    public AjaxResult getTokenInfo(String text) {
        /**
         * 2025-06-04
         * 处理地址和链的判定流程：
         * 1. 先用正则匹配文本，尝试直接提取区块链地址和链类型。
         * 2. 如果是 sol 地址，优先调用 gmgn 的 API 获取数据，若失败则依次尝试 moralis、dex 作为备用。
         * 3. 如果不是 sol 地址，先用 dex 的 API 判断该地址属于哪条公链，然后带上链类型和地址去查 gmgn 的 API（备用只查 dex）。
         *
         * 实现策略：
         * - 保证查询顺序优先级（Sol: gmgn > moralis > dex，其他链: gmgn > dex）
         * - 便于扩展更多链类型和查询逻辑
         * - 目前只支持 sol\eth\base\bsc
         * - 如后续支持新链，需在正则和判定逻辑处增加分支
         */

        //获取文本中的地址
        String address = AddressUtils.findAddress(text);
        if(StringUtils.isEmpty(address)){
            return error("请输入正确的地址！");
        }

        //确立哪条链 sol/evm
        String chainType = AddressUtils.findChainType(address);
        if(StringUtils.isEmpty(chainType)){
            return error("无法识别该地址所属的公链类型！");
        }

        if("sol".equals(chainType)){
            // sol直接按规则走
            String finalChainType = chainType;
            AjaxResult result = chainApiUtils.tryChainApis(
                    () -> chainApiUtils.getGMGNTokenInfo(address, finalChainType),
                    () -> chainApiUtils.getMoralisTokenPair(address),
                    () -> chainApiUtils.getDexPairInfo(address)
            );
            return result;
        } else{
            // evm需要先判定具体是哪个链
            AjaxResult dexPairInfo = chainApiUtils.getDexPairInfo(address);
            if(dexPairInfo.isSuccess()){
                JSONObject jsonObject = JSONUtil.parseObj(dexPairInfo.get("data"));
                chainType = jsonObject.getStr("chainId");
                if("ethereum".equals(chainType)){
                    chainType = "eth";
                }
            } else{
                return error("查询公链失败，请稍后！");
            }

            // 使用gmgn
            AjaxResult gmgnTokenInfo = chainApiUtils.getGMGNTokenInfo(address, chainType);
            if(gmgnTokenInfo.isSuccess()){
                Object data = gmgnTokenInfo.get("data");
                if(!JSONUtil.isTypeJSON(data + "")){
                    return error("datadata");
                }
                return gmgnTokenInfo;
            }
            return dexPairInfo;
        }
    }

    @Override
    public AjaxResult getTokenSecurityInfo(String address) {
        JSONObject json = new JSONObject();
        boolean isHoneypot = false; //是否貔貅
        String riskTag = "";

        AjaxResult ajaxResult = chainApiUtils.getGoPlusTokenSecurity(address);
        if(ajaxResult.isError()){
            return ajaxResult;
        }

        JSONObject jsonObject = JSONUtil.parseObj(ajaxResult.get(DATA_TAG));
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
        if(!JSONUtil.isNull(res.getJSONArray("creators")) && !res.getJSONArray("creators").isEmpty()){
            ownerAddress = res.getJSONArray("creators").getJSONObject(0).getStr("address", "");
        }
        //是否可增发
        String mintableStat = "1";
        if(!res.getJSONObject("mintable").isEmpty()){
            mintableStat = res.getJSONObject("mintable").getStr("status");
        }
        //是否可冻结
        String freezableStat = "1";
        if(!res.getJSONObject("freezable").isEmpty()){
            freezableStat = res.getJSONObject("freezable").getStr("status");
        }
        //是否可销毁
        String closableStat = "1";
        if(!res.getJSONObject("closable").isEmpty()){
            closableStat = res.getJSONObject("closable").getStr("status");
        }
        //税率
        String feeRate = "0";
        if(!res.getJSONObject("transfer_fee").isEmpty()){
            feeRate = res.getJSONObject("transfer_fee").getJSONObject("current_fee_rate").getStr("fee_rate", "0");
        }
        //是否上dex
        boolean dexFlag = false;
        if(!JSONUtil.isNull(res.getJSONArray("dex"))){
            dexFlag = true;
        }

        double rate = (double) (Integer.parseInt(feeRate)) / 10000;
        if ("1".equals(mintableStat)) riskTag += "⚠️ 可增发 ";
        if ("1".equals(freezableStat)) riskTag += "⚠️ 可冻结（黑名单） ";
        if ("1".equals(closableStat)) riskTag += "⚠️ 可销毁 ";
        if (rate >= 0.20) {
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
        json.append("feeRate", rate);
        json.append("dexFlag", dexFlag);
        json.append("riskTag", riskTag);
        json.append("isHoneypot", isHoneypot);
        return success(json);
    }


    @Override
    public AjaxResult getTopCoin(String coin) {
        AjaxResult topCoin = chainApiUtils.getTopCoin(coin);
        Object data = topCoin.get("data");
        if(data == null){
            return error("未查询到此价格");
        }
        JSONArray objects = JSONUtil.parseArray(data.toString());
        if(objects.isEmpty()){
            return error("未查询到此价格");
        }
        JSONObject object = JSONUtil.parseObj(objects.get(0));
        return success(object);
    }
}
