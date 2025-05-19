package com.ruoyi.cryptomonitor.mapper;

import java.util.List;
import com.ruoyi.cryptomonitor.domain.CryptoCaRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * CA记录Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoCaRecordMapper
{
    /**
     * 查询CA记录
     *
     * @param id CA记录ID
     * @return CA记录
     */
    public CryptoCaRecord selectCryptoCARecordById(Long id);

    /**
     * 查询CA记录列表
     *
     * @param cryptoCARecord CA记录
     * @return CA记录集合
     */
    public List<CryptoCaRecord> selectCryptoCARecordList(CryptoCaRecord cryptoCARecord);

    /**
     * 根据代币ID查询CA记录
     *
     * @param caId 代币ID
     * @return CA记录
     */
    public CryptoCaRecord selectCryptoCARecordByCaId(Long caId);

    /**
     * 新增CA记录
     *
     * @param cryptoCARecord CA记录
     * @return 结果
     */
    public int insertCryptoCARecord(CryptoCaRecord cryptoCARecord);

    /**
     * 修改CA记录
     *
     * @param cryptoCARecord CA记录
     * @return 结果
     */
    public int updateCryptoCARecord(CryptoCaRecord cryptoCARecord);

    /**
     * 删除CA记录
     *
     * @param id CA记录ID
     * @return 结果
     */
    public int deleteCryptoCARecordById(Long id);

    /**
     * 批量删除CA记录
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteCryptoCARecordByIds(Long[] ids);

    /**
     * 更新CA记录的查询次数
     *
     * @param caId 代币ID
     * @return 结果
     */
    public int updateQueryCount(Long caId);

    /**
     * 更新CA记录的最高价格信息
     *
     * @param cryptoCARecord CA记录
     * @return 结果
     */
    public int updateHighestPrice(CryptoCaRecord cryptoCARecord);
}