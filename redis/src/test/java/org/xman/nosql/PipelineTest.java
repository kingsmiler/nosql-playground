package org.xman.nosql;


import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.IOException;
import java.util.Set;

public class PipelineTest {
    private static Jedis jedis;
    static String key = "pub_tl_u1234";

    @BeforeClass
    public static void init() {
        jedis = RedisUtil.getRedisClient();
        jedis.select(8);
        jedis.flushDB();

        for(int i=0; i<100; i++) {
            jedis.zadd(key, i, "mid" + i);
        }
    }

    @Test
    public void helloPipeline() throws IOException {
        String key = "pipe";
        Pipeline pipeline = jedis.pipelined();
        pipeline.set(key, "1");
        for(int i=0; i<10; i++) {
            pipeline.incr(key);
        }

        pipeline.sync();
        pipeline.close();

//        Long index = pipeline.zrank(key, "mid80").get();
//        Set<String> sets = pipeline.zrange(key, index, index+20).get();
//
//
//        for (String mid : sets) {
//            System.out.println(mid);
//        }
//
//        System.out.println();
    }


}
