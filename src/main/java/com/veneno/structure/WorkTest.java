package com.veneno.structure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.veneno.DTO.User;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.Map;
import java.util.TreeMap;

public class WorkTest {
    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);
        String key = "hashUser";

        String js = JSON.toJSONString(new User(100,"cloud","god"));
        jedis.set("testUser",js);
        System.out.println(JSONObject.parseObject(jedis.get("testUser"), User.class));

        Map<String,String> map = new TreeMap<>();

        map.put("id","99");
        map.put("name","kang");
        map.put("pwd","u-guess");

        jedis.hset(key,map);
        Map<String, String> stringStringMap = jedis.hgetAll(key);
    }
}
