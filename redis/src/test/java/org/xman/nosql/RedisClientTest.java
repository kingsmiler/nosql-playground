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
        client.keyOperate();
    }

    @Test
    public void testStringOperate() {
        RedisClient client = new RedisClient();
        client.stringOperate();
    }

    @Test
    public void testListOperate() {
        RedisClient client = new RedisClient();
        client.listOperate();
    }

    @Test
    public void testSetOperate() {
        RedisClient client = new RedisClient();
        client.setOperate();
    }

    @Test
    public void testSortedSetOperate() {
        RedisClient client = new RedisClient();
        client.sortedSetOperate();
    }

    @Test
    public void testHashOperate() {
        RedisClient client = new RedisClient();
        client.hashOperate();
    }

}
