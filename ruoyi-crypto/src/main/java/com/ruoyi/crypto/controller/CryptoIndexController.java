package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
import com.ruoyi.crypto.domain.vo.CryptoIndexVo;
import com.ruoyi.crypto.service.CryptoCaRecordService;
import com.ruoyi.crypto.service.CryptoIndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 系统首页数据
 */
@RestController
@RequestMapping("/crypto/record")
public class CryptoIndexController extends BaseController {

    @Resource
    private CryptoIndexService cryptoIndexService;

    @Resource
    private CryptoCaRecordService cryptoCaRecordService;


    /**
     * 平台数据
     * 1. 微信机器人查询ca总次数
     * 2. 微信机器人活跃用户
     */
    @GetMapping("/getDailyActivityStats")
    public AjaxResult getDailyActivityStats()
    {
        CryptoIndexVo dailyActivityStats = cryptoIndexService.getDailyActivityStats();
        return success(dailyActivityStats);
    }

    /**
     * 热门Ca (微信查询)
     */
    @GetMapping("/getHotCaByWechat")
    public AjaxResult getHotCaByWechat()
    {
        List<CryptoCoinVO> hotCaByWechat = cryptoCaRecordService.getHotCaByWechat();
        return success(hotCaByWechat);
    }

    /**
     * 热门Ca (Tg查询)
     */
    @GetMapping("/getHotCaByTelegram")
    public AjaxResult getHotCaByTelegram()
    {
        List<CryptoCoinVO> hotCaByTelegram = cryptoCaRecordService.getHotCaByTelegram();
        return success(hotCaByTelegram);
    }

    @GetMapping("/getTgBotStatus")
    public AjaxResult getTgBotStatus()
    {
        return cryptoIndexService.getTgBotStatus();
    }


}
