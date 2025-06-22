package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.crypto.domain.CryptoWallet;
import com.ruoyi.crypto.mapper.CryptoWalletMapper;
import com.ruoyi.crypto.service.CryptoWalletService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class CryptoWalletServiceImpl implements CryptoWalletService {

    @Resource
    private CryptoWalletMapper cryptoWalletMapper;

    @Override
    public CryptoWallet selectCryptoWalletById(Long id) {
        return cryptoWalletMapper.selectCryptoWalletById(id);
    }

    @Override
    public List<CryptoWallet> selectCryptoWalletList(CryptoWallet cryptoWallet) {
        return cryptoWalletMapper.selectCryptoWalletList(cryptoWallet);
    }

    @Override
    public boolean checkWalletUnique(CryptoWallet cryptoWallet) {
        String walletAddress = cryptoWallet.getWalletAddress();
        if (StringUtils.isEmpty(walletAddress)) {
            return UserConstants.NOT_UNIQUE;
        }

        CryptoWallet existing = cryptoWalletMapper.checkWalletUnique(cryptoWallet);
        if (existing != null && (cryptoWallet.getId() == null || !existing.getId().equals(cryptoWallet.getId()))) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public int insertWallet(CryptoWallet cryptoWallet) {
        return cryptoWalletMapper.insertCryptoWallet(cryptoWallet);
    }

    @Override
    public int updateWallet(CryptoWallet cryptoWallet){
        return cryptoWalletMapper.updateCryptoWallet(cryptoWallet);
    }

    @Override
    public String importWallet(List<CryptoWallet> cryptoWalletList, String operName) {
        int success = 0, skip = 0;
        for (CryptoWallet wallet : cryptoWalletList) {
            if (!checkWalletUnique(wallet)) {
                skip++;
                continue;
            }
            wallet.setCreateBy(operName);
            wallet.setDelFlag("0");
            wallet.setMonitorState(1); // 导入的钱包默认设置为关注
            cryptoWalletMapper.insertCryptoWallet(wallet);
            success++;
        }
        return "成功导入：" + success + " 条，跳过重复：" + skip + " 条。";
    }

    @Override
    public int deleteCryptoWalletByIds(Long[] ids) {
        return cryptoWalletMapper.deleteCryptoWalletByIds(ids);
    }
}
