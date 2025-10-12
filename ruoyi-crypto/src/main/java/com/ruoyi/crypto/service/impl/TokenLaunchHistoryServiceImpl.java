package com.ruoyi.crypto.service.impl;

import com.ruoyi.crypto.domain.TokenLaunchHistory;
import com.ruoyi.crypto.mapper.TokenLaunchHistoryMapper;
import com.ruoyi.crypto.service.TokenLaunchHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


/**
 * Token发射历史Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TokenLaunchHistoryServiceImpl implements TokenLaunchHistoryService {

    private static final Logger log = LoggerFactory.getLogger(TokenLaunchHistoryServiceImpl.class);

    @Resource
    private TokenLaunchHistoryMapper tokenMapper;

    @Override
    public List<TokenLaunchHistory> selectTokenList(TokenLaunchHistory token) {
        log.info("查询Token列表，params: {}", token.getParams());
        return tokenMapper.selectTokenList(token);
    }

    @Override
    public TokenLaunchHistory selectTokenByCa(String ca) {
        return tokenMapper.selectTokenByCa(ca);
    }

    @Override
    public Map<String, Object> selectTokenStats() {
        return tokenMapper.selectTokenStats();
    }
}
