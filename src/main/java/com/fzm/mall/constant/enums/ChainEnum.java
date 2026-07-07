package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

public class ChainEnum {
    @Getter
    @AllArgsConstructor
    public enum TypeEnum {
        BTY(0, ChainEnum.CoinEnum.BTY),
        BSC(1, ChainEnum.CoinEnum.USD),
        ;
        private final int type;
        private final CoinEnum coinEnum;

        public static TypeEnum exist(Integer type) {
            if (type == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getType() == type).findFirst().orElse(null);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum CoinEnum {
        BTY(0),
        USD(1),
        CNY(2),
        ;
        private final int type;

        public static CoinEnum exist(Integer type) {
            if (type == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getType() == type).findFirst().orElse(null);
        }
    }
}
