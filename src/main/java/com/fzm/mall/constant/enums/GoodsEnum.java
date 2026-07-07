package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

public class GoodsEnum {

    @Getter
    @AllArgsConstructor
    public enum SpuTypeEnum {
        normal(0, "正常", true),
        blind_box(1, "盲盒", true),
        synthetic(2, "合成", false),
        ;
        private final int type;
        private final String message;
        private final boolean canBuy;

        public static SpuTypeEnum exist(Integer type) {
            if (type == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getType() == type).findFirst().orElse(null);
        }

        public static boolean canBuy(Integer type) {
            if (type == null) {
                return false;
            }
            return Arrays.stream(values()).filter(v -> v.canBuy).anyMatch(v -> v.getType() == type);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum SpuStatusEnum {
        draft(0, "草稿"),
        mint_ing(1, "发行中"),
        mint_fail(2, "发行失败"),
        mint_success(3, "发行成功待上架"),
        mint_sell_ing(4, "已上架"),
        mint_again_ing(5, "增发中/修改中"),
        mint_again_fail(6, "增发失败"),
        ;
        private final int status;
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    public enum MintStatusEnum {
        mint_wait(0, "等待发行"),
        mint_ing(1, "发行中"),
        mint_fail(2, "发行失败"),
        mint_success(3, "发行成功"),
        ;
        private final int status;
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    public enum SaleTypeEnum {
        normal(0, "常规销售"),
        white(1, "白名单销售"),
        ;
        private final int type;
        private final String message;

        public static SaleTypeEnum exist(Integer type) {
            if (type == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getType() == type).findFirst().orElse(null);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum OrderTypeEnum {
        none(-1, "无"),
        score(0, "默认"),
        sales_asc(1, "销量正序"),
        sales_desc(2, "销量倒序"),
        price_asc(3, "价格正序"),
        price_desc(4, "价格倒序"),
        asle_time_asc(5, "销售时间正序"),
        asle_time_desc(6, "销售时间倒序"),
        ;
        private final int type;
        private final String message;
    }
}
