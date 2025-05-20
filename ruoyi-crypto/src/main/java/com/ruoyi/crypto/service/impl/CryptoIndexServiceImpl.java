package com.ruoyi.crypto.service.impl;

import com.ruoyi.crypto.domain.vo.CryptoIndexVo;
import com.ruoyi.crypto.mapper.CryptoCaQueryRecordMapper;
import com.ruoyi.crypto.service.CryptoIndexService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CryptoIndexServiceImpl implements CryptoIndexService {

    @Resource
    private CryptoCaQueryRecordMapper cryptoCaQueryRecordMapper;
    @Override
    public CryptoIndexVo getDailyActivityStats() {
        return cryptoCaQueryRecordMapper.getDailyActivityStats();
    }
}
