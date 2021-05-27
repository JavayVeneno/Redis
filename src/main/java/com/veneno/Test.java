package com.veneno;

import com.alibaba.fastjson.JSON;
import com.veneno.DTO.User;
import redis.clients.jedis.Jedis;

import java.util.Random;

public class Test {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("10.0.4.18",7000);
//        for (int i = 0; i <5 ; i++) {
//            User demo = new User(i, "我是第" + i + "条",  new Random().nextDouble());
//            jedis.sadd("demo", JSON.toJSONString(demo));
//        }
//
//        System.out.println(jedis.smembers("demo"));
//        System.out.println(jedis.del("demo"));
       }


}
