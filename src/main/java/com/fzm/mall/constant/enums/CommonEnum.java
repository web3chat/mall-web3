package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

public class CommonEnum {
    @Getter
    @AllArgsConstructor
    public enum BoolEnum {
        NO(0),
        YES(1),
        ;
        private final int status;

        public static BoolEnum exist(Integer status) {
            if (status == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getStatus() == status).findFirst().orElse(null);
        }
    }

}
