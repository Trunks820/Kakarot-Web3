package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.CryptoWallet;
import com.ruoyi.crypto.domain.vo.CryptoCoinVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CryptoWalletMapper {

    public CryptoWallet selectCryptoWalletById(Long id);

    public List<CryptoWallet> selectCryptoWalletList(CryptoWallet cryptoWallet);

    CryptoWallet checkWalletUnique(CryptoWallet cryptoWallet);

    int insertCryptoWallet(CryptoWallet cryptoWallet);

    int updateCryptoWallet(CryptoWallet cryptoWallet);

    int deleteCryptoWalletByIds(Long[] ids);
    int batchUpdateWalletStatus(@Param("ids") Long[] ids, @Param("monitorState") Integer monitorState);
}
