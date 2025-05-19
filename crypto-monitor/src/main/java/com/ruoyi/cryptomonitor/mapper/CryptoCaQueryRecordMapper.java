package com.ruoyi.cryptomonitor.mapper;

import java.util.List;
import com.ruoyi.cryptomonitor.domain.CryptoCaQueryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * CA查询记录Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoCaQueryRecordMapper
{
    /**
     * 查询CA查询记录
     *
     * @param id CA查询记录ID
     * @return CA查询记录
     */
    public CryptoCaQueryRecord selectCryptoCaQueryRecordById(Long id);

    /**
     * 查询CA查询记录列表
     *
     * @param CryptoCaQueryRecord CA查询记录
     * @return CA查询记录集合
     */
    public List<CryptoCaQueryRecord> selectCryptoCaQueryRecordList(CryptoCaQueryRecord CryptoCaQueryRecord);

    /**
     * 根据代币ID查询CA查询记录
     *
     * @param caId 代币ID
     * @return CA查询记录集合
     */
    public List<CryptoCaQueryRecord> selectCryptoCaQueryRecordByCaId(Long caId);

    /**
     * 根据群组ID查询CA查询记录
     *
     * @param groupId 群组ID
     * @return CA查询记录集合
     */
    public List<CryptoCaQueryRecord> selectCryptoCaQueryRecordByGroupId(String groupId);

    /**
     * 新增CA查询记录
     *
     * @param CryptoCaQueryRecord CA查询记录
     * @return 结果
     */
    public int insertCryptoCaQueryRecord(CryptoCaQueryRecord CryptoCaQueryRecord);

    /**
     * 修改CA查询记录
     *
     * @param CryptoCaQueryRecord CA查询记录
     * @return 结果
     */
    public int updateCryptoCaQueryRecord(CryptoCaQueryRecord CryptoCaQueryRecord);

    /**
     * 删除CA查询记录
     *
     * @param id CA查询记录ID
     * @return 结果
     */
    public int deleteCryptoCaQueryRecordById(Long id);

    /**
     * 批量删除CA查询记录
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteCryptoCaQueryRecordByIds(Long[] ids);

    /**
     * 获取某个代币的查询次数
     *
     * @param caId 代币ID
     * @return 查询次数
     */
    public int getQueryCountByCaId(Long caId);
}