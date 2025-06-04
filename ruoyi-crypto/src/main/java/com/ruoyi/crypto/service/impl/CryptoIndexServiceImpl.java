package com.ruoyi.crypto.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.crypto.domain.vo.CryptoIndexVO;
import com.ruoyi.crypto.domain.vo.CryptoUserVO;
import com.ruoyi.crypto.mapper.CryptoCaQueryRecordMapper;
import com.ruoyi.crypto.mapper.CryptoUserMapper;
import com.ruoyi.crypto.service.CryptoIndexService;
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
    public AjaxResult getTgBotStatus() {
        String url = "http://localhost:5000/api/status";
        try{
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer abcdefg");
            String res = HttpUtils.sendGet(url, headers);
            if(!JSONUtil.isJson(res)){
                return AjaxResult.error("机器人状态请求失败");
            }
            JSONObject jsonObject = JSONUtil.parseObj(res);
            String msg = jsonObject.getStr("msg");
            String code = jsonObject.getStr("code");
            if(!"200".equals(code)){
                return AjaxResult.error(msg);
            }
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject telegram = data.getJSONObject("bots").getJSONObject("telegram_sol");
            return AjaxResult.success(telegram);
        }catch (Exception e){
            return AjaxResult.error("机器人状态请求异常");
        }
    }

    @Override
    public List<CryptoUserVO> getUserRange(){
        return cryptoUserMapper.getUserRange();
    }

}
