package com.fzm.mall.third.chain.util;

import com.fzm.mall.constant.ChainConstant;
import org.web3j.utils.Strings;

public class TokenUtils {

    public static long tokenId(long prefix, long serial) {
        return prefix * ChainConstant.token_max_serial + serial;
    }

    public static String tokenName(String name, Long tokenId) {
        if (tokenId == null) {
            return name;
        }
        return name + " #" + tokenId % ChainConstant.token_max_serial;
    }

    public static String tokenIdFullHex(long tokenId) {
        String tokenIdHex = Long.toHexString(tokenId);
        return Strings.zeros(64 - tokenIdHex.length()) + tokenIdHex;
    }

    public static String tokenIdFullHexLang(String tokenIdFullHex, String lang) {
        return tokenIdFullHex + "/" + lang;
    }
}
