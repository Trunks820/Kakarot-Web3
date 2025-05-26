package com.ruoyi.crypto;

import com.ruoyi.crypto.domain.vo.CryptoIndexVo;
import com.ruoyi.crypto.service.CryptoIndexService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class CryptoMonitorApplicationTests {

    @Resource
    private CryptoIndexService cryptoIndexService;
    @Test
    void contextLoads() {
        CryptoIndexVo dailyActivityStats = cryptoIndexService.getDailyActivityStats();
        System.err.println(dailyActivityStats);
    }

}
