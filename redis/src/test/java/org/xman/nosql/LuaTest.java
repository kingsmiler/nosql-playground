package org.xman.nosql;


import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class LuaTest {
    private static Jedis jedis;

    @BeforeClass
    public static void init() {
        jedis = RedisUtil.getRedisClient();
        jedis.select(8);
        jedis.flushDB();
    }

    @Test
    public void helloLua() {
        String lua =
                "local msg = \"Hello, world!\"\n" +
                        "return msg";

        Object message = jedis.eval(lua);

        System.out.println(message);
    }

    @Test
    public void testSimpleSetLuaGet() {
        jedis.set("n1", "name1");
        jedis.set("n2", "name2");
        jedis.set("n3", "name3");

        Set<String> keys = jedis.keys("*");
        keys.forEach(System.out::println);

        String lua =
                "local msg = redis.call(\"GET\", \"n1\")\n" +
                        "return msg";

        Object message = jedis.eval(lua);

        System.out.println(message);
        System.out.println();
    }

}
