package com.veneno.RateLimiter;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

public class SimpleRateLimiter {

    private Jedis jedis;

    public SimpleRateLimiter(Jedis jedis){
        this.jedis = jedis;
    }
    public boolean isActionAllowed(String userId,String actionKey,int period,int maxCount){
        String key = String.format("%s:%s",userId,actionKey);
        long nowTime = System.currentTimeMillis();
        Pipeline pipe = jedis.pipelined();
        pipe.multi();
        pipe.zadd(key,nowTime,""+nowTime);
        pipe.zremrangeByScore(key,0,nowTime-period*1000);
        Response<Long> count = pipe.scard(key);
        pipe.expire(key,period+1);
        pipe.exec();
        pipe.close();
        return count.get() <=maxCount;
    }

    public static void main(String[] args) {
        Jedis jedis = new Jedis("10.0.4.18",7001);
        SimpleRateLimiter simpleRateLimiter = new SimpleRateLimiter(jedis);
        for (int i = 0; i < 20; i++) {
            System.out.println(simpleRateLimiter.isActionAllowed("老刘","看书",60,5));

        }
    }

}
