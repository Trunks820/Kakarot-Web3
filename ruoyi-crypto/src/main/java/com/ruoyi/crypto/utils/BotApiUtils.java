package com.ruoyi.crypto.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.http.HttpUtils;
import java.util.HashMap;
import java.util.Map;

public class BotApiUtils {



    public static AjaxResult getBotApi(String url) {
        try{
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer abcdefg");
            String res = HttpUtils.sendGet(url, headers);
            if(!JSONUtil.isJson(res)){
                return AjaxResult.error("机器人请求失败");
            }
            JSONObject jsonObject = JSONUtil.parseObj(res);
            String msg = jsonObject.getStr("msg");
            String code = jsonObject.getStr("code");
            if(!"200".equals(code)){
                return AjaxResult.error(msg);
            }
            return AjaxResult.success(jsonObject.getJSONObject("data"));
        }catch (Exception e){
            return AjaxResult.error("机器人请求异常");
        }
    }

    public static AjaxResult postBotApi(String url) {
        try{
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer abcdefg");
            String res = HttpUtils.sendPost(url, headers);
            if(!JSONUtil.isJson(res)){
                return AjaxResult.error("机器人请求失败");
            }
            JSONObject jsonObject = JSONUtil.parseObj(res);
            String msg = jsonObject.getStr("msg");
            String code = jsonObject.getStr("code");
            if(!"200".equals(code)){
                return AjaxResult.error(msg);
            }
            return AjaxResult.success(jsonObject.getJSONObject("data"));
        }catch (Exception e){
            return AjaxResult.error("机器人请求异常");
        }
    }

}
