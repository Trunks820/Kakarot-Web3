package com.ruoyi.crypto.mapper;

import java.util.List;
import com.ruoyi.crypto.domain.CryptoCaRecord;
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
    List<CryptoCoinVO> getHotCaByWechat();
}