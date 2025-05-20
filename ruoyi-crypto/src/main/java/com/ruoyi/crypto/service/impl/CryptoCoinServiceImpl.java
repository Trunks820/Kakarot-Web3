package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.domain.CryptoCoin;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
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
    public CryptoCoinVO selectCryptoCoin(CryptoCoinVO cryptoCoin) {
        return cryptoCoinMapper.selectCryptoCoin(cryptoCoin);
    }

    @Override
    public List<CryptoCoinVO> selectCryptoCoinList(CryptoCoinVO cryptoCoin) {
        return cryptoCoinMapper.selectCryptoCoinList(cryptoCoin);
    }

    @Override
    public int deleteCryptoCoinByIds(Long[] coinIds) {
        return cryptoCoinMapper.deleteCryptoCoinByIds(coinIds);
    }
}
