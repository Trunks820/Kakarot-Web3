package com.ruoyi.crypto.mapper;

import java.util.List;
import com.ruoyi.crypto.domain.CryptoCaQueryRecord;
import com.ruoyi.crypto.domain.vo.CryptoIndexVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * CA查询记录Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoCaQueryRecordMapper
{
    /**
     * 获取当日活动统计
     * @return 包含总查询数和活跃用户数的VO对象
     */
    CryptoIndexVo getDailyActivityStats();

    CryptoIndexVo getHotCaByTelegram();
}