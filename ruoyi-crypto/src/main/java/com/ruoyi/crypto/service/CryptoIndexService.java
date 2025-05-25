package com.ruoyi.crypto.service;

import cn.hutool.json.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.domain.vo.CryptoIndexVo;

public interface CryptoIndexService {


    /**
     * 获取当日活动统计
     * @return 包含总查询数和活跃用户数的VO对象
     */
    CryptoIndexVo getDailyActivityStats();

    AjaxResult getTgBotStatus();
}
