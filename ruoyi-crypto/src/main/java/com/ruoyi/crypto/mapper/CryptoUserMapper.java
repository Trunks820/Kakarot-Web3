package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.vo.CryptoUserVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CryptoUserMapper {


    List<CryptoUserVo> getUserRange();
}
