package com.ruoyi.crypto.domain;

import lombok.Data;
import java.util.Map;

@Data
public class CryptoRealtimeData {
    // txns: m5/h1/h6/h24 -> 每个区间的买卖次数
    private Map<String, CryptoTxnStats> txns;
    // priceChange: m5/h1/h6/h24 -> 每个区间的涨跌幅
    private Map<String, Double> priceChange;
    // volume: m5/h1/h6/h24 -> 每个区间成交额
    private Map<String, Double> volume;
}
