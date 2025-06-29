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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CryptoIndexServiceImpl implements CryptoIndexService {

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
//        String url = "http://108.160.135.74:5000/api/status";
        AjaxResult botApi = BotApiUtils.getBotApi(url);
        if(botApi.isError()){
            return botApi;
        }

        try {
            String jsonString = botApi.get("data") + "";
            // 移除可能的前后空白字符
            jsonString = jsonString.trim();
            
            // 解析完整的Python API响应
            JSONObject fullResponse = JSONUtil.parseObj(jsonString);

            // 获取bots信息
            JSONObject bots = fullResponse.getJSONObject("bots");
            
            // 获取指定类型的机器人信息
            JSONObject botInfo = bots.getJSONObject(type);
            botInfo.put("name", type);
            return AjaxResult.success(botInfo);
        } catch (Exception e) {
            return AjaxResult.error("解析机器人状态数据失败：" + e.getMessage());
        }
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
