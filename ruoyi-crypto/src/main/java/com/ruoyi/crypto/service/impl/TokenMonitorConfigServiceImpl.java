package com.ruoyi.crypto.service.impl;

import com.ruoyi.crypto.domain.TokenMonitorConfig;
import com.ruoyi.crypto.mapper.TokenMonitorConfigMapper;
import com.ruoyi.crypto.service.ITokenMonitorConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Token监控配置服务实现
 *
 * @author ruoyi
 */
@Service
public class TokenMonitorConfigServiceImpl implements ITokenMonitorConfigService {

    private static final Logger log = LoggerFactory.getLogger(TokenMonitorConfigServiceImpl.class);

    @Resource
    private TokenMonitorConfigMapper monitorConfigMapper;

    @Override
    public List<TokenMonitorConfig> selectMonitorConfigList(TokenMonitorConfig config) {
        return monitorConfigMapper.selectMonitorConfigList(config);
    }

    @Override
    public TokenMonitorConfig selectMonitorConfigById(Long id) {
        return monitorConfigMapper.selectMonitorConfigById(id);
    }

    @Override
    public List<TokenMonitorConfig> selectMonitorConfigByCa(String ca) {
        return monitorConfigMapper.selectMonitorConfigByCa(ca);
    }

    @Override
    public int insertMonitorConfig(TokenMonitorConfig config) {
        try {
            int result = monitorConfigMapper.insertMonitorConfig(config);
            if (result > 0) {
                log.info("新增Token监控配置成功: CA={}, 模式={}", config.getCa(), config.getAlertMode());
            }
            return result;
        } catch (Exception e) {
            log.error("新增Token监控配置失败: CA={}", config.getCa(), e);
            throw e;
        }
    }

    @Override
    public int updateMonitorConfig(TokenMonitorConfig config) {
        try {
            int result = monitorConfigMapper.updateMonitorConfig(config);
            if (result > 0) {
                log.info("修改Token监控配置成功: ID={}", config.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("修改Token监控配置失败: ID={}", config.getId(), e);
            throw e;
        }
    }

    @Override
    public int deleteMonitorConfigById(Long id) {
        try {
            int result = monitorConfigMapper.deleteMonitorConfigById(id);
            if (result > 0) {
                log.info("删除Token监控配置成功: ID={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("删除Token监控配置失败: ID={}", id, e);
            throw e;
        }
    }

    @Override
    public int deleteMonitorConfigByIds(Long[] ids) {
        try {
            int result = monitorConfigMapper.deleteMonitorConfigByIds(ids);
            if (result > 0) {
                log.info("批量删除Token监控配置成功: 数量={}", result);
            }
            return result;
        } catch (Exception e) {
            log.error("批量删除Token监控配置失败", e);
            throw e;
        }
    }

    @Override
    public int enableMonitor(Long id) {
        try {
            int result = monitorConfigMapper.updateMonitorStatus(id, "1");
            if (result > 0) {
                log.info("启用Token监控成功: ID={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("启用Token监控失败: ID={}", id, e);
            throw e;
        }
    }

    @Override
    public int disableMonitor(Long id) {
        try {
            int result = monitorConfigMapper.updateMonitorStatus(id, "0");
            if (result > 0) {
                log.info("停用Token监控成功: ID={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("停用Token监控失败: ID={}", id, e);
            throw e;
        }
    }

    @Override
    public int batchEnableMonitor(Long[] ids) {
        try {
            int result = monitorConfigMapper.batchUpdateMonitorStatus(ids, "1");
            if (result > 0) {
                log.info("批量启用Token监控成功: 数量={}", result);
            }
            return result;
        } catch (Exception e) {
            log.error("批量启用Token监控失败", e);
            throw e;
        }
    }

    @Override
    public int batchDisableMonitor(Long[] ids) {
        try {
            int result = monitorConfigMapper.batchUpdateMonitorStatus(ids, "0");
            if (result > 0) {
                log.info("批量停用Token监控成功: 数量={}", result);
            }
            return result;
        } catch (Exception e) {
            log.error("批量停用Token监控失败", e);
            throw e;
        }
    }
}

