package com.fzm.mall.util;

import com.fzm.mall.constant.response.ResponseEnum;

public class ParamsUtils {

    public static String address(String address) {
        AssertUtils.isTrue(Validator.isETHAddress(address), ResponseEnum.invalid_address_format);
        return address.toLowerCase();
    }

    public static String txHash(String txHash) {
        AssertUtils.isTrue(Validator.isHash(txHash), ResponseEnum.invalid_address_format);
        return txHash.toLowerCase();
    }

    public static void positiveInteger(Integer number) {
        AssertUtils.isNotNull(number, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(number > 0, ResponseEnum.invalid_parameter);
    }

    public static void positiveLong(Long number) {
        AssertUtils.isNotNull(number, ResponseEnum.invalid_parameter);
        AssertUtils.isTrue(number > 0, ResponseEnum.invalid_parameter);
    }
}
