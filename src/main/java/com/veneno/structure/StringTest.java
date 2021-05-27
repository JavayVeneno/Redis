package com.veneno.structure;


import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class StringTest {
    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);

        System.out.println(jedis.exists("name")); // false

        System.out.println(jedis.set("name", "张志林")); // OK

        System.out.println(jedis.get("name")); // 张志林

        System.out.println(jedis.del("name")); // 1

        System.out.println(jedis.get("name")); // null

        jedis.set("name1","李凯");
        jedis.expire("name1",5);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(jedis.get("name1")); // 李凯

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(jedis.get("name1")); // null

        jedis.setex("name2",5,"陈康力");
        System.out.println(jedis.get("name2")); // 陈康力

        System.out.println(jedis.setnx("name2", "秦云峰")); // 0
        System.out.println(jedis.get("name2"));  // 陈康力


        jedis.set("grade","50");

        jedis.incr("grade");

        jedis.incrBy("grade",50);
        jedis.incrBy("grade",-1);

        System.out.println(jedis.get("grade")); // 100

        /*
         *
         * 字符串是由多个字节组成，每个字节又是由 8 个 bit 组成，如此便可以将一个字符串看成很多 bit 的组合，这便是 bitmap
         */


    }
}
