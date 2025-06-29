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
        AjaxResult botApi = BotApiUtils.getBotApi(url);
        if(botApi.isError()){
            return botApi;
        }

        JSONObject jsonObject = JSONUtil.parseObj(botApi.get("data") + "");
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject telegram = data.getJSONObject("bots").getJSONObject(type);
        return AjaxResult.success(telegram);
    }

    @Override
    public AjaxResult restartTgBot(String pid) {
        String url = "http://tgalert-app:5000/api/bot/" + pid + "/restart";
        return BotApiUtils.postBotApi(url);
    }

    @Override
    public AjaxResult startTgBot(String pid) {
        String url = "http://tgalert-app:5000/api/bot/" + pid + "/start";
        return BotApiUtils.postBotApi(url);
    }

    @Override
    public AjaxResult stopTgBot(String pid) {
        String url = "http://tgalert-app:5000/api/bot/" + pid + "/stop";
        return BotApiUtils.postBotApi(url);
    }

    @Override
    public AjaxResult startAllTgBot() {
        String url = "http://tgalert-app:5000/api/bots/start_all";
        return BotApiUtils.postBotApi(url);
    }
}
