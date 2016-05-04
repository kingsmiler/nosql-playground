package my.queue;

import redis.clients.jedis.ShardedJedis;

/**
 * 主题的切片生产者。
 */
public class ShardedProducer {
    /**
     * 分片客户端
     */
    private ShardedJedis jedis;
    /**
     * 主题
     */
    private Topic topic;

    public ShardedProducer(ShardedJedis jedis, Topic topic) {
        this.jedis = jedis;
        this.topic = topic;

        jedis.lpush(topic.getKey());
        jedis.lpush(topic.getPopKey());
    }

    /**
     * 发布主题
     *
     * @param items 主题元素
     * @return 操作标记
     */
    public Long publish(String... items) {
        return jedis.lpush(topic.getKey(), items);
    }
}
