package redis.rmq.benchmarks;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xman.nosql.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.rmq.Consumer;
import redis.rmq.Producer;

import java.io.IOException;
import java.util.Calendar;

public class PublishConsumeBenchmarkTest extends Assert {
    @Before
    public void setUp() throws IOException {
        Jedis jedis = RedisUtil.getRedisClient();
        jedis.flushAll();
        jedis.disconnect();

    }

    @Test
    public void publish() {
        final String topic = "foo";
        final String message = "hello world!";
        final int MESSAGES = 10000;
        Producer p = new Producer(RedisUtil.getRedisClient(), topic);

        long start = Calendar.getInstance().getTimeInMillis();
        for (int n = 0; n < MESSAGES; n++) {
            p.publish(message);
        }
        long elapsed = Calendar.getInstance().getTimeInMillis() - start;
        System.out.println(((1000 * MESSAGES) / elapsed) + " ops");
    }

    @Test
    public void consume() {
        final String topic = "foo";
        final String message = "hello world!";
        final int MESSAGES = 10000;
        Producer p = new Producer(RedisUtil.getRedisClient(), topic);
        Consumer c = new Consumer(RedisUtil.getRedisClient(), "consumer", topic);
        for (int n = 0; n < MESSAGES; n++) {
            p.publish(message);
        }

        long start = Calendar.getInstance().getTimeInMillis();
        String m = null;
        do {
            m = c.consume();
        } while (m != null);
        long elapsed = Calendar.getInstance().getTimeInMillis() - start;

        System.out.println(((1000 * MESSAGES) / elapsed) + " ops");
    }
}