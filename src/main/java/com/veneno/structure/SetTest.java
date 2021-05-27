package com.veneno.structure;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class SetTest {

    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);

        String key = "heroes";
        System.out.println(jedis.sadd(key, "YI"));// 1
        System.out.println(jedis.sadd(key, "YI"));// 0
        jedis.sadd(key,"JJ","Yasuo","Jax");
        jedis.smembers(key).forEach(System.out::println); // 无序
        System.out.println(jedis.sismember(key, "YI")); // true
        System.out.println(jedis.scard(key));// 4
        System.out.println(jedis.spop(key));// 随机
        jedis.del(key);
    }
}
