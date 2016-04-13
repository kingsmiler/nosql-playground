package grl.redis.users;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class HashSample {
    public static void main(String[] args) {
        // 从环境变量中获取主机信息
        String redisHost = System.getenv("REDIS_HOST");

        Jedis redis = new Jedis(redisHost);

        Map<String, String> map = new HashMap<>();
        map.put("email", "user1@domain.com");
        map.put("userid", "12356");
        map.put("address", "1111 Main St. Houston TX, 77054");
        map.put("phone", "555-124-5544");

        // 将所有数据保存在 map 中，然后使用 hmset 方法一次插入对象数据
        redis.hmset("user.by.id." + map.get("userid"), map);

        // 以邮件地址作为索引，保存 ID 信息
        redis.set("user.id.by.email." + map.get("email"), map.get("userid"));

        System.out.println("User stored: " + map.get("userid"));

        // 使用 hgetall 方法检查是否数据插入成功
        System.out.println(
                redis.hgetAll("user.by.id." + map.get("userid"))
        );

        // 第二个对象
        map = new HashMap<>();
        map.put("email", "user2@other.domain.com");
        map.put("userid", "24567");
        map.put("address", "1111 Main St. Houston TX, 77054");
        map.put("phone", "555-124-5544");

        redis.hmset("user.by.id." + map.get("userid"), map);
        redis.set("user.id.by.email.", map.get("email"));

        System.out.println("User stored: " + map.get("userid"));

        System.out.println(
                redis.hgetAll("user.by.id." + map.get("userid"))
        );

        // 通过索引得到用户 ID， 再根据 ID 来进行查询。
        String email = "user1@domain.com";
        String userid = redis.get("user.id.by.email." + email);
        if (userid != null) {
            System.out.println(String.format(
                    "Found user by email: %s",
                    redis.hgetAll("user.by.id." + userid)
            ));
        }
    }
}
