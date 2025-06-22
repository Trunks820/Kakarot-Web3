package com.ruoyi.crypto.utils;


import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.config.*;
import com.ruoyi.crypto.domain.*;
import com.ruoyi.crypto.domain.vo.CryptoApiResultVo;
import com.ruoyi.crypto.domain.vo.CryptoWalletData;
import com.ruoyi.crypto.service.ApiSupplier;
import okhttp3.*;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.ruoyi.common.core.domain.AjaxResult.*;

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
    
    @Resource
    private CryptoConfigManager configManager;

    private String GMGN_TOKEN_INFO = "";
    private String GMGN_TOKEN_WALLET = "";
    private String GMGN_TOKEN_HOLDERS = "";
    private String GMGN_TOKEN_SECURITY = "";
    private String GMGN_WALLET_UNFOLLOW = "";
    private String GMGN_WALLET_FOLLOW = "";
    private String GMGN_WALLET_ACTIVITY = "";

    private String GMGN_TOKEN_Stat = "";

    private String GMGN_TOKEN_SMART = "";
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
        // 设置代理，添加空值检查

//        System.setProperty("http.proxyHost", proxyProperties.getHttpHost());
//        System.setProperty("http.proxyPort", String.valueOf(proxyProperties.getPort()));
//        System.setProperty("https.proxyHost", proxyProperties.getHttpsHost());
//        System.setProperty("https.proxyPort", String.valueOf(proxyProperties.getPort()));

        GMGN_TOKEN_INFO = gmgnProperties.getTokenInfoUrl();
        GMGN_TOKEN_WALLET = gmgnProperties.getTokenWalletUrl();
        GMGN_TOKEN_HOLDERS = gmgnProperties.getTokenHoldersUrl();
        GMGN_TOKEN_SECURITY = gmgnProperties.getTokenSecurityUrl();
        GMGN_TOKEN_Stat = gmgnProperties.getTokenStatUrl();
        GMGN_TOKEN_SMART = gmgnProperties.getTokenSmartTradeUrl();
        GMGN_WALLET_UNFOLLOW = gmgnProperties.getWalletUnfollowUrl();
        GMGN_WALLET_FOLLOW = gmgnProperties.getWalletFollowUrl();
        GMGN_WALLET_ACTIVITY = gmgnProperties.getWalletActivityUrl();
        AXIOM_PAIR_INFO = axiomProperties.getPairInfoUrl();
        AXIOM_TOKEN_INFO = axiomProperties.getTokenPairUrl();
        MORALIS_TOKEN_PAIR = moralisProperties.getTokenPairUrl();
        MORALIS_PAIR_INFO = moralisProperties.getPairInfoUrl();
        MORALIS_TOKEN_TRADE = moralisProperties.getTokenTradeUrl();
        DEX_TOKEN_INFO = dexProperties.getTokenInfoUrl();
        GO_PLUS_SECURITY = goPlusProperties.getTokenSecurityUrl();

        // GMGN的动态参数现在将从数据库读取，这里不再硬编码
        // 先暂时保留兜底值，避免启动时出错

        GMGN_COMMON_PARAM = "device_id=" + (gmgnProperties.getDeviceId() != null ? gmgnProperties.getDeviceId() : "")
                + "&client_id=" + (gmgnProperties.getClientId() != null ? gmgnProperties.getClientId() : "")
                + "&from_app=" + (gmgnProperties.getFromApp() != null ? gmgnProperties.getFromApp() : "")
                + "&app_ver=" + (gmgnProperties.getAppVer() != null ? gmgnProperties.getAppVer() : "")
                + "&tz_name=" + (gmgnProperties.getTzName() != null ? gmgnProperties.getTzName() : "")
                + "&tz_offset=" + (gmgnProperties.getTzOffset() != null ? gmgnProperties.getTzOffset() : "")
                + "&app_lang=" + (gmgnProperties.getAppLang() != null ? gmgnProperties.getAppLang() : "")
                + "&fp_did=" + (gmgnProperties.getFpDid() != null ? gmgnProperties.getFpDid() : "")
                + "&os=" + (gmgnProperties.getOs() != null ? gmgnProperties.getOs() : "");
    }

    // 池子地址缓存，避免重复请求dex API
    private final Map<String, String> PAIR_ADDRESS_CACHE = new ConcurrentHashMap<>();
    
    // 缓存过期时间（毫秒），设置为30分钟
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000;
    private final Map<String, Long> CACHE_TIME_MAP = new ConcurrentHashMap<>();

    public AjaxResult getTopCoin(String coin){
        String url = "https://api.gateio.ws/api/v4/spot/tickers?currency_pair=" + coin + "&timezone=utc8";
        String s = HttpUtil.get(url);
        return success("success", s);
    }

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

        // 从配置管理器获取最新的headers和params
        Map<String, String> headers = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        
        try {
            // 获取动态配置，如果获取失败则使用yml配置兜底
            headers = configManager.getHeaders("gmgn");
            params = configManager.getParams("gmgn");
            
            // 如果动态配置为空，使用yml配置兜底
            if (headers.isEmpty() && gmgnProperties != null && gmgnProperties.getHeaders() != null) {
                headers = new HashMap<>(gmgnProperties.getHeaders());
            }
        } catch (Exception e) {
            // 如果配置管理器出错，使用yml配置兜底
            if (gmgnProperties != null && gmgnProperties.getHeaders() != null) {
                headers = new HashMap<>(gmgnProperties.getHeaders());
            }
        }

        // 动态构建参数字符串
        String commonParam = buildGmgnParams(params, gmgnProperties);
        
        String referer = "https://gmgn.ai/" + chainType + "/token/" + address;
        headers.put("referer", referer);

        String body = doPost(GMGN_TOKEN_INFO + commonParam, headers, json);
        if(StringUtils.isEmpty(body)){
            return error("查询ca信息失败！");
        }
        if(!JSONUtil.isJson(body)){
            return error("查询ca信息为空！");
        }
        JSONObject jsonObject = JSONUtil.parseObj(body);
        String code = jsonObject.getStr("code");
        String msg = jsonObject.getStr("message");
        if(!"0".equals(code)){
            return error(msg);
        }

        JSONArray jsonArray = jsonObject.getJSONArray("data");
        if(jsonArray == null){
            return error("数据为空！");
        }

        JSONObject data = jsonArray.getJSONObject(0);
        if(JSONUtil.isNull(data)){
            return error("数据为空！");
        }

        String gmgnTokenWallet = getGMGNTokenWallet(address, chainType);
        if(StringUtils.isNotEmpty(gmgnTokenWallet) && JSONUtil.isJson(gmgnTokenWallet)){
            JSONObject jsonWallet = JSONUtil.parseObj(gmgnTokenWallet);
            code = jsonWallet.getStr("code");
            if("0".equals(code)){
                JSONObject walletData = jsonWallet.getJSONObject("data");
                data.append("walletData", walletData);
            }
        }

        String gmgnTokenSmart = getGMGNTokenSmart(address, chainType);
        if(StringUtils.isNotEmpty(gmgnTokenSmart) && JSONUtil.isJson(gmgnTokenSmart)){
            JSONObject jsonSmart = JSONUtil.parseObj(gmgnTokenSmart);
            code = jsonSmart.getStr("code");
            if("0".equals(code)){
                JSONObject smartData = jsonSmart.getJSONObject("data");
                System.err.println(smartData.getJSONArray("history"));
                data.append("smartData", smartData);
            }
        }
        return successSource("gmgn", data);
    }
    
    /**
     * 构建GMGN参数字符串
     */
    private String buildGmgnParams(Map<String, String> params, GmgnProperties gmgnProperties) {
        StringBuilder sb = new StringBuilder("?");
        
        // 优先使用动态配置的参数
        sb.append("device_id=").append(params.getOrDefault("device_id", gmgnProperties.getDeviceId()));
        sb.append("&client_id=").append(params.getOrDefault("client_id", gmgnProperties.getClientId()));
        sb.append("&from_app=").append(params.getOrDefault("from_app", gmgnProperties.getFromApp()));
        sb.append("&app_ver=").append(params.getOrDefault("app_ver", gmgnProperties.getAppVer()));
        sb.append("&tz_name=").append(params.getOrDefault("tz_name", gmgnProperties.getTzName()));
        sb.append("&tz_offset=").append(params.getOrDefault("tz_offset", gmgnProperties.getTzOffset()));
        sb.append("&app_lang=").append(params.getOrDefault("app_lang", gmgnProperties.getAppLang()));
        sb.append("&fp_did=").append(params.getOrDefault("fp_did", gmgnProperties.getFpDid()));
        sb.append("&os=").append(params.getOrDefault("os", gmgnProperties.getOs()));
        
        return sb.toString();
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

    public String getGMGNTokenStat(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_Stat);
    }

    public String getGMGNTokenSmart(String address, String chainType){
        return getGMGNResult(address, chainType, GMGN_TOKEN_SMART);
    }

    /**
     * 钱包最新活动
     */
    public String getGmgnWalletActivity(String walletAddress, String chainType){
        String url = GMGN_WALLET_ACTIVITY + "/" + chainType.toLowerCase() + "?type=buy&type=sell&"
                + GMGN_COMMON_PARAM + "&wallet=" + walletAddress + "&limit=50&cost=10";
        String referer = "https://gmgn.ai/" + chainType + "/address/" + walletAddress;
        Map<String, String> headers = new HashMap<>(gmgnProperties.getHeaders());
        headers.put("referer", referer);
        String body = doGet(url, headers);
        return body;
    }

    public String gmgnWalletUnfollow(String walletAddress, String chainType){
        chainType = chainType.toLowerCase();
        Map<String, Object> param = new HashMap<>();
        param.put("chain", chainType);
        param.put("network", chainType);
        param.put("address", walletAddress);
        param.put("wallet_addresses", Collections.singletonList(walletAddress));
        param.put("group_ids", Collections.singletonList("657f4761-1b15-4b16-990f-f3e4b734fd41"));
        String json = JSONUtil.toJsonStr(param);

        // 从配置管理器获取最新的headers和params
        Map<String, String> headers = new HashMap<>();
        Map<String, String> params = new HashMap<>();

        try {
            // 获取动态配置，如果获取失败则使用yml配置兜底
            headers = configManager.getHeaders("gmgn");
            params = configManager.getParams("gmgn");

            // 如果动态配置为空，使用yml配置兜底
            if (headers.isEmpty() && gmgnProperties != null && gmgnProperties.getHeaders() != null) {
                headers = new HashMap<>(gmgnProperties.getHeaders());
            }
        } catch (Exception e) {
            // 如果配置管理器出错，使用yml配置兜底
            if (gmgnProperties != null && gmgnProperties.getHeaders() != null) {
                headers = new HashMap<>(gmgnProperties.getHeaders());
            }
        }

        // 动态构建参数字符串
        String commonParam = buildGmgnParams(params, gmgnProperties);

        String referer = "https://gmgn.ai/" + chainType + "/address/" + walletAddress;
        headers.put("referer", referer);

        String url = GMGN_WALLET_UNFOLLOW + commonParam;
        String body = doPost(GMGN_WALLET_UNFOLLOW + commonParam, headers, json);
        return body;
    }

    public String gmgnWalletFollow(String walletAddress, String chainType){
        chainType = chainType.toLowerCase();

        Map<String, Object> param = new HashMap<>();
        param.put("chain", chainType);
        param.put("network", chainType);
        param.put("address", walletAddress);
        param.put("wallet_addresses", Collections.singletonList(walletAddress));
        param.put("group_ids", Collections.singletonList("657f4761-1b15-4b16-990f-f3e4b734fd41"));
        String json = JSONUtil.toJsonStr(param);

        // 从配置管理器获取最新的headers和params
        Map<String, String> headers = new HashMap<>();
        Map<String, String> params = new HashMap<>();

        try {
            // 获取动态配置，如果获取失败则使用yml配置兜底
            headers = configManager.getHeaders("gmgn");
            params = configManager.getParams("gmgn");

            // 如果动态配置为空，使用yml配置兜底
            if (headers.isEmpty() && gmgnProperties != null && gmgnProperties.getHeaders() != null) {
                headers = new HashMap<>(gmgnProperties.getHeaders());
            }
        } catch (Exception e) {
            // 如果配置管理器出错，使用yml配置兜底
            if (gmgnProperties != null && gmgnProperties.getHeaders() != null) {
                headers = new HashMap<>(gmgnProperties.getHeaders());
            }
        }

        // 动态构建参数字符串
        String commonParam = buildGmgnParams(params, gmgnProperties);

        String referer = "https://gmgn.ai/" + chainType + "/address/" + walletAddress;
        headers.put("referer", referer);

        String body = doPost(GMGN_WALLET_FOLLOW + commonParam, headers, json);
        return body;
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

        return successSource("moralis", selectedPair);

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
//        String cachedPairAddress = getCachedPairAddress(address);
//        if (StringUtils.isNotEmpty(cachedPairAddress)) {
//            // 构造缓存的返回数据
//            JSONObject cachedData = new JSONObject();
//            cachedData.put("pairAddress", cachedPairAddress);
//            return success(cachedData);
//        }
        
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

        CryptoApiResultVo cryptoApiResultVo = convertFromDex(selectedPair);
        return successSource("dex", cryptoApiResultVo);
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

        return success("success", result);
    }

    private String getGMGNResult(String address, String chainType, String gmgnUrl){
        String url = gmgnUrl + "/" + chainType + "/" + address + "?" + GMGN_COMMON_PARAM;
        String referer = "https://gmgn.ai/" + chainType + "/token/" + address;
        if(GMGN_TOKEN_SMART.equals(gmgnUrl)){
            url += "&limt=" + gmgnProperties.getLimit()
                    + "&event=" + gmgnProperties.getEvent()
                    + "&maker="
                    + "&tag=" + gmgnProperties.getTag();
            referer += "?tag=" + gmgnProperties.getTag() + "&filter=buy";
        }
        Map<String, String> headers = new HashMap<>(gmgnProperties.getHeaders());
        headers.put("referer", referer);
        String body = doGet(url, headers);
        return body;
    }

    public String doGet(String url, Map<String, String> headers) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
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

    public String doPost(String url, Map<String, String> headers, String json) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
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

    public AjaxResult tryChainApis(ApiSupplier... apis) {
        for (ApiSupplier api : apis) {
            try {
                AjaxResult result = api.get();
                String dataObj = result.get("data") + "";
                if (result != null && result.isSuccess() && dataObj != null) {
                    String source = result.get("source") + "";
                    CryptoApiResultVo vo = new CryptoApiResultVo();
                    if ("gmgn".equals(source)) {
                        vo = convertFromGmgn(dataObj);
                    } else if ("moralis".equals(source)) {
                        vo = convertFromMoralis(dataObj);
                    } else if ("dex".equals(source)) {
                        JSONObject jsonObject = JSONUtil.parseObj("dataObj");
                        vo = convertFromDex(jsonObject);
                    } else {
                        return error("未知数据来源！");
                    }
                    return success(vo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return AjaxResult.error("所有API都失败");
    }

    public CryptoApiResultVo convertFromGmgn(Object dataObj) {
        JSONObject obj = JSONUtil.parseObj(dataObj);
        CryptoApiResultVo vo = new CryptoApiResultVo();
        CryptoPairInfo pairInfo = new CryptoPairInfo();
        CryptoSecurityData securityData = new CryptoSecurityData();
        CryptoRealtimeData cryptoRealtimeData = new CryptoRealtimeData();
        CryptoWalletData cryptoWalletData = new CryptoWalletData();
        vo.setAddress(obj.getStr("address"));
        vo.setSymbol(obj.getStr("symbol"));
        vo.setName(obj.getStr("name"));
        vo.setLogoUrl(obj.getStr("logo"));
        vo.setCreateTime(obj.getLong("creation_timestamp"));
        vo.setHolderCount(obj.getInt("holder_count"));
        vo.setLiquidity(obj.getDouble("liquidity"));
        Double circulatingSupply = obj.getDouble("circulating_supply");
        // Pool信息
        JSONObject poolJson = obj.getJSONObject("pool");
        if (poolJson != null) {
            pairInfo.setExchange(poolJson.getStr("exchange"));
            pairInfo.setFeeRatio(poolJson.getDouble("fee_ratio"));
            securityData.setFeeRate(poolJson.getDouble("fee_ratio"));
            vo.setPairInfo(pairInfo);
        }
        // Dev信息
        JSONObject devJson = obj.getJSONObject("dev");
        if (devJson != null) {
            securityData.setOwnerAddress(devJson.getStr("creator_address"));
            securityData.setCreatorTokenBalance(devJson.getDouble("creator_token_balance"));
            securityData.setTop10Percent(devJson.getDouble("top_10_holder_rate"));
            securityData.setDexscrAd(devJson.getInt("dexscr_ad"));
            securityData.setDexscrUpdateLink(devJson.getInt("dexscr_update_link"));
            securityData.setCtoFlag(devJson.getInt("cto_flag"));
            vo.setCryptoSecurityData(securityData);
        }
        // Price信息
        JSONObject priceJson = obj.getJSONObject("price");
        if (priceJson != null) {
            vo.setPrice(priceJson.getDouble("price"));
            double market = circulatingSupply * priceJson.getDouble("price");
            vo.setMarketCap((long)market);
            // Txns（构建前端要的结构）
            Map<String, CryptoTxnStats> txns = new HashMap<>();
            txns.put("m5", newTxnStats(priceJson, "buys_5m", "sells_5m"));
            txns.put("h1", newTxnStats(priceJson, "buys_1h", "sells_1h"));
            txns.put("h6", newTxnStats(priceJson, "buys_6h", "sells_6h"));
            txns.put("h24", newTxnStats(priceJson, "buys_24h", "sells_24h"));
            cryptoRealtimeData.setTxns(txns);
            // Volume
            Map<String, Double> volume = new HashMap<>();
            volume.put("m5", priceJson.getDouble("volume_5m"));
            volume.put("h1", priceJson.getDouble("volume_1h"));
            volume.put("h6", priceJson.getDouble("volume_6h"));
            volume.put("h24", priceJson.getDouble("volume_24h"));
            cryptoRealtimeData.setVolume(volume);
            // PriceChange
            Map<String, Double> priceChange = new HashMap<>();
            priceChange.put("m5", calcPriceChange(priceJson.getDouble("price_5m"), priceJson.getDouble("price")));
            priceChange.put("h1", calcPriceChange(priceJson.getDouble("price_1h"), priceJson.getDouble("price")));
            priceChange.put("h6", calcPriceChange(priceJson.getDouble("price_6h"), priceJson.getDouble("price")));
            priceChange.put("h24", calcPriceChange(priceJson.getDouble("price_24h"), priceJson.getDouble("price")));
            cryptoRealtimeData.setPriceChange(priceChange);
            vo.setRealtimeData(cryptoRealtimeData);
        }
        // Wallet信息
        JSONObject walletData = obj.getJSONArray("walletData").getJSONObject(0);
        if(walletData != null){
            cryptoWalletData.setSmartWallets(walletData.getInt("smart_wallets"));
            cryptoWalletData.setFreshWallets(walletData.getInt("fresh_wallets"));
            cryptoWalletData.setRenownedWallets(walletData.getInt("renowned_wallets"));
            cryptoWalletData.setSniperWallets(walletData.getInt("sniper_wallets"));
            cryptoWalletData.setRatTraderWallets(walletData.getInt("rat_trader_wallets"));
            cryptoWalletData.setWhaleWallets(walletData.getInt("whale_wallets"));
            cryptoWalletData.setTopWallets(walletData.getInt("top_wallets"));
            cryptoWalletData.setFollowingWallets(walletData.getInt("following_wallets"));
            cryptoWalletData.setBundlerWallets(walletData.getInt("bundler_wallets"));
            vo.setCryptoWalletData(cryptoWalletData);
        }
        System.err.println(vo);
        return vo;
    }

    // 小工具方法
    private CryptoTxnStats newTxnStats(JSONObject priceJson, String buyKey, String sellKey) {
        CryptoTxnStats tx = new CryptoTxnStats();
        tx.setBuys(priceJson.getInt(buyKey, 0));
        tx.setSells(priceJson.getInt(sellKey, 0));
        return tx;
    }

    // 价格涨跌幅换算，百分比（涨跌=（现价-历史价）/历史价*100）
    private Double calcPriceChange(Double oldPrice, Double nowPrice) {
        if (oldPrice == null || oldPrice == 0) return 0.0;
        return ((nowPrice - oldPrice) / oldPrice) * 100;
    }

    public CryptoApiResultVo convertFromMoralis(String dataObj){
        CryptoApiResultVo vo = new CryptoApiResultVo();
        return vo;
    }

    public CryptoApiResultVo convertFromDex(JSONObject obj){
        JSONObject baseToken = obj.getJSONObject("baseToken");
        JSONObject liquidity = obj.getJSONObject("liquidity");
        JSONObject info = obj.getJSONObject("info");

        CryptoApiResultVo vo = new CryptoApiResultVo();
        CryptoPairInfo pairInfo = new CryptoPairInfo();
        CryptoRealtimeData cryptoRealtimeData = new CryptoRealtimeData();

        vo.setChainId(obj.getStr("chainId"));
        vo.setAddress(baseToken.getStr("address"));
        vo.setSymbol(baseToken.getStr("symbol"));
        vo.setName(baseToken.getStr("name"));
        vo.setCreateTime(obj.getLong("pairCreatedAt"));
        vo.setLiquidity(liquidity.getDouble("usd"));
        vo.setPrice(obj.getDouble("priceUsd"));
        pairInfo.setExchange(obj.getStr("dexId"));


        JSONObject txnsJson = obj.getJSONObject("txns");
        JSONObject volumeJson = obj.getJSONObject("volume");
        JSONObject priceChangeJson = obj.getJSONObject("priceChange");
        // Txns（构建前端要的结构）
        Map<String, CryptoTxnStats> txns = new HashMap<>();
        String buyKey = "buys";
        String sellKey = "sells";
        txns.put("m5", newTxnStats(txnsJson, buyKey, sellKey));
        txns.put("h1", newTxnStats(txnsJson, buyKey, sellKey));
        txns.put("h6", newTxnStats(txnsJson, buyKey, sellKey));
        txns.put("h24", newTxnStats(txnsJson, buyKey, sellKey));
        cryptoRealtimeData.setTxns(txns);
        // Volume

        Map<String, Double> volume = new HashMap<>();
        volume.put("m5", volumeJson.getDouble("5m"));
        volume.put("h1", volumeJson.getDouble("1h"));
        volume.put("h6", volumeJson.getDouble("6h"));
        volume.put("h24", volumeJson.getDouble("24h"));
        cryptoRealtimeData.setVolume(volume);
        // PriceChange
        Map<String, Double> priceChange = new HashMap<>();
        priceChange.put("m5", priceChangeJson.getDouble("5m"));
        priceChange.put("h1", priceChangeJson.getDouble("h1"));
        priceChange.put("h6", priceChangeJson.getDouble("h6"));
        priceChange.put("h24", priceChangeJson.getDouble("h24"));
        cryptoRealtimeData.setPriceChange(priceChange);
        vo.setRealtimeData(cryptoRealtimeData);

        // Social
        // vo.setSocialLinks(...);
        vo.setLogoUrl(info.getStr("imageUrl"));
        List<CryptoSocialLink> list = new ArrayList<>();
        JSONArray websites = info.getJSONArray("websites");
        for (Object website : websites) {
            CryptoSocialLink cryptoSocialLink = new CryptoSocialLink();
            JSONObject jsonObject = JSONUtil.parseObj(website);
            String type = jsonObject.getStr("type");
            String url = jsonObject.getStr("url");
            cryptoSocialLink.setUrl(url);
            cryptoSocialLink.setType(type);
            list.add(cryptoSocialLink);
        }

        JSONArray socials = info.getJSONArray("socials");
        for (Object social : socials) {
            CryptoSocialLink cryptoSocialLink = new CryptoSocialLink();
            JSONObject jsonObject = JSONUtil.parseObj(social);
            String type = jsonObject.getStr("type");
            String url = jsonObject.getStr("url");
            cryptoSocialLink.setUrl(url);
            cryptoSocialLink.setType(type);
            list.add(cryptoSocialLink);
        }
        vo.setSocialLinks(list);
        // 官网、白皮书等字段
        return vo;
    }

}
