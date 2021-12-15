package com.veneno.scan;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

public class Scan {
    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);
        /**
         * keys这个指令使用非常简单，提供一个简单的正则字符串即可，但是有很明显的两个缺点。
         *
         * 没有 offset、limit 参数，一次性吐出所有满足条件的 key，万一实例中有几百 w 个 key 满足条件，当你看到满屏的字符串刷的没有尽头时，你就知道难受了。
         * keys 算法是遍历算法，复杂度是 O(n)，如果实例中有千万级以上的 key，这个指令就会导致 Redis 服务卡顿，所有读写 Redis 的其它的指令都会被延后甚至会超时报错，因为 Redis 是单线程程序，顺序执行所有指令，其它指令必须等到当前的 keys 指令执行完了才可以继续。
         * 面对这两个显著的缺点该怎么办呢？
         *
         * Redis 为了解决这个问题，它在 2.8 版本中加入了大海捞针的指令——scan。scan 相比 keys 具备有以下特点:
         *
         * 复杂度虽然也是 O(n)，但是它是通过游标分步进行的，不会阻塞线程;
         * 提供 limit 参数，可以控制每次返回结果的最大条数，limit 只是一个 hint，返回的结果可多可少;
         * 同 keys 一样，它也提供模式匹配功能;
         * 服务器不需要为游标保存状态，游标的唯一状态就是 scan 返回给客户端的游标整数;
         * 返回的结果可能会有重复，需要客户端去重复，这点非常重要;
         * 遍历的过程中如果有数据修改，改动后的数据能不能遍历到是不确定的;
         * 单次返回的结果是空的并不意味着遍历结束，而要看返回的游标值是否为零;
         *
         */
        for (int i = 0; i < 1000; i++) {
            jedis.set("{veneno-test}-"+i,"hello");
        }
        //好，Redis 中现在有了 10000 条数据，接下来我们找出以 veneno-test 开头 key 列表。
        //
        //scan 参数提供了三个参数，第一个是 cursor 整数值，第二个是 key 的正则模式
        //第三个是遍历的 limit hint。第一次遍历时，cursor 值为 0，
        // 然后将返回结果中第一个整数值作为下一次遍历的 cursor。一直遍历到返回的 cursor 值为 0 时结束。
        ScanParams scanParams = new ScanParams();
        scanParams.count(5).match("{veneno-test}-*");
        ScanResult<String> scan = jedis.scan(ScanParams.SCAN_POINTER_START, scanParams);
        System.out.println(scan.getCursor());
        for (String key : scan.getResult()) {
            System.out.println(key);
        }
        //从上面的过程可以看到虽然提供的 limit 是 100，但是返回的结果为60左右。
        // 因为这个 limit 不是限定返回结果的数量，而是限定服务器单次遍历的字典槽位数量(约等于)。
        // 如果将 limit 设置为 5，你会发现返回结果是空的，但是游标值不为零，意味着遍历还没结束。





        //scan 指令返回的游标就是第一维数组的位置索引，我们将这个位置索引称为槽 (slot)。
        // 如果不考虑字典的扩容缩容，直接按数组下标挨个遍历就行了。
        // limit 参数就表示需要遍历的槽位数，之所以返回的结果可能多可能少，
        // 是因为不是所有的槽位上都会挂接链表，有些槽位可能是空的，还有些槽位上挂接的链表上的元素可能会有多个。
        // 每一次遍历都会将 limit 数量的槽位上挂接的所有链表元素进行模式匹配过滤后，一次性返回给客户端。
    }
}
