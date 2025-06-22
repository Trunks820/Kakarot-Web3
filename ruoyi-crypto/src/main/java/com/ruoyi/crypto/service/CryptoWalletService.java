package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.CryptoWallet;
import java.util.List;

public interface CryptoWalletService {
    /**
     * 查询钱包
     *
     * @return 钱包信息
     */
    CryptoWallet selectCryptoWalletById(Long id);


    /**
     * 查询钱包列表
     *
     * @param cryptoWallet 钱包信息
     * @return 钱包集合
     */
    List<CryptoWallet> selectCryptoWalletList(CryptoWallet cryptoWallet);

    boolean checkWalletUnique(CryptoWallet cryptoWallet);

    int insertWallet(CryptoWallet cryptoWallet);

    int updateWallet(CryptoWallet cryptoWallet);

    String importWallet(List<CryptoWallet> cryptoWalletList, String operName);

    int deleteCryptoWalletByIds(Long[] ids);
}
