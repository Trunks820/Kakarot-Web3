package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.service.CryptoIndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 机器人控制相关接口
 */
@RestController
@RequestMapping("/crypto/bot")
public class CryptoBotController extends BaseController {

    @Resource
    private CryptoIndexService cryptoIndexService;

    /**
     * 机器人状态查询
     * @return
     */
    @GetMapping("/tg/getTgBotStatus")
    public AjaxResult getTgBotStatus()
    {
        return cryptoIndexService.getBotStatus("telegram_sol");
    }


    /**
     * 重启TG机器人
     */
     @GetMapping("/tg/restart")
     public AjaxResult restartTgBot(String pid) {
         return cryptoIndexService.restartTgBot(pid);
     }

     /**
      * 启动TG机器人
      */
     @GetMapping("/tg/start")
     public AjaxResult startTgBot(String pid) {
         return cryptoIndexService.startTgBot(pid);
     }

     /**
      * 停止TG机器人
      */
     @GetMapping("/tg/stop")
     public AjaxResult stopTgBot(String pid) {
         return cryptoIndexService.stopTgBot(pid);
     }

} 