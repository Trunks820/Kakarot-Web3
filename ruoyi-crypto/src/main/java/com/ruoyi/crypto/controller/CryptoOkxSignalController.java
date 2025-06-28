package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * OKX信号控制器
 */
@RestController
@RequestMapping("/crypto/okxSignal")
public class CryptoOkxSignalController extends BaseController {

    /**
     * 获取OKX信号列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:okxSignal:view')")
    @GetMapping("/list")
    public AjaxResult getSignalList(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "20") Integer pageSize,
                                   @RequestParam(required = false) String trend) {
        // 模拟数据，后续你可以替换为真实的API调用
        List<Map<String, Object>> signals = new ArrayList<>();
        
        // 添加一些示例数据
        signals.add(createSignalData(1, "卖出", 1, "MEERKAT", "461.63", 
            "0.00042299", "96.53K", "422.98K", System.currentTimeMillis() - 10 * 60 * 1000));
        
        signals.add(createSignalData(2, "卖出", 7, "RICH", "14.99K", 
            "0.0045015", "395.55K", "4.50M", System.currentTimeMillis() - 30 * 60 * 1000));
        
        signals.add(createSignalData(3, "卖出", 3, "MOONBOY", "4.14K", 
            "0.000033017", "20.63K", "33.01K", System.currentTimeMillis() - 60 * 60 * 1000));
        
        signals.add(createSignalData(4, "卖出", 5, "RICH", "13.52K", 
            "0.0028613", "230.75K", "2.86M", System.currentTimeMillis() - 90 * 60 * 1000));
        
        signals.add(createSignalData(5, "卖出", 7, "VANCLINGS", "22.61K", 
            "0.00025393", "64.90K", "253.91K", System.currentTimeMillis() - 120 * 60 * 1000));
        
        signals.add(createSignalData(6, "买入", 3, "MONKEPHONE", "8.01K", 
            "0.010598", "2.03M", "10.59M", System.currentTimeMillis() - 150 * 60 * 1000));
        
        signals.add(createSignalData(7, "卖出", 1, "MyBags", "42.32", 
            "0.000004908", "7.52K", "4.90K", System.currentTimeMillis() - 180 * 60 * 1000));
        
        signals.add(createSignalData(8, "卖出", 3, "MYBAGS", "4.73K", 
            "0.0000098342", "35.22K", "98.33K", System.currentTimeMillis() - 200 * 60 * 1000));

        // 根据trend参数过滤
        if (trend != null && !trend.isEmpty()) {
            signals = signals.stream()
                    .filter(signal -> trend.equals(signal.get("trendText")))
                    .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", signals);
        result.put("total", signals.size());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        
        return AjaxResult.success(result);
    }

    /**
     * 获取信号统计数据
     */
    @PreAuthorize("@ss.hasPermi('crypto:okxSignal:view')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayTotal", 156);
        stats.put("buySignals", 45);
        stats.put("sellSignals", 111);
        stats.put("activeTokens", 89);
        
        return AjaxResult.success(stats);
    }

    /**
     * 创建信号数据
     */
    private Map<String, Object> createSignalData(int id, String trendText, int addressNum, 
                                                String tokenSymbol, String amount, String price, 
                                                String liquidity, String mcap, long eventTime) {
        Map<String, Object> signal = new HashMap<>();
        
        signal.put("id", id);
        signal.put("addressNum", addressNum);
        signal.put("addresses", "A4DCAjDwkq5jYhNoZ5Xn2NbkTLimARkerVv81w2dhXgL,Cdhxwq3DXD2c8Zp7T4TZCTCk7H8rhx28C74ZVBRsg1Y8");
        signal.put("batchId", System.currentTimeMillis());
        signal.put("batchIndex", 0);
        signal.put("duration", 5516338);
        signal.put("eventTime", eventTime);
        signal.put("liquidity", liquidity);
        signal.put("mcap", mcap);
        signal.put("price", price);
        signal.put("trend", "买入".equals(trendText) ? 1 : 2);
        signal.put("trendText", trendText);
        signal.put("volume", amount);
        
        // 代币信息
        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("chainBWLogoUrl", "https://static.coinall.ltd/cdn/assets/imgs/227/2C597ACB210BFE51.png");
        tokenInfo.put("chainId", "501");
        tokenInfo.put("chainLogo", "https://static.oklink.com/cdn/wallet/logo/SOL-20220525.png");
        tokenInfo.put("riskControlLevel", "1");
        tokenInfo.put("riskLevel", "2");
        tokenInfo.put("tokenContractAddress", "2RA1v8NdkEQcF5N5zHUqLuAHxjnDMQFjwEE8fwKNpump");
        tokenInfo.put("tokenCreateTime", System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        tokenInfo.put("tokenLogoUrl", "https://static.oklink.com/cdn/web3/currency/token/small/501-2RA1v8NdkEQcF5N5zHUqLuAHxjnDMQFjwEE8fwKNpump-108");
        tokenInfo.put("tokenName", tokenSymbol.toLowerCase());
        tokenInfo.put("tokenSymbol", tokenSymbol);
        tokenInfo.put("tokenTagList", new ArrayList<>());
        
        signal.put("tokenInfo", tokenInfo);
        
        return signal;
    }
} 