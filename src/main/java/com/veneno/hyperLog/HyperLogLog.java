package com.veneno.hyperLog;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

public class HyperLogLog {

    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        Jedis jedis = new Jedis("10.0.4.18",7001);
//        JedisCluster jedis = new JedisCluster(hp);

        //至于这个指令为什么是pf,因为hyperloglog这种数据结构的发明人叫Philippe flajolet

//        String key  = " uniqueVisitor";
//        jedis.del(key);
//        jedis.pfadd(key,"龙杰");
//        System.out.println(jedis.pfadd(key, "龙杰"));
//        for (int i = 0; i < 1000; i++) {
//            jedis.pfadd(key, "龙杰"+i);
//        }
//        // 这个值应该是1001,但是实际上是997,hyperLogLog不是非常精确的,但是用于统计uv这种数据实际上一个近似值非常准确了
//        System.out.println(jedis.pfcount(key));

        jedis.pfadd("404","老刘","张海军");
        jedis.pfadd("403","王波");



        // 讲访问403页面的用户数量合并到404中
        System.out.println(jedis.pfmerge("404", "403"));


    }
}
