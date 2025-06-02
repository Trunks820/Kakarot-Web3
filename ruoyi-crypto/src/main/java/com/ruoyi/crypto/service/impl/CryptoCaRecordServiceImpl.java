package com.ruoyi.crypto.service.impl;

import com.ruoyi.crypto.domain.vo.CryptoCaRecordVO;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
import com.ruoyi.crypto.mapper.CryptoCaRecordMapper;
import com.ruoyi.crypto.service.CryptoCaRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CryptoCaRecordServiceImpl implements CryptoCaRecordService {

    @Resource
    private CryptoCaRecordMapper cryptoCaRecordMapper;

    @Override
    public List<CryptoCaRecordVO> getHotCaByWechat() {
        return cryptoCaRecordMapper.getHotCaByWechat();
    }

    @Override
    public List<CryptoCaRecordVO> getHotCaByTelegram() {
        return cryptoCaRecordMapper.getHotCaByTelegram();
    }

    @Override
    public CryptoCaRecordVO selectCryptoCoin(CryptoCaRecordVO cryptoCoin) {
        return cryptoCaRecordMapper.selectCryptoCoin(cryptoCoin);
    }

    @Override
    public List<CryptoCaRecordVO> selectCryptoCoinList(CryptoCaRecordVO cryptoCoin) {
        return cryptoCaRecordMapper.selectCryptoCoinList(cryptoCoin);
    }

    @Override
    public int deleteCryptoCoinByIds(Long[] coinIds) {
        return cryptoCaRecordMapper.deleteCryptoCoinByIds(coinIds);
    }
}
