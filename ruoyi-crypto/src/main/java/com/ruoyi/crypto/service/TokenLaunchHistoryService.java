package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.TokenLaunchHistory;

import java.util.List;
import java.util.Map;

public interface TokenLaunchHistoryService {
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
    TokenLaunchHistory selectTokenByCa(String ca);

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

}
