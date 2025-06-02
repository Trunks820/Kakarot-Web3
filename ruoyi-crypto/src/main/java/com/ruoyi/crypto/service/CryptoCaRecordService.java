package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.vo.CryptoCaRecordVO;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;

import java.util.List;

public interface CryptoCaRecordService {

    List<CryptoCaRecordVO> getHotCaByWechat();

    List<CryptoCaRecordVO> getHotCaByTelegram();

    /**
     * 查询代币
     *
     * @param cryptoCoin 代币信息
     * @return 代币信息
     */
    CryptoCaRecordVO selectCryptoCoin(CryptoCaRecordVO cryptoCoin);

    /**
     * 查询代币列表
     *
     * @param cryptoCoin 代币信息
     * @return 代币集合
     */
    List<CryptoCaRecordVO> selectCryptoCoinList(CryptoCaRecordVO cryptoCoin);

    /**
     * 批量删除代币信息
     *
     * @param coinIds 需要删除的代币ID
     * @return 结果
     */
    int deleteCryptoCoinByIds(Long[] coinIds);


}
