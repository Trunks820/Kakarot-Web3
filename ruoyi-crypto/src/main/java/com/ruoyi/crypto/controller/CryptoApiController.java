package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.service.CryptoApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@RestController
@RequestMapping("/crypto/api")
public class CryptoApiController extends BaseController {


    @Resource
    private CryptoApiService cryptoApiService;

    @GetMapping("/tokenInfo")
    public AjaxResult tokenInfo(String address)
    {
        return cryptoApiService.getTokenInfo(address);
    }

    @GetMapping("/securityInfo")
    public AjaxResult securityInfo(String address)
    {
        return cryptoApiService.getTokenSecurityInfo(address);
    }
}
