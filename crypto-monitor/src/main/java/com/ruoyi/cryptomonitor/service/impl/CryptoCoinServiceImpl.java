package com.ruoyi.cryptomonitor.service.impl;

import com.ruoyi.cryptomonitor.domain.CryptoCoin;
import com.ruoyi.cryptomonitor.mapper.CryptoCoinMapper;
import com.ruoyi.cryptomonitor.service.CryptoCoinService;

import javax.annotation.Resource;
import java.util.List;

public class CryptoCoinServiceImpl implements CryptoCoinService {

    @Resource
    private CryptoCoinMapper cryptoCoinMapper;

    @Override
    public CryptoCoin selectCryptoCoin(CryptoCoin cryptoCoin) {
        return cryptoCoinMapper.selectCryptoCoin(cryptoCoin);
    }

    @Override
    public List<CryptoCoin> selectCryptoCoinList(CryptoCoin cryptoCoin) {
        return cryptoCoinMapper.selectCryptoCoinList(cryptoCoin);
    }

    @Override
    public int insertCryptoCoin(CryptoCoin cryptoCoin) {
        return cryptoCoinMapper.insertCryptoCoin(cryptoCoin);
    }

    @Override
    public int updateCryptoCoin(CryptoCoin cryptoCoin) {
        return cryptoCoinMapper.updateCryptoCoin(cryptoCoin);
    }

    @Override
    public int deleteCryptoCoinById(Long coinId) {
        return cryptoCoinMapper.deleteCryptoCoinById(coinId);
    }

    @Override
    public int deleteCryptoCoinByIds(Long[] coinIds) {
        return cryptoCoinMapper.deleteCryptoCoinByIds(coinIds);
    }
}
