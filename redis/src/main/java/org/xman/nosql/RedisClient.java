package org.xman.nosql;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.SortingParams;


public class RedisClient {

    private Jedis jedis;//非切片池连接客户端
    private ShardedJedis sharedJedis;//切片池连接客户端

    RedisClient() {
        sharedJedis = RedisUtil.getShardedRedisClient();
        jedis = RedisUtil.getRedisClient();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void close() {
        jedis.close();
        sharedJedis.close();
    }

    void keyOperate() {
        System.out.println("======================key==========================");
        // 清空数据
        System.out.println("清空库中所有数据：" + jedis.flushDB());

        // 判断key否存在
        System.out.println("判断key999键是否存在：" + sharedJedis.exists("key999"));
        System.out.println("新增key001,value001键值对：" + sharedJedis.set("key001", "value001"));
        System.out.println("判断key001是否存在：" + sharedJedis.exists("key001"));
        System.out.println("新增key002,value002键值对：" + sharedJedis.set("key002", "value002"));

        // 输出系统中所有的key
        System.out.println("系统中所有键如下：");
        jedis.keys("*").forEach(System.out::println);

        // 删除某个key,若key不存在，则忽略该命令。
//        System.out.println("系统中删除key002: " + jedis.del("key002"));
        System.out.println("系统中删除key002: " + sharedJedis.del("key002"));
        System.out.println("判断key002是否存在：" + sharedJedis.exists("key002"));
        // 设置 key001的过期时间
        System.out.println("设置 key001的过期时间为5秒:" + jedis.expire("key001", 5));

        sleep(2000);

        // 查看某个key的剩余生存时间,单位【秒】.永久生存或者不存在的都返回-1
        System.out.println("查看key001的剩余生存时间：" + jedis.ttl("key001"));
        // 移除某个key的生存时间
        System.out.println("移除key001的生存时间：" + jedis.persist("key001"));
        System.out.println("查看key001的剩余生存时间：" + jedis.ttl("key001"));
        // 查看key所储存的值的类型
        System.out.println("查看key所储存的值的类型：" + jedis.type("key001"));

        /*
         * 一些其他方法：
         * 1、修改键名：jedis.rename("key6", "key0");
         * 2、将当前db的key移动到给定的db当中：jedis.move("foo", 1)
         */
    }

    void stringOperate() {
        System.out.println("======================String_1==========================");
        // 清空数据
        System.out.println("清空库中所有数据：" + jedis.flushDB());

        System.out.println("=============增=============");
        jedis.set("key001", "value001");
        jedis.set("key002", "value002");
        jedis.set("key003", "value003");
        System.out.println("已新增的3个键值对如下：");
        System.out.println(jedis.get("key001"));
        System.out.println(jedis.get("key002"));
        System.out.println(jedis.get("key003"));

        System.out.println();
        System.out.println("=============删=============");
        System.out.println("删除key003键值对：" + jedis.del("key003"));
        System.out.println("获取key003键对应的值：" + jedis.get("key003"));

        System.out.println();
        System.out.println("=============改=============");
        //1、直接覆盖原来的数据
        System.out.println("直接覆盖key001原来的数据：" + jedis.set("key001", "value001-update"));
        System.out.println("获取key001对应的新值：" + jedis.get("key001"));
        //2、直接覆盖原来的数据
        System.out.println("在key002原来值后面追加：" + jedis.append("key002", "+appendString"));
        System.out.println("获取key002对应的新值" + jedis.get("key002"));

        System.out.println();
        System.out.println("=============增，删，查（多个）=============");
        /**
         * mset,mget同时新增，修改，查询多个键值对
         * 等价于：
         * jedis.set("name","ssss");
         * jedis.set("jarorwar","xxxx");
         */
        System.out.println("一次性新增key201,key202,key203,key204及其对应值：" +
                jedis.mset("key201", "value201", "key202", "value202", "key203", "value203", "key204", "value204"));
        System.out.println("一次性获取key201,key202,key203,key204各自对应的值：" +
                jedis.mget("key201", "key202", "key203", "key204"));
        System.out.println("一次性删除key201,key202：" + jedis.del("key201", "key202"));
        System.out.println("一次性获取key201,key202,key203,key204各自对应的值：" +
                jedis.mget("key201", "key202", "key203", "key204"));


        System.out.println();
        //jedis具备的功能sharedJedis中也可直接使用，下面测试一些前面没用过的方法
        System.out.println("======================String_2==========================");
        // 清空数据
        System.out.println("清空库中所有数据：" + jedis.flushDB());

        System.out.println("=============新增键值对时防止覆盖原先值=============");
        System.out.println("原先key301不存在时，新增key301：" + sharedJedis.setnx("key301", "value301"));
        System.out.println("原先key302不存在时，新增key302：" + sharedJedis.setnx("key302", "value302"));
        System.out.println("当key302存在时，尝试新增key302：" + sharedJedis.setnx("key302", "value302_new"));
        System.out.println("获取key301对应的值：" + sharedJedis.get("key301"));
        System.out.println("获取key302对应的值：" + sharedJedis.get("key302"));

        System.out.println("=============超过有效期键值对被删除=============");
        // 设置key的有效期，并存储数据
        System.out.println("新增key303，并指定过期时间为2秒" + sharedJedis.setex("key303", 2, "key303-2second"));
        System.out.println("获取key303对应的值：" + sharedJedis.get("key303"));

        sleep(3000);

        System.out.println("3秒之后，获取key303对应的值：" + sharedJedis.get("key303"));

        System.out.println("=============获取原值，更新为新值一步完成=============");
        System.out.println("key302原值：" + sharedJedis.getSet("key302", "value302-after-getset"));
        System.out.println("key302新值：" + sharedJedis.get("key302"));

        System.out.println("=============获取子串=============");
        System.out.println("获取key302对应值中的子串：" + sharedJedis.getrange("key302", 5, 7));
    }

    void listOperate() {
        System.out.println("======================list==========================");
        // 清空数据
        System.out.println("清空库中所有数据：" + jedis.flushDB());

        System.out.println("=============增=============");
        sharedJedis.lpush("stringlists", "vector");
        sharedJedis.lpush("stringlists", "ArrayList");
        sharedJedis.lpush("stringlists", "vector");
        sharedJedis.lpush("stringlists", "vector");
        sharedJedis.lpush("stringlists", "LinkedList");
        sharedJedis.lpush("stringlists", "MapList");
        sharedJedis.lpush("stringlists", "SerialList");
        sharedJedis.lpush("stringlists", "HashList");
        sharedJedis.lpush("numberlists", "3");
        sharedJedis.lpush("numberlists", "1");
        sharedJedis.lpush("numberlists", "5");
        sharedJedis.lpush("numberlists", "2");
        System.out.println("所有元素-stringlists：" + sharedJedis.lrange("stringlists", 0, -1));
        System.out.println("所有元素-numberlists：" + sharedJedis.lrange("numberlists", 0, -1));

        System.out.println();
        System.out.println("=============删=============");
        // 删除列表指定的值 ，第二个参数为删除的个数（有重复时），后add进去的值先被删，类似于出栈
        System.out.println("成功删除指定元素个数-stringlists：" + sharedJedis.lrem("stringlists", 2, "vector"));
        System.out.println("删除指定元素之后-stringlists：" + sharedJedis.lrange("stringlists", 0, -1));
        // 删除区间以外的数据
        System.out.println("删除下标0-3区间之外的元素：" + sharedJedis.ltrim("stringlists", 0, 3));
        System.out.println("删除指定区间之外元素后-stringlists：" + sharedJedis.lrange("stringlists", 0, -1));
        // 列表元素出栈
        System.out.println("出栈元素：" + sharedJedis.lpop("stringlists"));
        System.out.println("元素出栈后-stringlists：" + sharedJedis.lrange("stringlists", 0, -1));

        System.out.println();
        System.out.println("=============改=============");
        // 修改列表中指定下标的值
        sharedJedis.lset("stringlists", 0, "hello list!");
        System.out.println("下标为0的值修改后-stringlists：" + sharedJedis.lrange("stringlists", 0, -1));

        System.out.println();
        System.out.println("=============查=============");
        // 数组长度
        System.out.println("长度-stringlists：" + sharedJedis.llen("stringlists"));
        System.out.println("长度-numberlists：" + sharedJedis.llen("numberlists"));

        // 排序
        /*
         * list中存字符串时必须指定参数为alpha，如果不使用SortingParams，而是直接使用sort("list")，
         * 会出现"ERR One or more scores can't be converted into double"
         */
        SortingParams sortingParameters = new SortingParams();
        sortingParameters.alpha();
        sortingParameters.limit(0, 3);
        System.out.println("返回排序后的结果-stringlists：" + sharedJedis.sort("stringlists", sortingParameters));
        System.out.println("返回排序后的结果-numberlists：" + sharedJedis.sort("numberlists"));
        // 子串：  start为元素下标，end也为元素下标；-1代表倒数一个元素，-2代表倒数第二个元素
        System.out.println("子串-第二个开始到结束：" + sharedJedis.lrange("stringlists", 1, -1));
        // 获取列表指定下标的值
        System.out.println("获取下标为2的元素：" + sharedJedis.lindex("stringlists", 2) + "\n");

        jedis.brpoplpush("stringlists", "stringlists2", 0);
    }

    void setOperate() {
        System.out.println("======================set==========================");
        // 清空数据
        System.out.println("清空库中所有数据：" + jedis.flushDB());

        System.out.println("=============增=============");
        System.out.println("向sets集合中加入元素element001：" + jedis.sadd("sets", "element001"));
        System.out.println("向sets集合中加入元素element002：" + jedis.sadd("sets", "element002"));
        System.out.println("向sets集合中加入元素element003：" + jedis.sadd("sets", "element003"));
        System.out.println("向sets集合中加入元素element004：" + jedis.sadd("sets", "element004"));
        System.out.println("查看sets集合中的所有元素:" + jedis.smembers("sets"));

        System.out.println();
        System.out.println("=============删=============");
        System.out.println("集合sets中删除元素element003：" + jedis.srem("sets", "element003"));
        System.out.println("查看sets集合中的所有元素:" + jedis.smembers("sets"));
        /*System.out.println("sets集合中任意位置的元素出栈："+jedis.spop("sets"));//注：出栈元素位置居然不定？--无实际意义
        System.out.println("查看sets集合中的所有元素:"+jedis.smembers("sets"));*/

        System.out.println();
        System.out.println("=============改=============");

        System.out.println();
        System.out.println("=============查=============");
        System.out.println("判断element001是否在集合sets中：" + jedis.sismember("sets", "element001"));
        System.out.println("循环查询获取sets中的每个元素：");

        jedis.smembers("sets").forEach(System.out::println);

        System.out.println();
        System.out.println("=============集合运算=============");
        System.out.println("sets1中添加元素element001：" + jedis.sadd("sets1", "element001"));
        System.out.println("sets1中添加元素element002：" + jedis.sadd("sets1", "element002"));
        System.out.println("sets1中添加元素element003：" + jedis.sadd("sets1", "element003"));
        System.out.println("sets1中添加元素element002：" + jedis.sadd("sets2", "element002"));
        System.out.println("sets1中添加元素element003：" + jedis.sadd("sets2", "element003"));
        System.out.println("sets1中添加元素element004：" + jedis.sadd("sets2", "element004"));
        System.out.println("查看sets1集合中的所有元素:" + jedis.smembers("sets1"));
        System.out.println("查看sets2集合中的所有元素:" + jedis.smembers("sets2"));
        System.out.println("sets1和sets2交集：" + jedis.sinter("sets1", "sets2"));
        System.out.println("sets1和sets2并集：" + jedis.sunion("sets1", "sets2"));
        System.out.println("sets1和sets2差集：" + jedis.sdiff("sets1", "sets2"));//差集：set1中有，set2中没有的元素
    }

    void sortedSetOperate() {
        System.out.println("======================zset==========================");
        // 清空数据
        System.out.println(jedis.flushDB());

        System.out.println("=============增=============");
        System.out.println("zset中添加元素element001：" + sharedJedis.zadd("zset", 7.0, "element001"));
        System.out.println("zset中添加元素element002：" + sharedJedis.zadd("zset", 8.0, "element002"));
        System.out.println("zset中添加元素element003：" + sharedJedis.zadd("zset", 2.0, "element003"));
        System.out.println("zset中添加元素element004：" + sharedJedis.zadd("zset", 3.0, "element004"));
        System.out.println("zset集合中的所有元素：" + sharedJedis.zrange("zset", 0, -1));//按照权重值排序

        System.out.println();
        System.out.println("=============删=============");
        System.out.println("zset中删除元素element002：" + sharedJedis.zrem("zset", "element002"));
        System.out.println("zset集合中的所有元素：" + sharedJedis.zrange("zset", 0, -1));

        System.out.println();
        System.out.println("=============改=============");
        System.out.println();

        System.out.println("=============查=============");
        System.out.println("统计zset集合中的元素中个数：" + sharedJedis.zcard("zset"));
        System.out.println("统计zset集合中权重某个范围内（1.0——5.0），元素的个数：" + sharedJedis.zcount("zset", 1.0, 5.0));
        System.out.println("查看zset集合中element004的权重：" + sharedJedis.zscore("zset", "element004"));
        System.out.println("查看下标1到2范围内的元素值：" + sharedJedis.zrange("zset", 1, 2));

    }

    void hashOperate() {
        System.out.println("======================hash==========================");
        //清空数据
        System.out.println(jedis.flushDB());

        System.out.println("=============增=============");
        System.out.println("hashs中添加key001和value001键值对：" + sharedJedis.hset("hashs", "key001", "value001"));
        System.out.println("hashs中添加key002和value002键值对：" + sharedJedis.hset("hashs", "key002", "value002"));
        System.out.println("hashs中添加key003和value003键值对：" + sharedJedis.hset("hashs", "key003", "value003"));
        System.out.println("新增key004和4的整型键值对：" + sharedJedis.hincrBy("hashs", "key004", 4L));
        System.out.println("hashs中的所有值：" + sharedJedis.hvals("hashs"));
        System.out.println();

        System.out.println("=============删=============");
        System.out.println("hashs中删除key002键值对：" + sharedJedis.hdel("hashs", "key002"));
        System.out.println("hashs中的所有值：" + sharedJedis.hvals("hashs"));
        System.out.println();

        System.out.println("=============改=============");
        System.out.println("key004整型键值的值增加100：" + sharedJedis.hincrBy("hashs", "key004", 100L));
        System.out.println("hashs中的所有值：" + sharedJedis.hvals("hashs"));
        System.out.println();

        System.out.println("=============查=============");
        System.out.println("判断key003是否存在：" + sharedJedis.hexists("hashs", "key003"));
        System.out.println("获取key004对应的值：" + sharedJedis.hget("hashs", "key004"));
        System.out.println("批量获取key001和key003对应的值：" + sharedJedis.hmget("hashs", "key001", "key003"));
        System.out.println("获取hashs中所有的key：" + sharedJedis.hkeys("hashs"));
        System.out.println("获取hashs中所有的value：" + sharedJedis.hvals("hashs"));
        System.out.println();
    }
}