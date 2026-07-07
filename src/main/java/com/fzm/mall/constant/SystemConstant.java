package com.fzm.mall.constant;

/**
 * 系统配置常量
 */
public class SystemConstant {

    public static class Header {
        public static final String authorization_name = "authorization";
    }

    public static class Time {
        // 订单过期时间
        public final static long order_expire_time_gap = 30 * 60 * 1000;
        // 订单可退款时间
        public final static long order_can_refund_time_gap = 7 * 24 * 60 * 60 * 1000;
        // 订单资金解冻时间
        public final static long unfreeze_time_gap = 7 * 24 * 60 * 60 * 1000;
        // 提货可退货时间
        public final static long express_can_refund_time_gap = 7 * 24 * 60 * 60 * 1000;
        // 自动确认收货时间
        public final static long express_auto_confirm_time_gap = 7 * 24 * 60 * 60 * 1000;
        // 退货自动收货时间
        public final static long refund_express_auto_time_gap = 5 * 24 * 60 * 60 * 1000;
    }
}
