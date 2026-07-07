package com.fzm.mall.redis.key;

/**
 * redis缓存的key
 */
public class RedisLockKey {
    private static final String prefix = "mall-chain:lock:";

    public static class Mint {
        public static final String goods_id_judge = prefix + "mint:goods_id_judge:%s";
        public static final String sku_id_judge = prefix + "mint:sku_id_judge:%s";
    }

    public static class Order {
        public static final String info_expire = prefix + "order:info_expire:%s";
        public static final String unfreeze = prefix + "order:info_unfreeze:%s";
        public static final String express_confirm = prefix + "order:express_confirm:%s";
        public static final String refund_confirm = prefix + "order:refund_confirm:%s";
    }

    public static class Chain {
        public static final String nonce_of_address = prefix + "chain:nonce:address:%s";
        public static final String event_listener = prefix + "chain:event:listener";
    }

    public static class Withdraw {
        public static final String back_confirm = prefix + "withdraw:back_confirm:%s";
    }

    public static class Airdrop {
        public static final String info_id = prefix + "airdrop:info_id:%s";
    }
}
