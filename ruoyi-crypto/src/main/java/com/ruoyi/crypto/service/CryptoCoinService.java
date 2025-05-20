package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
import java.util.List;

public interface CryptoCoinService {
    /**
     * 查询代币
     *
     * @param cryptoCoin 代币信息
     * @return 代币信息
     */
    public CryptoCoinVO selectCryptoCoin(CryptoCoinVO cryptoCoin);

    /**
     * 查询代币列表
     *
     * @param cryptoCoin 代币信息
     * @return 代币集合
     */
    public List<CryptoCoinVO> selectCryptoCoinList(CryptoCoinVO cryptoCoin);

    /**
     * 批量删除代币信息
     *
     * @param coinIds 需要删除的代币ID
     * @return 结果
     */
    public int deleteCryptoCoinByIds(Long[] coinIds);
}
