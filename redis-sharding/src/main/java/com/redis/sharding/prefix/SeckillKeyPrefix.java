package com.redis.sharding.prefix;

public class SeckillKeyPrefix extends BasePrefix {

    public static SeckillKeyPrefix seckillKeyPrefix = new SeckillKeyPrefix("prefix");

    public SeckillKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public SeckillKeyPrefix(String prefix) {
        super(prefix);
    }

}
