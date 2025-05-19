package com.ruoyi.crypto.service.impl;

import com.ruoyi.crypto.domain.CryptoCoin;
import com.ruoyi.crypto.mapper.CryptoCoinMapper;
import com.ruoyi.crypto.service.CryptoCoinService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
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
