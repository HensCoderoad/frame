package com.redis.sharding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Collections;
@Component
public class RedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisService.class);

    private static JedisPool jedisPool;

    private long deadTimeLine = 200;

    static{
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(1000);
        jedisPoolConfig.setMaxTotal(1000);
        jedisPool = new JedisPool(jedisPoolConfig, "127.0.0.1", 6379);
    }

    public static void returnToPool(Jedis jedis) {
        jedis.close();
    }

    /**
     * 加锁
     * @param uuid
     * @param key
     * @param value
     * @param lockExpireTimeOut
     * @param lockWaitTimeOut
     * @return
     */
    public boolean lock(String uuid, String key, String value, Long lockExpireTimeOut, Long lockWaitTimeOut) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = uuid + key;
            for (; ; ) {
                if (jedis.setnx(realKey, value) == 1) {
                    return true;
                }
                String currentValue = jedis.get(realKey);
                // if lock is expired
                if (!StringUtils.isEmpty(currentValue) && Long.valueOf(currentValue) < System.currentTimeMillis()) {
                    String oldValue = jedis.getSet(realKey, value);
                    if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
                        return true;
                    }
                }
                lockWaitTimeOut = deadTimeLine - System.currentTimeMillis();

                if (lockWaitTimeOut <= 0L) {
                    return false;
                }
            }
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 解锁
     * @param uuid
     * @param key
     * @param value
     * @return
     */
    public boolean unlock(String uuid,String key, String value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            String realKey = uuid + key;
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Object result = jedis.eval(luaScript, Collections.singletonList(realKey), Collections.singletonList(value));
            if ("1".equals(result)) {
                return true;
            }
        }catch (Exception e){
            LOGGER.info("unlock error");
        }finally {
            returnToPool(jedis);
        }
        return false;
    }

}
