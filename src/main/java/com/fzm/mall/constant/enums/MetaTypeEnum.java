package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum MetaTypeEnum {
    unknown(-1, "未知", "unknown"),
    goods(0, "商品", "goods"),
    ;
    private final int type;
    private final String name;
    private final String nameEn;

    public static MetaTypeEnum exist(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        return Arrays.stream(values()).filter(v -> type.equals(String.valueOf(v.getType())) || type.equals(v.getName()) || type.equalsIgnoreCase(v.getNameEn())).findFirst().orElse(null);
    }
}
