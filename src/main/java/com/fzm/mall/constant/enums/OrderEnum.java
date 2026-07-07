package com.fzm.mall.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

public class OrderEnum {

    @Getter
    @AllArgsConstructor
    public enum InfoStatusEnum {
        wait(0, "待付款"),
        paid(1, "已付款"),
        cancel(2, "已撤销"),
        expired(3, "已超时"),
        refund_apply(4, "申请退款"),
        refund_fail(5, "不同意"),
        refund_success(6, "同意"),
        ;
        private final int status;
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    public enum ExpressStatusEnum {
        wait(0, "待发货"),
        express(1, "已发货"),
        confirm(2, "已收货"),
        refund_apply(3, "申请退货"),
        refund_audit_fail(4, "不同意退货"),
        refund_audit_success(5, "已同意退货，用户填写退货单号"),
        refund_ing(6, "退货中"),
        refund_confirm(7, "已退货"),
        ;
        private final int status;
        private final String message;

        public static ExpressStatusEnum exist(Integer status) {
            if (status == null) {
                return null;
            }
            return Arrays.stream(values()).filter(v -> v.getStatus() == status).findFirst().orElse(null);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum TxStatusEnum {
        wait(0, "等待中"),
        success(1, "成功"),
        fail(2, "失败"),

        ;
        private final int status;
        private final String message;
    }

    @Getter
    @AllArgsConstructor
    public enum UnfreezeStatusEnum {
        none(0, "无需处理"),
        freeze(1, "冻结中"),
        unfreeze(2, "已解冻"),
        ;
        private final int status;
        private final String message;
    }
}
