package org.xman.nosql;


import org.junit.Test;

public class RedisClientTest {

    @Test
    public void testInitialPool() {
        new RedisClient();
    }

    @Test
    public void testKeyOperate() {
        RedisClient client = new RedisClient();
        client.KeyOperate();
    }
}
