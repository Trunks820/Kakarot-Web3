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
        log.info("开始获取机器人状态，类型: {}", type);
        String url = "http://tgalert-app:5000/api/status";
        log.info("调用API: {}", url);
        
        AjaxResult botApi = BotApiUtils.getBotApi(url);
        if(botApi.isError()){
            log.error("API调用失败: {}", botApi.get("msg"));
            return botApi;
        }

        try {
            log.info("API调用成功，开始解析响应数据");
            // 直接获取已经解析好的data对象
            Object dataObj = botApi.get("data");
            log.debug("响应数据类型: {}", dataObj.getClass().getSimpleName());
            
            // 如果是JSONObject，直接使用；如果是字符串，则解析
            JSONObject fullResponse;
            if (dataObj instanceof JSONObject) {
                fullResponse = (JSONObject) dataObj;
                log.debug("数据已经是JSONObject格式");
            } else {
                // 如果是字符串，则解析
                String jsonString = dataObj.toString();
                log.debug("原始响应数据: {}", jsonString);
                fullResponse = JSONUtil.parseObj(jsonString);
            }

            // 获取bots信息
            JSONObject bots = fullResponse.getJSONObject("bots");
            
            // 获取指定类型的机器人信息
            JSONObject botInfo = bots.getJSONObject(type);
            botInfo.put("name", type);
            log.info("机器人状态获取成功，类型: {}, 状态: {}", type, botInfo.getStr("status"));
            return AjaxResult.success(botInfo);
        } catch (Exception e) {
            log.error("解析机器人状态数据失败，类型: {}, 错误: {}", type, e.getMessage(), e);
            return AjaxResult.error("解析机器人状态数据失败：" + e.getMessage());
        }
    }

    @Override
    public AjaxResult restartTgBot(String name) {
        log.info("开始重启机器人: {}", name);
        String url = "http://tgalert-app:5000/api/bot/" + name + "/restart";
        log.info("调用重启API: {}", url);
        
        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            log.error("重启机器人失败，机器人: {}, 错误: {}", name, ajaxResult.get("msg"));
            return ajaxResult;
        } else{
            log.info("重启机器人成功: {}", name);
            return AjaxResult.success();
        }
    }

    @Override
    public AjaxResult startTgBot(String name) {
        log.info("开始启动机器人: {}", name);
        String url = "http://tgalert-app:5000/api/bot/" + name + "/start";
        log.info("调用启动API: {}", url);
        
        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            log.error("启动机器人失败，机器人: {}, 错误: {}", name, ajaxResult.get("msg"));
            return ajaxResult;
        } else{
            log.info("启动机器人成功: {}", name);
            return AjaxResult.success();
        }
    }

    @Override
    public AjaxResult stopTgBot(String name) {
        log.info("开始停止机器人: {}", name);
        String url = "http://tgalert-app:5000/api/bot/" + name + "/stop";
        log.info("调用停止API: {}", url);
        
        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            log.error("停止机器人失败，机器人: {}, 错误: {}", name, ajaxResult.get("msg"));
            return ajaxResult;
        } else{
            log.info("停止机器人成功: {}", name);
            return AjaxResult.success();
        }
    }

    @Override
    public AjaxResult startAllTgBot() {
        log.info("开始启动所有机器人");
        String url = "http://tgalert-app:5000/api/bots/start_all";
        log.info("调用启动所有机器人API: {}", url);
        
        AjaxResult ajaxResult = BotApiUtils.postBotApi(url);
        if(ajaxResult.isError()){
            log.error("启动所有机器人失败，错误: {}", ajaxResult.get("msg"));
            return ajaxResult;
        } else{
            log.info("启动所有机器人成功");
            return AjaxResult.success();
        }
    }
}
