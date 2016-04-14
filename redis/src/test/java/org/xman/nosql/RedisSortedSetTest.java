package org.xman.nosql;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.HashMap;

public class RedisSortedSetTest {
    private static Jedis jedis;

    @BeforeClass
    public static void init() {
        jedis = RedisUtil.getRedisClient();
        jedis.select(8);
        jedis.flushDB();
    }

    @Test
    public void testSaddOneByOne() {
        jedis.flushDB();

        String key ="memory";

        jedis.zadd(key, 1, "1m");
        jedis.zadd(key, 64, "64m");
        jedis.zadd(key, 512, "512m");

        Assert.assertEquals(3, jedis.zcard(key), 0);
        Assert.assertEquals(1, jedis.zcount(key, 8, 128), 0);
        Assert.assertEquals(2, jedis.zcount(key, 1, 128), 0);
    }

    @Test
    public void testSaddBatch() {
        jedis.flushDB();
        String key ="memory";

        HashMap<String, Double> items = new HashMap<>();
        items.put("1m", 1D);
        items.put("64m", 64D);
        items.put("512m", 512D);

        jedis.zadd(key, items);

        Assert.assertEquals(3, jedis.zcard(key), 0);
        Assert.assertEquals(2, jedis.zcount(key, 1, 128), 0);
        Assert.assertEquals(1, jedis.zcount(key, 8, 128), 0);
    }


}
