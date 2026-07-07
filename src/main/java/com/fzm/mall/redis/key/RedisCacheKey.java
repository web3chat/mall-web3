package com.fzm.mall.redis.key;

/**
 * redis缓存的key
 */
public class RedisCacheKey {
    private static final String prefix = "mall-chain:cache:";

    public static class Authorization {
        public static final String to_address = prefix + "authorization:%s";
    }

    public static class User {
        public static final String front = prefix + "user:front:%s";
        public static final String back = prefix + "user:back:%s";
    }

    public static class Token {
        public static final String serial_prefix = prefix + "token:serial:prefix";
        public static final String serial_sku_id = prefix + "token:serial:sku_id:%s";
    }

    public static class Id {
        public static final String spu_id = prefix + "id:spu:%s";
        public static final String sku_id = prefix + "id:sku:%s";
        public static final String order_id = prefix + "id:order:%s";
    }

    public static class CoinPrice {
        public static final String price = prefix + "price:%s_%s";
        public static final String price_by_address = prefix + "price:address:%s_%s:%s";
    }

    public static class ChainEvent {
        public static final String confirm_height = prefix + "chain_event:confirm_height";
    }

    public static class Order {
        public static final String logistics_by_order_id = prefix + "order:logistics:%s:%s";
    }

    public static class Siwe {
        public static final String nonce = prefix + "siwe:nonce:%s";
    }
}
