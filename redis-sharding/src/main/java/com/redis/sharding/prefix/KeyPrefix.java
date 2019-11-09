package com.redis.sharding.prefix;

public interface KeyPrefix {

    int expireSeconds();

    String getPrefix();
}
