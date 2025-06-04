package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.vo.CryptoUserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CryptoUserMapper {


    List<CryptoUserVO> getUserRange();
}
