package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.vo.CryptoIndexVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CA查询记录Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoCaQueryRecordMapper {
    /**
     * 获取当日活动统计
     *
     * @return 包含总查询数和活跃用户数的VO对象
     */
    CryptoIndexVO getDailyActivityStats();

}