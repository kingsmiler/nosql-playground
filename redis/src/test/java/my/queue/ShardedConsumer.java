package my.queue;

/**
 * 消费者。
 */
public class ShardedConsumer {
    /**
     * 主题
     */
    private Topic topic;

    public ShardedConsumer(Topic topic) {
        this.topic = topic;
    }
}
