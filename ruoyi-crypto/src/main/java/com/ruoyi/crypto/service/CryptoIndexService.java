package com.ruoyi.crypto.service;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.domain.vo.CryptoIndexVO;
import com.ruoyi.crypto.domain.vo.CryptoUserVO;
import org.aspectj.weaver.loadtime.Aj;

import java.util.List;

public interface CryptoIndexService {


    /**
     * 获取当日活动统计
     * @return 包含总查询数和活跃用户数的VO对象
     */
    CryptoIndexVO getDailyActivityStats();

    List<CryptoUserVO> getUserRange();

    AjaxResult getBotStatus(String type);

    AjaxResult restartTgBot(String pid);

    AjaxResult startTgBot(String pid);

    AjaxResult stopTgBot(String pid);

    AjaxResult startAllTgBot();
}
