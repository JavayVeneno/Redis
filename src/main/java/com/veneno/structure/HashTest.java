package com.veneno.structure;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.Map;

public class HashTest {

    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);
        String key = "heroes";
        jedis.del(key);

        jedis.hset(key,"first","masteryi");
        Map<String, String> jj = new HashMap<String, String>();
        jj.put("second","fiona");
        jedis.hset(key,jj);
        jedis.hset(key,"third","yasuo");

        Map<String, String> all = jedis.hgetAll(key);
        for (String s : all.keySet()) {
            System.out.println(s+":"+all.get(s));
        }
        System.out.println(jedis.hget(key, "first"));
        jedis.hset(key,"first","masterYi");
        System.out.println(jedis.hget(key, "first"));
        jj.put("4f","jax");
        jedis.hmset(key,jj);
        jedis.hset(key,"me","99");
        jedis.hincrBy(key,"me",1);
        System.out.println(jedis.hlen(key));
        System.out.println(jedis.hget(key,"me"));
        jedis.del(key);
    }
}
