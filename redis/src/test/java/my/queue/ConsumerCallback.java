package my.queue;

/**
 * 回调函数，当有数据的时候，将数据传递给具体业务。
 */
public interface ConsumerCallback {
    void onMessage(String message);
}
