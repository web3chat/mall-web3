package com.fzm.mall.util;

import org.apache.commons.lang3.StringUtils;
import org.web3j.crypto.Keys;

import java.util.regex.Pattern;

public class Validator {

    private static final Pattern ethPattern = Pattern.compile("^0x[0-9a-fA-F]{40}$");
    private static final Pattern hashPattern = Pattern.compile("^0x[0-9a-fA-F]{64}$");

    public static boolean isHash(String hash) {
        if (StringUtils.isBlank(hash)) {
            return false;
        }
        return hashPattern.matcher(hash).matches();
    }

    // ETH地址校验：检查长度和十六进制格式
    public static boolean isETHAddress(String address) {
        if (StringUtils.isBlank(address)) {
            return false;
        }

        // 检查是否以0x开头，且总长度为42（0x + 40个十六进制字符）
        boolean matches = ethPattern.matcher(address).matches();
        if (!matches) {
            return false;
        }

        // 如果原始地址是混合大小写，验证校验和
        if (!address.equals(address.toLowerCase()) && !address.equals(address.toUpperCase())) {
            String checksumAddress = Keys.toChecksumAddress(address);
            return address.equals(checksumAddress);
        }

        return true;
    }
}
