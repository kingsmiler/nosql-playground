package my.queue;

import redis.clients.jedis.ShardedJedis;

/**
 * 消费者。
 */
public class Consumer {
    /**
     * 分片客户端
     */
    private ShardedJedis jedis;
    /**
     * 主题
     */
    private Topic topic;

    private boolean recycled = true;

    Consumer(ShardedJedis jedis, Topic topic) {
        this.jedis = jedis;
        this.topic = topic;
    }

    public void consume(ConsumerCallback callback) {
        Thread thread = new Thread(() -> {
            while (recycled) {

            }
        });
    }
}
