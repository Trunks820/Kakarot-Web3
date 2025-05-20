package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.CryptoCoin;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CryptoCoinMapper {
    /**
     * 查询代币
     * @return 代币
     */
    public CryptoCoinVO selectCryptoCoin(CryptoCoinVO cryptoCoin);

    /**
     * 查询代币列表
     *
     * @param cryptoCoin 代币
     * @return 代币集合
     */
    public List<CryptoCoinVO> selectCryptoCoinList(CryptoCoinVO cryptoCoin);

    /**
     * 批量删除代币
     *
     * @param coinIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteCryptoCoinByIds(Long[] coinIds);


}
