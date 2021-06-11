package com.veneno.bitmap;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class Bitmap {

    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);

        char[] hello = {'h','e','l','l','o'};
        for (char c : hello) {
            System.out.println(c+" : "+Integer.toBinaryString((int) c));
        }
        // 零存
        jedis.setbit("s",1,true); //第一位是true即是1
        jedis.setbit("s",2,true); //第二位是true
        jedis.setbit("s",4,true);
        jedis.setbit("s",9,true);
        jedis.setbit("s",10,true);
        jedis.setbit("s",13,true);
        jedis.setbit("s",15,true);
        // 猜想结果为 01101000 01100101
        // 对应的char   h        e
        // 整取
        System.out.println(jedis.get("s"));

        // 零存
        jedis.setbit("w",1,true);
        jedis.setbit("w",2,true);
        jedis.setbit("w",4,true);
        // 零取
        for (int i = 0; i < 8 ; i++) {
            System.out.print(jedis.getbit("w", i)?1:0);
        }
        System.out.println();

        // 整存
        jedis.set("m","o");
        // 零取
        for (int i = 0; i < 8 ; i++) {
            System.out.print(jedis.getbit("m", i)?1:0);
        }
        System.out.println();

        jedis.setbit("x",0,true);
        jedis.setbit("x",1,true);
        System.out.println(jedis.get("x"));

        /*
         * Redis 提供了位图统计指令 bitcount 和位图查找指令 bitpos，bitcount 用来统计指定位置范围内 1 的个数，bitpos 用来查找指定范围内出现的第一个 0 或 1。
         *
         * 比如我们可以通过 bitcount 统计用户一共签到了多少天，通过 bitpos 指令查找用户从哪一天开始第一次签到。
         * 如果指定了范围参数[start, end]，就可以统计在某个时间范围内用户签到了多少天，用户自某天以后的哪天开始签到。
         *
         * 遗憾的是， start 和 end 参数是字节索引，也就是说指定的位范围必须是 8 的倍数，而不能任意指定。
         * 这很奇怪，我表示不是很能理解 Antirez 为什么要这样设计。
         * 因为这个设计，我们无法直接计算某个月内用户签到了多少天，
         * 而必须要将这个月所覆盖的字节内容全部取出来 (getrange 可以取出字符串的子串) 然后在内存里进行统计，这个非常繁琐。
         *
         * 接下来我们简单试用一下 bitcount 指令和 bitpos 指令:
         * */

        jedis.set("key","hello");
        System.out.println(jedis.bitcount("key"));
        jedis.bitcount("key",0,0); // 第一个字符中1出现的次数
        jedis.bitcount("key",0,1); // 前2个字符中1出现的次数


    }
}
