package com.veneno.delyQueue;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class RedisDelyQueue<T> {

    private JedisCluster jedis;

    private String queKey;

    static class TaskItem<T>{
        public String id;
        public T msg;
    }

    private Type taskType = new TypeReference<TaskItem<T>>() {
    }.getType();

    public RedisDelyQueue(JedisCluster jedis,String queKey){
        this.jedis = jedis;
        this.queKey = queKey;
    }

    //入队
    public void enqueue(T msg){
        //生成msg的封装对象
        TaskItem<T>  tTaskItem = new TaskItem<>();
        tTaskItem.id = UUID.randomUUID().toString();
        tTaskItem.msg = msg;
        String value = JSONObject.toJSONString(tTaskItem);
        // 将封装对象转成json后放入redis的zset,score为当前时间加上5秒延时
        jedis.zadd(queKey,System.currentTimeMillis()+5000,value);
    }

    // 出队

    public TaskItem<T> dequeue(){
        // 这样的读和删不是同一个原子,会导致其他线程白白读取到value
        while(!Thread.interrupted()){
            Set<String> value = jedis.zrangeByScore(queKey, 0, System.currentTimeMillis(), 0, 1);
            if(value.isEmpty()){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }
            String ms = value.iterator().next();
            if(jedis.zrem(queKey,ms)>0){
                return JSONObject.parseObject(ms,taskType);
            }
        }
        return null;
    }

    //handle

    public void handle(TaskItem<T> it){
        System.out.println(it.id);
        System.out.println(it.msg);
    }

    // 出队 使用lua来出队,将获取与删除放在同一个原子操作内
   private TaskItem<T> dequeueWithLua(){
       String script = " local resultDelayMsg = {}; " +
                       " local arr = redis.call('zrangebyscore', KEYS[1], '0', ARGV[1]) ; " +
                        " if next(arr) == nil then return resultDelayMsg  end ;" +
                        " if redis.call('zrem', KEYS[1], arr[1]) > 0 then table.insert(resultDelayMsg, arr[1]) return resultDelayMsg end ; " +
                        " return resultDelayMsg ; ";
        Object result = jedis.eval(script, Collections.singletonList(queKey), Collections.singletonList("" + System.currentTimeMillis()));
        List<String> value;
        if (result == null || (value = (List<String>) result).isEmpty()) {
            return null ;
        }

       return JSON.parseObject(value.iterator().next(), taskType);
    }


    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);
        RedisDelyQueue<String> queue = new RedisDelyQueue<>(jedis,"queue1");
        Thread producer = new Thread(() -> {
            for (int i = 0; i <5 ; i++) {
                queue.enqueue("i am "+(i+1)+" message!");
            }
        });

        Thread consumer = new Thread(()->{
            TaskItem<String> task = queue.dequeueWithLua();
            queue.handle(task);

        });
        producer.start();
        consumer.start();

        try {
            producer.join();
            Thread.sleep(6000);
            consumer.interrupt();
            consumer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

