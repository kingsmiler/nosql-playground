package redisinaction;

import com.google.gson.Gson;
import org.xman.nosql.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Chapter02 {
    public static final void main(String[] args) throws InterruptedException {
        new Chapter02().run();
    }

    public void run() throws InterruptedException {
        Jedis conn = RedisUtil.getRedisClient();
        conn.select(15);

        testLoginCookies(conn);
        testShoppingCartCookies(conn);
        testCacheRows(conn);
        testCacheRequest(conn);
    }

    public void testLoginCookies(Jedis conn) throws InterruptedException {
        System.out.println("\n----- testLoginCookies -----");
        String token = UUID.randomUUID().toString();

        updateToken(conn, token, "username", "itemX");
        System.out.println("We just logged-in/updated token: " + token);
        System.out.println("For user: 'username'");
        System.out.println();

        System.out.println("What username do we get when we look-up that token?");
        String r = checkToken(conn, token);
        System.out.println(r);
        System.out.println();
        assert r != null;

        System.out.println("Let's drop the maximum number of cookies to 0 to clean them out");
        System.out.println("We will start a thread to do the cleaning, while we stop it later");

        CleanSessionsThread thread = new CleanSessionsThread(0);
        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()) {
            throw new RuntimeException("The clean sessions thread is still alive?!?");
        }

        long s = conn.hlen("login:");
        System.out.println("The current number of sessions still available is: " + s);
        assert s == 0;
    }

    public void testShoppingCartCookies(Jedis conn)
            throws InterruptedException {
        System.out.println("\n----- testShoppingCartCookies -----");
        String token = UUID.randomUUID().toString();

        System.out.println("We'll refresh our session...");
        updateToken(conn, token, "username", "itemX");
        System.out.println("And add an item to the shopping cart");
        addToCart(conn, token, "itemY", 3);
        Map<String, String> r = conn.hgetAll("cart:" + token);
        System.out.println("Our shopping cart currently has:");
        for (Map.Entry<String, String> entry : r.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();

        assert r.size() >= 1;

        System.out.println("Let's clean out our sessions and carts");
        CleanFullSessionsThread thread = new CleanFullSessionsThread(0);
        thread.start();
        Thread.sleep(1000);
        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()) {
            throw new RuntimeException("The clean sessions thread is still alive?!?");
        }

        r = conn.hgetAll("cart:" + token);
        System.out.println("Our shopping cart now contains:");
        for (Map.Entry<String, String> entry : r.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        assert r.size() == 0;
    }

    public void testCacheRows(Jedis conn)
            throws InterruptedException {
        System.out.println("\n----- testCacheRows -----");
        System.out.println("First, let's schedule caching of itemX every 5 seconds");
        scheduleRowCache(conn, "itemX", 5);
        System.out.println("Our schedule looks like:");
        Set<Tuple> s = conn.zrangeWithScores("schedule:", 0, -1);
        for (Tuple tuple : s) {
            System.out.println("  " + tuple.getElement() + ", " + tuple.getScore());
        }
        assert s.size() != 0;

        System.out.println("We'll start a caching thread that will cache the data...");

        CacheRowsThread thread = new CacheRowsThread();
        thread.start();

        Thread.sleep(1000);
        System.out.println("Our cached data looks like:");
        String r = conn.get("inv:itemX");
        System.out.println(r);
        assert r != null;
        System.out.println();

        System.out.println("We'll check again in 5 seconds...");
        Thread.sleep(5000);
        System.out.println("Notice that the data has changed...");
        String r2 = conn.get("inv:itemX");
        System.out.println(r2);
        System.out.println();
        assert r2 != null;
        assert !r.equals(r2);

        System.out.println("Let's force un-caching");
        scheduleRowCache(conn, "itemX", -1);
        Thread.sleep(1000);
        r = conn.get("inv:itemX");
        System.out.println("The cache was cleared? " + (r == null));
        assert r == null;

        thread.quit();
        Thread.sleep(2000);
        if (thread.isAlive()) {
            throw new RuntimeException("The database caching thread is still alive?!?");
        }
    }

    public void testCacheRequest(Jedis conn) {
        System.out.println("\n----- testCacheRequest -----");
        String token = UUID.randomUUID().toString();

        Callback callback = new Callback() {
            public String call(String request) {
                return "content for " + request;
            }
        };

        updateToken(conn, token, "username", "itemX");
        String url = "http://test.com/?item=itemX";
        System.out.println("We are going to cache a simple request against " + url);
        String result = cacheRequest(conn, url, callback);
        System.out.println("We got initial content:\n" + result);
        System.out.println();

        assert result != null;

        System.out.println("To test that we've cached the request, we'll pass a bad callback");
        String result2 = cacheRequest(conn, url, null);
        System.out.println("We ended up getting the same response!\n" + result2);

        assert result.equals(result2);

        assert !canCache(conn, "http://test.com/");
        assert !canCache(conn, "http://test.com/?item=itemX&_=1234536");
    }

    public String checkToken(Jedis conn, String token) {
        return conn.hget("login:", token);
    }

    public void updateToken(Jedis conn, String token, String user, String item) {
        // 获取当前时间戳。
        long timestamp = System.currentTimeMillis() / 1000;
        // 维持令牌与已登录用户之间的映射。
        conn.hset("login:", token, user);
        // 记录令牌最后一次出现的时间。
        conn.zadd("recent:", timestamp, token);
        if (item != null) {
            // 记录用户浏览过的商品。
            conn.zadd("viewed:" + token, timestamp, item);
            // 移除旧的记录，只保留用户最近浏览过的25个商品。
            conn.zremrangeByRank("viewed:" + token, 0, -26);
            // 向上提升查看的商品的排位
            conn.zincrby("viewed:", -1, item);
        }
    }

    public void addToCart(Jedis conn, String session, String item, int count) {
        if (count <= 0) {
            // 从购物车里面移除指定的商品。
            conn.hdel("cart:" + session, item);
        } else {
            // 将指定的商品添加到购物车。
            conn.hset("cart:" + session, item, String.valueOf(count));
        }
    }

    public void scheduleRowCache(Jedis conn, String rowId, int delay) {
        // 先设置数据行的延迟值。
        conn.zadd("delay:", delay, rowId);

        // 立即缓存数据行。
        conn.zadd("schedule:", System.currentTimeMillis() / 1000, rowId);
    }

    public String cacheRequest(Jedis conn, String request, Callback callback) {
        // 对于不能被缓存的请求，直接调用回调函数。
        if (!canCache(conn, request)) {
            return callback != null ? callback.call(request) : null;
        }

        // 将请求转换成一个简单的字符串键，方便之后进行查找。
        String pageKey = "cache:" + hashRequest(request);
        // 尝试查找被缓存的页面。
        String content = conn.get(pageKey);

        if (content == null && callback != null) {
            // 如果页面还没有被缓存，那么生成页面。
            content = callback.call(request);

            // 将新生成的页面放到缓存里面。
            conn.setex(pageKey, 300, content);
        }

        // 返回页面。
        return content;
    }

    public boolean canCache(Jedis conn, String request) {
        try {
            URL url = new URL(request);
            HashMap<String, String> params = new HashMap<String, String>();
            if (url.getQuery() != null) {
                for (String param : url.getQuery().split("&")) {
                    String[] pair = param.split("=", 2);
                    params.put(pair[0], pair.length == 2 ? pair[1] : null);
                }
            }

            String itemId = extractItemId(params);
            if (itemId == null || isDynamic(params)) {
                return false;
            }
            Long rank = conn.zrank("viewed:", itemId);
            return rank != null && rank < 10000;
        } catch (MalformedURLException mue) {
            return false;
        }
    }

    public boolean isDynamic(Map<String, String> params) {
        return params.containsKey("_");
    }

    public String extractItemId(Map<String, String> params) {
        return params.get("item");
    }

    public String hashRequest(String request) {
        return String.valueOf(request.hashCode());
    }

    public interface Callback {
        public String call(String request);
    }

    public static class Inventory {
        private String id;
        private String data;
        private long time;

        private Inventory(String id) {
            this.id = id;
            this.data = "data to cache...";
            this.time = System.currentTimeMillis() / 1000;
        }

        public static Inventory get(String id) {
            return new Inventory(id);
        }
    }

    public class CleanSessionsThread
            extends Thread {
        private Jedis conn;
        private int limit;
        private boolean quit;

        public CleanSessionsThread(int limit) {
            this.conn = RedisUtil.getRedisClient();
            this.conn.select(15);
            this.limit = limit;
        }

        public void quit() {
            quit = true;
        }

        public void run() {
            while (!quit) {
                // 找出目前已有令牌的数量。
                long size = conn.zcard("recent:");

                // 令牌数量未超过限制，休眠并在之后重新检查。
                if (size <= limit) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                // 获取需要移除的令牌ID。
                long endIndex = Math.min(size - limit, 100);
                Set<String> tokenSet = conn.zrange("recent:", 0, endIndex - 1);
                String[] tokens = tokenSet.toArray(new String[tokenSet.size()]);

                // 为那些将要被删除的令牌构建键名。
                ArrayList<String> sessionKeys = new ArrayList<>();
                for (String token : tokens) {
                    sessionKeys.add("viewed:" + token);
                }

                // 移除最旧的那些令牌。
                conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
                conn.hdel("login:", tokens);
                conn.zrem("recent:", tokens);
            }
        }
    }

    public class CleanFullSessionsThread
            extends Thread {
        private Jedis conn;
        private int limit;
        private boolean quit;

        public CleanFullSessionsThread(int limit) {
            this.conn = RedisUtil.getRedisClient();
            this.conn.select(15);
            this.limit = limit;
        }

        public void quit() {
            quit = true;
        }

        public void run() {
            while (!quit) {
                long size = conn.zcard("recent:");
                if (size <= limit) {
                    try {
                        sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                long endIndex = Math.min(size - limit, 100);
                Set<String> sessionSet = conn.zrange("recent:", 0, endIndex - 1);
                String[] sessions = sessionSet.toArray(new String[sessionSet.size()]);

                ArrayList<String> sessionKeys = new ArrayList<String>();
                for (String sess : sessions) {
                    sessionKeys.add("viewed:" + sess);

                    // 新增加的这行代码用于删除旧会话对应用户的购物车。
                    sessionKeys.add("cart:" + sess);
                }

                conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
                conn.hdel("login:", sessions);
                conn.zrem("recent:", sessions);
            }
        }
    }

    public class CacheRowsThread extends Thread {
        private Jedis conn;
        private boolean quit;

        public CacheRowsThread() {
            this.conn = RedisUtil.getRedisClient();
            this.conn.select(15);
        }

        public void quit() {
            quit = true;
        }

        public void run() {
            Gson gson = new Gson();
            while (!quit) {
                // 尝试获取下一个需要被缓存的数据行以及该行的调度时间戳，
                // 命令会返回一个包含零个或一个元组（tuple）的列表。
                Set<Tuple> range = conn.zrangeWithScores("schedule:", 0, 0);

                Tuple next = range.size() > 0 ? range.iterator().next() : null;
                long now = System.currentTimeMillis() / 1000;
                if (next == null || next.getScore() > now) {
                    try {
                        // 暂时没有行需要被缓存，休眠50毫秒后重试。
                        sleep(50);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                String rowId = next.getElement();

                // 获取下一次调度前的延迟时间。
                double delay = conn.zscore("delay:", rowId);
                if (delay <= 0) {
                    // 不必再缓存这个行，将它从缓存中移除。
                    conn.zrem("delay:", rowId);
                    conn.zrem("schedule:", rowId);
                    conn.del("inv:" + rowId);
                    continue;
                }

                //读取数据行。
                Inventory row = Inventory.get(rowId);

                // 新调度时间并设置缓存值。
                conn.zadd("schedule:", now + delay, rowId);
                conn.set("inv:" + rowId, gson.toJson(row));
            }
        }
    }
}
