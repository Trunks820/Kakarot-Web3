package com.ruoyi.crypto.service.impl;

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
    public List<CryptoCoinVO> getHotCaByWechat() {
        return cryptoCaRecordMapper.getHotCaByWechat();
    }
}
