package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.vo.CryptoCoinVO;

import java.util.List;

public interface CryptoCaRecordService {

    List<CryptoCoinVO> getHotCaByWechat();
}
