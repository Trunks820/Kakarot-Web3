package com.ruoyi.crypto.utils;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.service.ApiSupplier;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.ruoyi.common.core.domain.AjaxResult.error;
import static com.ruoyi.common.core.domain.AjaxResult.success;

public class ChainApiUtils {

    private static final String X_APP_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJub25jZSI6IjdmYjkxNzAyLTIwY2QtNDVkOS1iNjI5LTY1MzNkMzA2YjA3MCIsIm9yZ0lkIjoiNDUwODc1IiwidXNlcklkIjoiNDYzOTE2IiwidHlwZUlkIjoiOWY1MGNjYTgtODEwNC00NzU0LWI2MDAtYmMyZmM5YzU4MDM5IiwidHlwZSI6IlBST0pFQ1QiLCJpYXQiOjE3NDkwMjEyMDcsImV4cCI6NDkwNDc4MTIwN30.9RgqquajqDX38Df0EauxayYQcDuiFkkwopD5k0BnOCU";
    private static final String GMGN_TOKEN_INFO = "https://gmgn.ai/api/v1/mutil_window_token_info";
    private static final String GMGN_TOKEN_WALLET = "https://gmgn.ai/api/v1/token_wallet_tags_stat";
    private static final String GMGN_TOKEN_HOLDERS = "https://gmgn.ai/vas/api/v1/token_holders";
    private static final String GMGN_TOKEN_SECURITY = "https://gmgn.ai/api/v1/mutil_window_token_security_launchpad";

    private static final String MORALIS_TOKEN_PAIR = "https://solana-gateway.moralis.io/token/mainnet/";

    private static final String MORALIS_PAIR_INFO = "https://solana-gateway.moralis.io/token/mainnet/pairs/";
    private static final String MORALIS_TOKEN_TRADE = "https://deep-index.moralis.io/api/v2.2/tokens/";
    private static final String AXIOM_PAIR_INFO = "https://api2.axiom.trade/pair-info?pairAddress=";

    private static final String AXIOM_TOKEN_INFO = "https://api2.axiom.trade/token-info?pairAddress=";

    private static final String DEX_TOKEN_INFO = "https://api.dexscreener.com/latest/dex/tokens/";

    private static final String GO_PLUS_SECURITY = "https://api.gopluslabs.io/api/v1/solana/token_security?contract_addresses=";

    private static final String GMGN_COMMON_PARAM = "?device_id=5461cc48-2e83-4740-8e90-8409db8cf33d" +
            "&client_id=gmgn_web_20250603-1799-730df90" +
            "&from_app=gmgn&app_ver=20250603-1799-730df90" +
            "&tz_name=Asia%2FShanghai" +
            "&tz_offset=28800" +
            "&app_lang=zh-CN" +
            "&fp_did=68fd1291b5f4f826d98e6378c0e3a663" +
            "&os=web";

    private static final String GMGN_COOKIE = "_ga=GA1.1.704572436.1712248135; _ga_0XM0LYXGC8=deleted; b-user-id=f3b3bb30-0b5c-fc55-f1fa-5af0522175ce; GMGN_LOCALE=zh-CN; GMGN_THEME=dark; sid=gmgn%7C691295b013c0e5714ab5f79835145729; _ga_UGLVBMV4Z0=GS1.2.1748597965489087.a12f85a73d09c2e4fe36d46f9d41ade6.QaPK6yv2T4rQ6xjGBTlGnA%3D%3D.jX0Cij%2B%2BT0w5vaKwjl4PnA%3D%3D.lzf%2FKVwJ2Frr9r77jD4QUQ%3D%3D.H3EBykaM5jrX9MGPSyFtfg%3D%3D; GMGN_CHAIN=sol; __cf_bm=9HnOPa4neqo6CA1vYBePh.sI48rtejnAzm5xHZQordU-1748965722-1.0.1.1-atM7ddn7JsqfjPVOix7QEoTpAl4DAAQ69n277jcJJ9bk0vggGlP6dk4rubolKo8QeCDrav7LG2gX_s4N8ukUiGdq_bUvOuq0ySiZOUclslw; cf_clearance=2PgwveiglxlNmOCwrkQgXTEN.tEdWFmJ0fCisOogrIM-1748966032-1.2.1.1-nqhd1Mx2jE49qDWUWtWiBxLB9h0mzwt9OmLfUy48pGOwZuuGVYEElM1YPK.4tnPbyt7Yt6ov3DDrRHp2eagNwywx81TE9JV8AKNt9KUBskZ2wVjvzK4QFRdjzLDjFghz_ApXecOhczoMe8vUtpLV4Z9Jf5omck4Drnh5u4bV1JwlRpAd5CBhvVTI3T0FKZNHCuVVMLEFJiu7_qXx7pshWYIjC9iId3c6nkG64.r5Y91NSK577fRnxSNOPYBe7BMF9OqalS5_dBRERhZosGkSE2st6MiGQJpKdX7mD2Ef0BWmihC3oz9u.tMO5zL956jsekp2Xrc6LW4NayrO_zxUZWxHsN0Mlzv3VhW5txhILbs; _ga_0XM0LYXGC8=GS2.1.s1748966033$o32$g1$t1748966094$j60$l0$h0";

    public static final Map<String, String> GMGN_COMMON_HEADERS = new HashMap<String, String>() {{
        put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
        put("Accept", "application/json, text/plain, */*");
        put("sentry-trace", "47817276c5b44d07b2627d8c2f221f30-aeb3f1408e5f05b5-0");
        put("sec-ch-ua-platform", "\"Windows\"");
        put("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"");
        put("sec-ch-ua-mobile", "?0");
        put("baggage", "sentry-environment=production,sentry-release=20250603-1799-730df90,sentry-public_key=93c25bab7246077dc3eb85b59d6e7d40,sentry-trace_id=47817276c5b44d07b2627d8c2f221f30,sentry-sample_rate=0.005,sentry-sampled=false");
        put("sec-fetch-site", "same-origin");
        put("sec-fetch-mode", "cors");
        put("sec-fetch-dest", "empty");
        put("accept-language", "zh-CN,zh;q=0.9");
        put("priority", "u=1, i");
        put("Cookie", GMGN_COOKIE);
    }};

    public static final Map<String, String> MORALIS_COMMON_HEADERS = new HashMap<String, String>() {{
        put("Accept", "application/json");
        put("X-API-Key", X_APP_KEY);
    }};

    public static final Map<String, String> AXIOM_COMMON_HEADERS = new HashMap<String, String>() {{
        put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36");
        put("Accept", "application/json, text/plain, */*");
        put("Accept-Encoding", "gzip, deflate, br, zstd");
        put("sec-ch-ua-platform", "\"Windows\"");
        put("sec-ch-ua", "\"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"");
        put("sec-ch-ua-mobile", "?0");
        put("origin", "https://axiom.trade");
        put("sec-fetch-site", "same-site");
        put("sec-fetch-mode", "cors");
        put("sec-fetch-dest", "empty");
        put("referer", "https://axiom.trade/");
        put("accept-language", "zh-CN,zh;q=0.9");
        put("priority", "u=1, i");
        put("Cookie", "auth-refresh-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyZWZyZXNoVG9rZW5JZCI6IjRhODM5ZWJkLTRlMzUtNDUzMy05ZjdiLTBiMjQ5MDhkODlhNSIsImlhdCI6MTc0ODg4Nzc1OH0.-XRWiCkfcswkGH1-T-neknarXIkVR1zK7YopISLXH30; auth-access-token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdXRoZW50aWNhdGVkVXNlcklkIjoiOGQxNmY1OTktNjJlNC00Njk3LTljMTYtZDM0ZmE3M2Y4ZTc2IiwiaWF0IjoxNzQ4OTY0MDI0LCJleHAiOjE3NDg5NjQ5ODR9.hpiLxOeJtIdYkqi5aZFOwbY3qkAv5kLzeeBd9YOc48g");
    }};

    // 池子地址缓存，避免重复请求dex API
    private static final Map<String, String> PAIR_ADDRESS_CACHE = new ConcurrentHashMap<>();
    
    // 缓存过期时间（毫秒），设置为30分钟
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000;
    private static final Map<String, Long> CACHE_TIME_MAP = new ConcurrentHashMap<>();

    public static AjaxResult getTokenInfoAutomatic(String address, String chainType){
        /**
         * 2025-06-04
         * 如果是 sol 地址，优先调用 gmgn 的 API 获取数据，若失败则依次尝试 moralis、dex 作为备用。
         * 如果是evm地址，需要先查询dex 确认是哪条链，然后在调用gmgn
         */
        if("sol".equals(chainType)){
            // sol直接按规则走
            String finalChainType = chainType;
            AjaxResult result = tryChainApis(
                    () -> getGMGNTokenInfo(address, finalChainType),
                    () -> getMoralisTokenPair(address),
                    () -> getDexPairInfo(address)
            );
            return result;
//            AjaxResult gmgnTokenInfo = getGMGNTokenInfo(address, chainType);
//            if(gmgnTokenInfo.isSuccess()){
//                return gmgnTokenInfo;
//            }
//
//            AjaxResult moralisTokenPair = getMoralisTokenPair(address);
//            if(moralisTokenPair.isSuccess()){
//                return moralisTokenPair;
//            }
//
//            AjaxResult dexPairInfo = getDexPairInfo(address);
//            if(dexPairInfo.isSuccess()){
//                return dexPairInfo;
//            }
        } else{
            // evm需要先判定具体是哪个链
            AjaxResult dexPairInfo = getDexPairInfo(address);
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
            AjaxResult gmgnTokenInfo = getGMGNTokenInfo(address, chainType);
            if(gmgnTokenInfo.isSuccess()){
                return gmgnTokenInfo;
            }
            return dexPairInfo;
        }
    }

    /**
     * 通过池子地址直接获取axiom代币信息（避免重复调用dex）
     * @param pairAddress 池子地址
     * @return
     */
    private static String getAxiomTokenInfoByPairAddress(String pairAddress){
        if(StringUtils.isEmpty(pairAddress)){
            return null;
        }
        String url = AXIOM_TOKEN_INFO + pairAddress;
        String body = doGet(url, AXIOM_COMMON_HEADERS);
        return body;
    }

    /**
     * 代币信息GMGN
     * @param address
     * @param chainType
     * @return
     */
    public static AjaxResult getGMGNTokenInfo(String address, String chainType){
        String url = GMGN_TOKEN_INFO + GMGN_COMMON_PARAM;
        Map<String, Object> param = new HashMap<>();
        param.put("chain", chainType);
        param.put("addresses", Collections.singletonList(address));
        String json = JSONUtil.toJsonStr(param);

        String referer = "https://gmgn.ai/" + chainType + "/token/" + address;
        Map<String, String> headers = new HashMap<>(GMGN_COMMON_HEADERS);
        headers.put("referer", referer);
        String body = doPost(url, headers, json);
        if(StringUtils.isEmpty(body)){
            return error("查询ca信息失败！");
        }
        if(!JSONUtil.isJson(body)){
            return error("查询ca信息为空！");
        }
        JSONObject jsonObject = JSONUtil.parseObj(body);
        String code = jsonObject.getStr("code");
        String message = jsonObject.getStr("message");
        if(!"0".equals(code)){
            return error(message);
        }
        JSONArray data = jsonObject.getJSONArray("data");
        return success(data);
    }

    /**
     * 代币钱包信息GMGN
     * @param address
     * @param chainType
     * @return
     */
    public static String getGMGNTokenWallet(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_WALLET);
    }

    /**
     * 代币持有人信息GMGN
     * @param address
     * @param chainType
     * @return
     */
    public static String getGMGNTokenHolders(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_HOLDERS);
    }

    /**
     * 代币安全信息GMGN
     * @param address
     * @param chainType
     * @return
     */
    public static String getGMGNTokenSecurity(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_SECURITY);
    }

    /**
     * 代币池子信息axiom
     * @param address
     * @return
     */
    public static String getAxiomPairInfo(String address){
        AjaxResult dexPairAddress = getDexPairInfo(address);
        if(dexPairAddress.isSuccess()){
            JSONObject jsonObject = JSONUtil.parseObj(dexPairAddress.get(AjaxResult.DATA_TAG));
            String pairAddress = jsonObject.getStr("pairAddress", "");
            if(StringUtils.isNotEmpty(pairAddress)) {
                String url = AXIOM_PAIR_INFO + pairAddress;
                String body = doGet(url, AXIOM_COMMON_HEADERS);
                return body;
            }
        }
        return null;
    }

    /**
     * 代币信息axiom
     * @param address
     * @return
     */
    public static String getAxiomTokenInfo(String address){
        AjaxResult dexPairAddress = getDexPairInfo(address);
        if(dexPairAddress.isSuccess()){
            JSONObject jsonObject = JSONUtil.parseObj(dexPairAddress.get(AjaxResult.DATA_TAG));
            String pairAddress = jsonObject.getStr("pairAddress", "");
            if(StringUtils.isNotEmpty(pairAddress)){
                String url = AXIOM_TOKEN_INFO + pairAddress;
                String body = doGet(url, AXIOM_COMMON_HEADERS);
                return body;
            }
        }
        return null;
    }

    /**
     * 获取代币所有池子信息 Moralis
     * @param address
     * @return
     */
    public static AjaxResult getMoralisTokenPair(String address){
        String url = MORALIS_TOKEN_PAIR + address + "/pairs";
        String result = doGet(url, MORALIS_COMMON_HEADERS);
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
        JSONObject selectedPair;
        if(pairs.size() == 1){
            selectedPair = (JSONObject) pairs.get(0);
        } else {
            selectedPair = pairs.stream()
                    .map(item -> (JSONObject) item)
                    .filter(pairJson -> !pairJson.getBool("inactivePair", false)) // 只要活跃池子
                    .max(Comparator.comparing(pairJson -> {
                        String value = pairJson.getStr("liquidityUsd", "0.0");
                        try {
                            return Double.parseDouble(value);
                        } catch (Exception e) {
                            return 0.0;
                        }
                    })).orElse(null);
        }

        return success(selectedPair);

    }

    /**
     * 获取代币池子信息 Moralis
     * @param pairAddress
     * @return
     */
    public static String getMoralisPairInfo(String pairAddress){
        String url = MORALIS_PAIR_INFO + pairAddress + "/stats";
        String get = doGet(url, MORALIS_COMMON_HEADERS);
        return get;
    }

    public static String getMoralisTokenTrade(String address, String chainType){
        String url = MORALIS_TOKEN_TRADE + address + "/analytics?chain=" + chainType;
        String get = doGet(url, MORALIS_COMMON_HEADERS);
        return get;
    }


    /**
     * 代币信息 dex
     */
    public static AjaxResult getDexPairInfo(String address){
        // 先检查缓存
        String cachedPairAddress = getCachedPairAddress(address);
        if (StringUtils.isNotEmpty(cachedPairAddress)) {
            // 构造缓存的返回数据
            JSONObject cachedData = new JSONObject();
            cachedData.put("pairAddress", cachedPairAddress);
            return success(cachedData);
        }
        
        String result = HttpUtil.get(DEX_TOKEN_INFO + address);
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
        
        JSONObject selectedPair;
        if(pairs.size() == 1){
            selectedPair = (JSONObject) pairs.get(0);
        } else {
            selectedPair = pairs.stream()
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
        }
        
        // 缓存池子地址
        if (selectedPair != null) {
            String pairAddress = selectedPair.getStr("pairAddress");
            if (StringUtils.isNotEmpty(pairAddress)) {
                cachePairAddress(address, pairAddress);
            }
        }
        
        return success(selectedPair);
    }

    /**
     * 获取缓存的池子地址
     * @param tokenAddress 代币地址
     * @return 池子地址，如果缓存不存在或已过期则返回null
     */
    private static String getCachedPairAddress(String tokenAddress) {
        Long cacheTime = CACHE_TIME_MAP.get(tokenAddress);
        if (cacheTime == null || System.currentTimeMillis() - cacheTime > CACHE_EXPIRE_TIME) {
            // 缓存过期，清理缓存
            PAIR_ADDRESS_CACHE.remove(tokenAddress);
            CACHE_TIME_MAP.remove(tokenAddress);
            return null;
        }
        return PAIR_ADDRESS_CACHE.get(tokenAddress);
    }

    /**
     * 缓存池子地址
     * @param tokenAddress 代币地址
     * @param pairAddress 池子地址
     */
    private static void cachePairAddress(String tokenAddress, String pairAddress) {
        PAIR_ADDRESS_CACHE.put(tokenAddress, pairAddress);
        CACHE_TIME_MAP.put(tokenAddress, System.currentTimeMillis());
    }

    /**
     * 清理过期缓存
     */
    public static void cleanExpiredCache() {
        long currentTime = System.currentTimeMillis();
        CACHE_TIME_MAP.entrySet().removeIf(entry -> {
            if (currentTime - entry.getValue() > CACHE_EXPIRE_TIME) {
                PAIR_ADDRESS_CACHE.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * 代币安全信息GoPlus
     * @param address
     * @return
     */
    public static AjaxResult getGoPlusTokenSecurity(String address) {
        String result = HttpUtil.get(GO_PLUS_SECURITY + address);
        if(StringUtils.isEmpty(result)){
            return error("查询ca信息失败！");
        }
        if(!JSONUtil.isTypeJSON(result)){
            return error("查询ca信息为空！");
        }

        return success(result);
    }

    private static String getGMGNResult(String address, String chainType, String gmgnUrl){
        String url = gmgnUrl + chainType + "/" + address + GMGN_COMMON_PARAM;
        String referer = "https://gmgn.ai/" + chainType + "/token/" + address;
        Map<String, String> headers = new HashMap<>(GMGN_COMMON_HEADERS);
        headers.put("referer", referer);
        String body = doGet(url, headers);
        return body;
    }

    public static String doGet(String url, Map<String, String> headers) {
        try {
            HttpResponse response = HttpRequest.get(url)
                    .contentType("application/json")
                    .headerMap(headers, true)
                    .execute();
            return response.body();
        }catch (Exception e){
            return null;
        }
    }

    public static String doPost(String url, Map<String, String> headers, String json) {
        try {
            HttpResponse response = HttpRequest.post(url)
                    .body(json)
                    .contentType("application/json")
                    .headerMap(headers, true)
                    .execute();
            return response.body();
        }catch (Exception e){
            return null;
        }
    }

    private static boolean isValidGMGNResponse(String json) {
        // 判断请求是否成功
        JSONObject obj = JSONUtil.parseObj(json);
        String code = obj.getStr("code");
        if(!"0".equals(code)){
            return true;
        }
        return false;
    }

    private static boolean isValidMoralisResponse(String json) {
        // 判断请求是否成功
        JSONObject obj = JSONUtil.parseObj(json);
        JSONArray pairs = obj.getJSONArray("pairs");
        if(!pairs.isEmpty()) {
            return true;
        }
        return false;
    }

    private static boolean isValidAxiomResponse(String json) {
        // 判断请求是否成功
        JSONObject obj = JSONUtil.parseObj(json);
        if(obj.containsKey("error")){
            return false;
        }
        return true;
    }

    public static AjaxResult tryChainApis(ApiSupplier... apis) {
        for (ApiSupplier api : apis) {
            try {
                AjaxResult result = api.get();
                if (result != null && result.isSuccess()) {
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AjaxResult.error("所有API都失败");
    }


}
