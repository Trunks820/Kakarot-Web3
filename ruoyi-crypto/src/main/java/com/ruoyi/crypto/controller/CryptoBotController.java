package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.service.CryptoIndexService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     public AjaxResult restartTgBot(@RequestParam("name") String name) {
         return cryptoIndexService.restartTgBot(name);
     }

     /**
      * 启动TG机器人
      */
     @GetMapping("/tg/start")
     public AjaxResult startTgBot(@RequestParam("name") String name) {
         return cryptoIndexService.startTgBot(name);
     }

     /**
      * 停止TG机器人
      */
     @GetMapping("/tg/stop")
     public AjaxResult stopTgBot(@RequestParam("name") String name) {
         return cryptoIndexService.stopTgBot(name);
     }

     @GetMapping("/tg/startAll")
    public AjaxResult startAllTgBot() {
         return cryptoIndexService.startAllTgBot();
     }

} 