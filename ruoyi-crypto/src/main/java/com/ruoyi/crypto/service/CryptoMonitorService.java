package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.CryptoMonitorConfig;
import com.ruoyi.crypto.domain.vo.CryptoCaRecordVO;

import java.util.List;

public interface CryptoMonitorService {


    List<CryptoMonitorConfig> select(CryptoMonitorConfig cryptoMonitorConfig);

    CryptoMonitorConfig getConfig(CryptoMonitorConfig cryptoMonitorConfig);

}
