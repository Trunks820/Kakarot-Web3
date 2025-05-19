package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.CryptoCoin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CryptoCoinMapper {
    /**
     * 查询代币
     * @return 代币
     */
    public CryptoCoin selectCryptoCoin(CryptoCoin cryptoCoin);

    /**
     * 查询代币列表
     *
     * @param cryptoCoin 代币
     * @return 代币集合
     */
    public List<CryptoCoin> selectCryptoCoinList(CryptoCoin cryptoCoin);

    /**
     * 新增代币
     *
     * @param cryptoCoin 代币
     * @return 结果
     */
    public int insertCryptoCoin(CryptoCoin cryptoCoin);

    /**
     * 修改代币
     *
     * @param cryptoCoin 代币
     * @return 结果
     */
    public int updateCryptoCoin(CryptoCoin cryptoCoin);

    /**
     * 删除代币
     *
     * @param coinId 代币ID
     * @return 结果
     */
    public int deleteCryptoCoinById(Long coinId);

    /**
     * 批量删除代币
     *
     * @param coinIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteCryptoCoinByIds(Long[] coinIds);


}
