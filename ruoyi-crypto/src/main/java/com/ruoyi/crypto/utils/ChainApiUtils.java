package com.ruoyi.crypto.utils;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.config.*;
import com.ruoyi.crypto.service.ApiSupplier;
import okhttp3.*;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import static com.ruoyi.common.core.domain.AjaxResult.error;
import static com.ruoyi.common.core.domain.AjaxResult.success;

@Component
public class ChainApiUtils {
    
    @Resource
    private GmgnProperties gmgnProperties;
    
    @Resource
    private MoralisProperties moralisProperties;
    
    @Resource
    private DexProperties dexProperties;
    
    @Resource
    private AxiomProperties axiomProperties;
    
    @Resource
    private GoPlusProperties goPlusProperties;

    @Resource
    private ProxyProperties proxyProperties;

    private String GMGN_TOKEN_INFO = "";
    private String GMGN_TOKEN_WALLET = "";
    private String GMGN_TOKEN_HOLDERS = "";
    private String GMGN_TOKEN_SECURITY = "";
    private String GMGN_COMMON_PARAM = "";

    private String AXIOM_PAIR_INFO = "";
    private String AXIOM_TOKEN_INFO = "";
    private String MORALIS_TOKEN_PAIR = "";
    private String MORALIS_TOKEN_TRADE = "";
    private String MORALIS_PAIR_INFO = "";
    private String DEX_TOKEN_INFO = "";
    private String GO_PLUS_SECURITY = "";

    @PostConstruct
    public void init() {
        System.setProperty("http.proxyHost", proxyProperties.getHttpHost());
        System.setProperty("http.proxyPort", String.valueOf(proxyProperties.getHttpPort()));
        System.setProperty("https.proxyHost", proxyProperties.getHttpsHost());
        System.setProperty("https.proxyPort", String.valueOf(proxyProperties.getHttpsPort()));

        GMGN_TOKEN_INFO = gmgnProperties.getTokenInfoUrl();
        GMGN_TOKEN_WALLET = gmgnProperties.getTokenWalletUrl();
        GMGN_TOKEN_HOLDERS = gmgnProperties.getTokenHoldersUrl();
        GMGN_TOKEN_SECURITY = gmgnProperties.getTokenSecurityUrl();
        GMGN_COMMON_PARAM = "?device_id=" + gmgnProperties.getDeviceId()
                + "&client_id=" + gmgnProperties.getClient_id()
                + "&from_app=" + gmgnProperties.getFrom_app()
                + "&app_ver=" + gmgnProperties.getApp_ver()
                + "&tz_name=" + gmgnProperties.getTz_name()
                + "&tz_offset=" + gmgnProperties.getTz_offset()
                + "&app_lang=" + gmgnProperties.getApp_lang()
                + "&fp_did=" + gmgnProperties.getFp_did()
                + "&os=" + gmgnProperties.getOs();

        AXIOM_PAIR_INFO = axiomProperties.getPairInfoUrl();
        AXIOM_TOKEN_INFO = axiomProperties.getTokenPairUrl();

        MORALIS_TOKEN_PAIR = moralisProperties.getTokenPairUrl();
        MORALIS_PAIR_INFO = moralisProperties.getPairInfoUrl();
        MORALIS_TOKEN_TRADE = moralisProperties.getTokenTradeUrl();

        DEX_TOKEN_INFO = dexProperties.getTokenPairUrl();

        GO_PLUS_SECURITY = goPlusProperties.getTokenSecurityUrl();
    }

    // 池子地址缓存，避免重复请求dex API
    private final Map<String, String> PAIR_ADDRESS_CACHE = new ConcurrentHashMap<>();
    
    // 缓存过期时间（毫秒），设置为30分钟
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000;
    private final Map<String, Long> CACHE_TIME_MAP = new ConcurrentHashMap<>();

    /**
     * 通过池子地址直接获取axiom代币信息（避免重复调用dex）
     * @param pairAddress 池子地址
     * @return
     */
    private String getAxiomTokenInfoByPairAddress(String pairAddress){
        
        if(StringUtils.isEmpty(pairAddress)){
            return null;
        }
        String url = axiomProperties.getTokenPairUrl() + pairAddress;
        String body = doGet(url, axiomProperties.getHeaders());
        return body;
    }

    /**
     * 代币信息GMGN
     * @param address
     * @param chainType
     * @return
     */
    public AjaxResult getGMGNTokenInfo(String address, String chainType) {
        Map<String, Object> param = new HashMap<>();
        param.put("chain", chainType);
        param.put("addresses", Collections.singletonList(address));
        String json = JSONUtil.toJsonStr(param);

        String referer = "https://gmgn.ai/" + chainType + "/token/" + address;
        Map<String, String> headers = new HashMap<>(gmgnProperties.getHeaders());
        headers.put("referer", referer);

        String body = doPost(GMGN_TOKEN_INFO + GMGN_COMMON_PARAM, headers, json);
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
    public String getGMGNTokenWallet(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_WALLET);
    }

    /**
     * 代币持有人信息GMGN
     * @param address
     * @param chainType
     * @return
     */
    public String getGMGNTokenHolders(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_HOLDERS);
    }

    /**
     * 代币安全信息GMGN
     * @param address
     * @param chainType
     * @return
     */
    public String getGMGNTokenSecurity(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_SECURITY);
    }

    /**
     * 代币池子信息axiom
     * @param address
     * @return
     */
    public String getAxiomPairInfo(String address){
        AjaxResult dexPairAddress = getDexPairInfo(address);
        if(dexPairAddress.isSuccess()){
            JSONObject jsonObject = JSONUtil.parseObj(dexPairAddress.get(AjaxResult.DATA_TAG));
            String pairAddress = jsonObject.getStr("pairAddress", "");
            if(StringUtils.isNotEmpty(pairAddress)) {
                String url = AXIOM_PAIR_INFO + pairAddress;
                String body = doGet(url, axiomProperties.getHeaders());
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
    public String getAxiomTokenInfo(String address){
        AjaxResult dexPairAddress = getDexPairInfo(address);
        if(dexPairAddress.isSuccess()){
            JSONObject jsonObject = JSONUtil.parseObj(dexPairAddress.get(AjaxResult.DATA_TAG));
            String pairAddress = jsonObject.getStr("pairAddress", "");
            if(StringUtils.isNotEmpty(pairAddress)){
                String url = AXIOM_TOKEN_INFO + pairAddress;
                String body = doGet(url, axiomProperties.getHeaders());
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
    public AjaxResult getMoralisTokenPair(String address){
        String url = MORALIS_TOKEN_PAIR + address + "/pairs";
        String result = doGet(url, moralisProperties.getHeaders());
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
    public String getMoralisPairInfo(String pairAddress){
        String url = MORALIS_PAIR_INFO + pairAddress + "/stats";
        String get = doGet(url, moralisProperties.getHeaders());
        return get;
    }

    public String getMoralisTokenTrade(String address, String chainType){
        String url = MORALIS_TOKEN_TRADE + address + "/analytics?chain=" + chainType;
        String get = doGet(url, moralisProperties.getHeaders());
        return get;
    }


    /**
     * 代币信息 dex
     */
    public AjaxResult getDexPairInfo(String address){
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
    private String getCachedPairAddress(String tokenAddress) {
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
    private void cachePairAddress(String tokenAddress, String pairAddress) {
        PAIR_ADDRESS_CACHE.put(tokenAddress, pairAddress);
        CACHE_TIME_MAP.put(tokenAddress, System.currentTimeMillis());
    }

    /**
     * 清理过期缓存
     */
    public void cleanExpiredCache() {
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
    public AjaxResult getGoPlusTokenSecurity(String address) {
        String result = HttpUtil.get(GO_PLUS_SECURITY + address);
        if(StringUtils.isEmpty(result)){
            return error("查询ca信息失败！");
        }
        if(!JSONUtil.isTypeJSON(result)){
            return error("查询ca信息为空！");
        }

        return success(result);
    }

    private String getGMGNResult(String address, String chainType, String gmgnUrl){
        String url = gmgnUrl + chainType + "/" + address + GMGN_COMMON_PARAM;
        String referer = "https://gmgn.ai/" + chainType + "/token/" + address;
        Map<String, String> headers = new HashMap<>(gmgnProperties.getHeaders());
        headers.put("referer", referer);
        String body = doGet(url, headers);
        return body;
    }

    public String doGet(String url, Map<String, String> headers) {
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

    public String doPost(String url, Map<String, String> headers, String json) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json.getBytes()))
                .headers(Headers.of(headers))
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            String string = response.body().string();
            return string;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private boolean isValidGMGNResponse(String json) {
        // 判断请求是否成功
        JSONObject obj = JSONUtil.parseObj(json);
        String code = obj.getStr("code");
        if(!"0".equals(code)){
            return true;
        }
        return false;
    }

    private boolean isValidMoralisResponse(String json) {
        // 判断请求是否成功
        JSONObject obj = JSONUtil.parseObj(json);
        JSONArray pairs = obj.getJSONArray("pairs");
        if(!pairs.isEmpty()) {
            return true;
        }
        return false;
    }

    private boolean isValidAxiomResponse(String json) {
        // 判断请求是否成功
        JSONObject obj = JSONUtil.parseObj(json);
        if(obj.containsKey("error")){
            return false;
        }
        return true;
    }

    public AjaxResult tryChainApis(ApiSupplier... apis) {
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
