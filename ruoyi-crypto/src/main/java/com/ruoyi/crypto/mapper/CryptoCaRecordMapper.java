package com.ruoyi.crypto.mapper;

import java.util.List;
import com.ruoyi.crypto.domain.CryptoCaRecord;
import com.ruoyi.crypto.domain.vo.CryptoCaRecordVO;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CA记录Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoCaRecordMapper
{
    List<CryptoCaRecordVO> getHotCaByWechat();


    List<CryptoCaRecordVO> getHotCaByTelegram();

    /**
     * 查询代币
     * @return 代币
     */
    CryptoCaRecordVO selectCryptoCoin(CryptoCaRecordVO cryptoCoin);

    /**
     * 查询代币列表
     *
     * @param cryptoCoin 代币
     * @return 代币集合
     */
    List<CryptoCaRecordVO> selectCryptoCoinList(CryptoCaRecordVO cryptoCoin);

    /**
     * 批量删除代币
     *
     * @param coinIds 需要删除的数据ID
     * @return 结果
     */
    int deleteCryptoCoinByIds(Long[] coinIds);
}