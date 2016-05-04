package redis.rmq;

public interface Callback {
    void onMessage(String message);
}
