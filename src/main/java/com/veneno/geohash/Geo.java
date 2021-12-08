package com.veneno.geohash;

import com.veneno.DTO.Company;
import redis.clients.jedis.GeoRadiusResponse;
import redis.clients.jedis.GeoUnit;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.params.GeoRadiusParam;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Geo {
    public static void main(String[] args) {
        HostAndPort hp = new HostAndPort("10.0.4.18",7001);
        JedisCluster jedis = new JedisCluster(hp);
        String key = "company";
        List<Company> companies = new ArrayList<>(5);
        companies.add(new Company(116.336295,39.977498,"byte_dance"));
        companies.add(new Company(116.497608,40.007945,"alibaba"));
        companies.add(new Company(116.279767,40.046512,"qq"));
        companies.add(new Company(116.570059,39.791678,"jd"));
        // geoadd 指令携带集合名称以及多个经纬度名称三元组，注意这里可以加入多个三元组 ,
        // 也许你会问为什么 Redis 没有提供 geo 删除指令？前面我们提到 geo 存储结构上使用的是 zset，
        // 意味着我们可以使用 zset 相关的指令来操作 geo 数据，所以删除指令可以直接使用 zrem 指令即可。
//        for (Company c :companies) {
//            System.out.println(jedis.geoadd(key, c.getLongitude(), c.getLatitude(), c.getName()));
//        }
        //geodist 指令可以用来计算两个元素之间的距离，携带集合名称、2 个名称和距离单位。
        // 千米
        System.out.println(jedis.geodist(key,"qq","jd", GeoUnit.KM));
        // 米
        System.out.println(jedis.geodist(key,"qq","alibaba", GeoUnit.KM));
        // 英里
        System.out.println(jedis.geodist(key,"alibaba","byte_dance", GeoUnit.MI));
        // 尺
        System.out.println(jedis.geodist(key,"qq","jd", GeoUnit.FT));

        //geopos 指令可以获取集合中任意元素的经纬度坐标，可以一次获取多个。

        System.out.println(jedis.geopos(key,"byte_dance"));
        // 116.336295,39.977498

        // [(116.33629649877548,39.97749919111771)]
        // 我们观察到获取的经纬度坐标和 geoadd 进去的坐标有轻微的误差，原因是 geohash 对二维坐标进行的一维映射是有损的，
        // 通过映射再还原回来的值会出现较小的差别。对于「附近的人」这种功能来说，这点误差根本不是事

        System.out.println(jedis.geohash(key,"qq"));

        List<GeoRadiusResponse> qqs =
                jedis.georadiusByMember(
                        key, "qq", 10.0,
                        GeoUnit.KM, GeoRadiusParam.geoRadiusParam().count(3).sortAscending());
        //距离qq最近10公里的公司除了qq自己就是字节跳动了,改方法返回了ascii数组,是menber
        for (GeoRadiusResponse company : qqs) {
            System.out.println(new String(company.getMember(), StandardCharsets.UTF_8));
        }

        List<GeoRadiusResponse> res =jedis.georadiusByMember(
                        key, "qq", 10.0,
                        GeoUnit.KM, GeoRadiusParam.geoRadiusParam().count(3).sortDescending().withCoord().withDist());
        //添加参数后返回更多参数
        for (GeoRadiusResponse company : res) {
            String name = new String(company.getMember(), StandardCharsets.UTF_8);
            System.out.printf("%s's distance = %f,%s 's 坐标:%s \n",name,company.getDistance() ,name,company.getCoordinate());
        }
        //除了 georadiusbymember 指令根据元素查询附近的元素，
        // Redis 还提供了根据坐标值来查询附近的元素，这个指令更加有用，
        // 它可以根据用户的定位来计算「附近的车」，「附近的餐馆」等。
        // 它的参数和 georadiusbymember 基本一致，除了将目标元素改成经纬度坐标值。

        List<GeoRadiusResponse> other = jedis.georadius(key, 116.514202, 39.905409, 20,
                GeoUnit.KM, GeoRadiusParam.geoRadiusParam().count(2).sortAscending().withCoord().withDist());

        for (GeoRadiusResponse company : other) {
            String name = new String(company.getMember(), StandardCharsets.UTF_8);
            System.out.printf("%s's distance = %f,%s 's 坐标:%s \n",name,company.getDistance() ,name,company.getCoordinate());
        }
        //在一个地图应用中，车的数据、餐馆的数据、人的数据可能会有百万千万条，
        // 如果使用 Redis 的 Geo 数据结构，它们将全部放在一个 zset 集合中。
        // 在 Redis 的集群环境中，集合可能会从一个节点迁移到另一个节点，
        // 如果单个 key 的数据过大，会对集群的迁移工作造成较大的影响，
        // 在集群环境中单个 key 对应的数据量不宜超过 1M，否则会导致集群迁移出现卡顿现象，影响线上服务的正常运行。
        // 所以，这里建议 Geo 的数据使用单独的 Redis 实例部署，不使用集群环境。
        // 如果数据量过亿甚至更大，就需要对 Geo 数据进行拆分，按国家拆分、按省拆分，按市拆分，在人口特大城市甚至可以按区拆分。
        // 这样就可以显著降低单个 zset 集合的大小。

        // 使用zrem删除测试数据。
        System.out.println(jedis.zrem(key, "qq", "byte_dance", "jd", "alibaba"));
    }
}
