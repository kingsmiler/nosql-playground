package org.xman.nosql;


import redis.clients.jedis.*;

import java.util.ArrayList;
import java.util.List;

public final class RedisUtil {
    private static final int DEFAULT_PORT = 6379;

    /**
     * 获取非切片池。
     *
     * @param redisHost 远程主机
     * @param port      远程端口
     * @return 池对象
     */
    public static JedisPool getJedisPool(String redisHost, int port) {
        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000L);
        config.setTestOnBorrow(false);

        return new JedisPool(config, redisHost, port);
    }

    /**
     * 获取切片池。
     *
     * @param redisHost 远程主机
     * @param port      远程端口
     * @return 池对象
     */
    public static ShardedJedisPool getShardedJedisPool(String redisHost, int port) {
        // 池基本配置
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(5);
        config.setMaxWaitMillis(1000L);
        config.setTestOnBorrow(false);

        // slave链接
        List<JedisShardInfo> shards = new ArrayList<>();
        shards.add(new JedisShardInfo(redisHost, port, "master"));

        return new ShardedJedisPool(config, shards);
    }

    public static Jedis getRedisClient() {
        return getRedisClient(System.getenv("REDIS_HOST"), DEFAULT_PORT);
    }

    public static Jedis getRedisClient(String redisHost, int port) {
        return getJedisPool(redisHost, port).getResource();
    }

    /**
     * 获取切片客户端。
     *
     * @return 客户端
     */
    public static ShardedJedis getShardedRedisClient() {
        return getShardedRedisClient(System.getenv("REDIS_HOST"), DEFAULT_PORT);
    }

    /**
     * 获取切片客户端。
     *
     * @param redisHost 远程主机
     * @param port      远程端口
     * @return 客户端
     */
    public static ShardedJedis getShardedRedisClient(String redisHost, int port) {
        return getShardedJedisPool(redisHost, port).getResource();
    }

}
