package com.ruoyi.crypto.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.domain.vo.CryptoIndexVO;
import com.ruoyi.crypto.domain.vo.CryptoUserVO;
import com.ruoyi.crypto.mapper.CryptoCaQueryRecordMapper;
import com.ruoyi.crypto.mapper.CryptoUserMapper;
import com.ruoyi.crypto.service.CryptoIndexService;
import com.ruoyi.crypto.utils.BotApiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CryptoIndexServiceImpl implements CryptoIndexService {

    private static final Logger log = LoggerFactory.getLogger(CryptoIndexServiceImpl.class);

    @Resource
    private CryptoCaQueryRecordMapper cryptoCaQueryRecordMapper;

    @Resource
    private CryptoUserMapper cryptoUserMapper;

    @Override
    public CryptoIndexVO getDailyActivityStats() {
        return cryptoCaQueryRecordMapper.getDailyActivityStats();
    }


    @Override
    public List<CryptoUserVO> getUserRange(){
        return cryptoUserMapper.getUserRange();
    }

    @Override
    public AjaxResult getBotStatus(String type) {
        String url = "http://tgalert-app:5000/api/status";

//        AjaxResult botApi = BotApiUtils.getBotApi(url);
//        if(botApi.isError()){
//            return botApi;
//        }
//
//        try {
//            // 直接获取已经解析好的data对象
//            Object dataObj = botApi.get("data");
//
//            // 如果是JSONObject，直接使用；如果是字符串，则解析
//            JSONObject fullResponse;
//            if (dataObj instanceof JSONObject) {
//                fullResponse = (JSONObject) dataObj;
//            } else {
//                // 如果是字符串，则解析
//                String jsonString = dataObj.toString();
//                fullResponse = JSONUtil.parseObj(jsonString);
//            }
//
//            // 获取bots信息
//            JSONObject bots = fullResponse.getJSONObject("bots");
//
//            // 获取指定类型的机器人信息
//            JSONObject botInfo = bots.getJSONObject(type);
//            botInfo.put("name", type);
//            return AjaxResult.success(botInfo);
            return AjaxResult.success();
//        } catch (Exception e) {
//            return AjaxResult.error("解析机器人状态数据失败：" + e.getMessage());
//        }
    }

    @Override
    public AjaxResult restartTgBot(String name) {
        String url = "http://tgalert-app:5000/api/bot/" + name + "/restart";

        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            return ajaxResult;
        } else{
            return AjaxResult.success();
        }
    }

    @Override
    public AjaxResult startTgBot(String name) {
        String url = "http://tgalert-app:5000/api/bot/" + name + "/start";

        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            return ajaxResult;
        } else{
            return AjaxResult.success();
        }
    }

    @Override
    public AjaxResult stopTgBot(String name) {
        String url = "http://tgalert-app:5000/api/bot/" + name + "/stop";

        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            return ajaxResult;
        } else{
            return AjaxResult.success();
        }
    }

    @Override
    public AjaxResult startAllTgBot() {
        String url = "http://tgalert-app:5000/api/bots/start_all";
        
        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            return ajaxResult;
        } else{
            return AjaxResult.success();
        }
    }


}
