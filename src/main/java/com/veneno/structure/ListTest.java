package com.veneno.structure;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;



public class ListTest {

    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);

        // 队列 FIFO
        jedis.rpush("heroes","剑圣","剑豪","剑姬","剑魔");

        for (int i = 0; i <5 ; i++) {
            System.out.print(jedis.lpop("heroes"));
            // 剑圣剑豪剑姬剑魔null
        }
        System.out.println();

        // 栈 LIFO
        jedis.rpush("heroes","剑圣","剑豪","剑姬","剑魔");

        for (int i = 0; i <5 ; i++) {
            System.out.print(jedis.rpop("heroes"));
            //剑魔剑姬剑豪剑圣null
        }
        System.out.println();
        /*
         *lindex 相当于 Java 链表的get(int index)方法，它需要对链表进行遍历，性能随着参数index增大而变差。
         *
         * ltrim 和字面上的含义不太一样，个人觉得它叫 lretain(保留) 更合适一些，因为 ltrim 跟的两个参数start_index和end_index定义了一个区间，在这个区间内的值，ltrim 要保留，区间之外统统砍掉。我们可以通过ltrim来实现一个定长的链表，这一点非常有用。
         *
         * index 可以为负数，index=-1表示倒数第一个元素，同样index=-2表示倒数第二个元素。
         */

        jedis.rpush("heroes","剑圣","剑豪","剑姬","剑魔");
        // 时间复杂度为O（n)慎用
        System.out.println(jedis.lindex("heroes", 3)); // 剑魔

        jedis.ltrim("heroes",1,-2);

        for (String heroes : jedis.lrange("heroes", 0, -1)) {
            System.out.print(heroes);
        }
        System.out.println();


    }
}
