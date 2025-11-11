package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.TokenLaunchHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Token发射历史Mapper接口
 * 
 * @author ruoyi
 */
public interface TokenLaunchHistoryMapper {
    
    /**
     * 查询Token发射历史列表
     * 
     * @param token Token发射历史
     * @return Token发射历史集合
     */
    List<TokenLaunchHistory> selectTokenList(TokenLaunchHistory token);

    /**
     * 根据合约地址查询Token详情
     * 
     * @param ca 合约地址
     * @return Token发射历史
     */
    TokenLaunchHistory selectTokenByCa(@Param("ca") String ca);

    /**
     * 查询统计数据
     * 
     * @return 统计数据Map
     */
    Map<String, Object> selectTokenStats();

    /**
     * 获取今日新增Token数量
     * 
     * @return 今日新增数量
     */
    Long getTodayNewCount();

    /**
     * 根据智能条件筛选Token（用于Monitor V2目标同步）
     * 
     * @param conditions 筛选条件（chainType, minMarketCap, requireTwitter, maxTargets等）
     * @return Token列表
     */
    List<TokenLaunchHistory> selectBySmartConditions(Map<String, Object> conditions);
}


