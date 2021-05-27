package com.veneno.structure;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class ZsetTest {

    /*
     *zset 可能是 Redis 提供的最为特色的数据结构，它也是在面试中面试官最爱问的数据结构。
     * 它类似于 Java 的 SortedSet 和 HashMap 的结合体，一方面它是一个 set，
     * 保证了内部 value 的唯一性，另一方面它可以给每个 value 赋予一个 score，
     * 代表这个 value 的排序权重。它的内部实现用的是一种叫做「跳跃列表」的数据结构。
     * zset 中最后一个 value 被移除后，数据结构自动删除，内存被回收。
     */


    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);
        String key = "heroes";
        jedis.zadd(key,9.5,"YI");
        jedis.zadd(key,9.6,"JJ");
        jedis.zadd(key,9.4,"yasuo");
        jedis.zadd(key,9.8,"jax");
        jedis.zrange(key,0,-1).forEach(System.out::println);
        System.out.println(jedis.zrevrange(key, 0, -1));
        System.out.println(jedis.zcard(key));
        System.out.println("yasuo:"+jedis.zscore(key, "yasuo"));
        jedis.zrangeByScore(key,9.5,10.0).forEach(System.out::println);
        jedis.zrangeByScore(key,"-inf","inf").forEach(System.out::println);
        jedis.zrangeByScoreWithScores(key,9.5,9.6).forEach(System.out::println);
        jedis.zrem(key,"JJ");
        jedis.zrangeWithScores(key,0,-1).forEach(System.out::println);

        jedis.del(key);

        //list/set/hash/zset 这四种数据结构是容器型数据结构，它们共享下面两条通用规则：
        //
        //create if not exists
        //
        //如果容器不存在，那就创建一个，再进行操作。比如 rpush 操作刚开始是没有列表的，Redis 就会自动创建一个，然后再 rpush 进去新元素。
        //
        //drop if no elements
        //
        //如果容器里元素没有了，那么立即删除元素，释放内存。这意味着 lpop 操作到最后一个元素，列表就消失了


        /*
         * Redis 所有的数据结构都可以设置过期时间，时间到了，
         * Redis 会自动删除相应的对象。需要注意的是过期是以对象为单位，
         * 比如一个 hash 结构的过期是整个 hash 对象的过期，而不是其中的某个子 key。
         * 还有一个需要特别注意的地方是如果一个字符串已经设置了过期时间，然后你调用了 set 方法修改了它，它的过期时间会消失
         */

        jedis.setex("test",5,"test");

        System.out.println(jedis.ttl("test"));
        jedis.set("test","我已经永生");
        try {
            Thread.sleep(5100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(jedis.ttl("test"));
        System.out.println(jedis.exists("test"));
        System.out.println(jedis.get("test"));
    }

}
