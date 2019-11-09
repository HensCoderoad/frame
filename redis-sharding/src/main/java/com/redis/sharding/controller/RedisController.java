package com.redis.sharding.controller;

import com.redis.sharding.service.RedisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@RestController
@RequestMapping("/redis")
public class RedisController {
    private static LongAdder longAdder = new LongAdder();
    private static Long LOCK_EXPIRE_TIME = 200L;
    private static Long stock = 10000L;
    @Resource
    private RedisService redisService;

    static {
        longAdder.add(10000L);
    }

    @GetMapping("/v1/seckill")
    public String seckillV1(){
        Long time = System.currentTimeMillis() + LOCK_EXPIRE_TIME;
        String uuid = UUID.randomUUID().toString();
        if(!redisService.lock(uuid,"redis-seckill", String.valueOf(time), 1000L, 1000L)){
            return "人太多了，换个姿势操作一下";
        }
        if(longAdder.longValue() == 0L){
            return "已经抢光";
        }
        doSomeThing();

        if(longAdder.longValue() == 0L){
            return "已经抢光";
        }

        longAdder.decrement();

        redisService.unlock(uuid,"redis-seckill", String.valueOf(time));

        Long stock = longAdder.longValue();
        Long bought = 10000L - stock;
        return "已抢" + bought + ",还剩下" + stock;
    }

    @GetMapping("/detail")
    public String detail(){
        Long stock = longAdder.longValue();
        Long bought = 10000L - stock;
        return "已抢" + bought + ",还剩下" + stock;
    }
    @GetMapping("/v2/seckill")
    public String seckillV2(){
        if (longAdder.longValue() == 0L){
            return "已经抢光";
        }
        longAdder.decrement();
        Long stock = longAdder.longValue();
        Long bought = 10000L - stock;
        return "已抢" + bought + ",还剩下" + stock;
    }
    @GetMapping("/v3/seckill")
    public String seckillV3(){
        if(stock == 0){
            return "已经抢光";
        }
        doSomeThing();
        stock--;
        Long bought = 10000L - stock;
        return "已抢" + bought + ",还剩下" + stock;
    }

    public void doSomeThing(){
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
