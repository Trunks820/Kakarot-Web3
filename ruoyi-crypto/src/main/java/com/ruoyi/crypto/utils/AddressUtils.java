package com.ruoyi.crypto.utils;

import com.ruoyi.common.utils.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressUtils {

    private static final String SOL_REGEX = "\\b[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{40,44}\\b";
    private static final String EVM_REGEX = "\\b0x[a-fA-F0-9]{40}\\b";

    /**
     * 获取文本中的地址
     * @param address
     * @return
     */
    public static String findAddress(String address){
        String solAddress = findSolAddress(address);
        if(StringUtils.isNotBlank(solAddress)){
            return solAddress;
        }

        String evmAddress = findEvmAddress(address);
        if(StringUtils.isNotBlank(evmAddress)){
            return evmAddress;
        }
        return null;
    }

    /**
     * 区分公链
     * @param address 地址
     * @return
     */
    public static String findChainType(String address){
        if (address.startsWith("0x")){
            return "evm";
        } else if (StringUtils.isNotEmpty(findSolAddress(address))){
            return "sol";
        } else{
            return null;
        }
    }


    /**
     * 获取sol地址
     * @param address
     * @return
     */
    public static String findSolAddress(String address) {
        Pattern pattern = Pattern.compile(SOL_REGEX);
        Matcher matcher = pattern.matcher(address);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 获取evm地址
     * @param address
     * @return
     */
    public static String findEvmAddress(String address) {
        Pattern pattern = Pattern.compile(EVM_REGEX);
        Matcher matcher = pattern.matcher(address);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }


}
