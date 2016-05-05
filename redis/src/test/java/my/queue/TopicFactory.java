package my.queue;


import org.xman.nosql.RedisUtil;
import redis.clients.jedis.ShardedJedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 主题工厂，所有的主题，生产者，消费者均从这里获取。
 */
public class TopicFactory {
    private static final HashMap<String, Topic> TOPIC_MAP = new HashMap<>();
    private static final HashMap<Topic, List<Consumer>> CONSUMER_MAP = new HashMap<>();
    private static final HashMap<Topic, List<Producer>> PRODUCER_MAP = new HashMap<>();
    private static final ShardedJedis REDIS = RedisUtil.getShardedRedisClient();

    private static Lock lock = new ReentrantLock();

    /**
     * 根据主题名称获取主题，如果是初次创建主题，则同时还会为该主题创建一个生产者和一个消费者。
     *
     * @param topicName 主题名称
     * @return 主题
     */
    public static Topic getTopic(String topicName) {
        Topic topic = TOPIC_MAP.get(topicName);

        if (topic == null) {
            lock.lock();
            try {
                topic = TOPIC_MAP.get(topicName);
                if (topic == null) {
                    topic = new Topic(topicName);
                }

                // 添加主题
                TOPIC_MAP.put(topicName, topic);

                // 默认添加一个生产者
                List<Producer> producers = new ArrayList<>();
                producers.add(new Producer(REDIS, topic));
                PRODUCER_MAP.put(topic, producers);

                // 默认添加一个消费者
                List<Consumer> consumers = new ArrayList<>();
                consumers.add(new Consumer(REDIS, topic));
                CONSUMER_MAP.put(topic, consumers);
            } catch (Exception ignored) {
                // ignored
            } finally {
                lock.unlock();
            }
        }

        return topic;
    }

    /**
     * 获取当前所有的主题。
     *
     * @return 所有主题
     */
    public static List<Topic> getToppics() {

        return (List<Topic>) TOPIC_MAP.values();
    }

    /**
     * 根据主题名称新创建一个消费者。
     *
     * @param topicName 主题名称
     * @return 消费者
     */
    public static void newConsumer(String topicName) {
        Topic topic = TOPIC_MAP.get(topicName);
        if(topic == null) {
            getTopic(topicName);
        } else {
            List<Consumer> consumers = CONSUMER_MAP.get(topic);
            consumers.add(new Consumer(REDIS, topic));
        }
    }

    public static void startConsume(String topicName, ConsumerCallback callback) {

    }
}
