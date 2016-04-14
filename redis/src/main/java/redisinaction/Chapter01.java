package redisinaction;

import org.xman.nosql.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

import java.util.*;

public class Chapter01 {
    private static final int ONE_WEEK_IN_SECONDS = 7 * 86400;
    private static final int VOTE_SCORE = 432;
    private static final int ARTICLES_PER_PAGE = 25;

    public static final void main(String[] args) {
        new Chapter01().run();
    }

    public void run() {
        Jedis jedis = RedisUtil.getRedisClient();
        jedis.select(15);
        jedis.flushDB();

        // 发表文章1
        String articleId = postArticle(jedis,
                "user1",
                "A title",
                "http://www.google.com");

        System.out.println("We posted a new article with id: " + articleId);
        System.out.println("Its HASH looks like:");
        Map<String, String> articleData = jedis.hgetAll("article:" + articleId);
        for (Map.Entry<String, String> entry : articleData.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println();

        // 发表文章2
        postArticle(jedis,
                "user2",
                "user2 title",
                "http://www.google2.com");

        // 发表文章3
        postArticle(jedis,
                "user3",
                "user3 title",
                "http://www.google3.com");

        // 给文章1投票
        articleVote(jedis, "other_user", "article:" + articleId);
        String votes = jedis.hget("article:" + articleId, "votes");
        System.out.println("We voted for the article, it now has votes: " + votes);
        assert Integer.parseInt(votes) > 1;

        // 根据分数排序输出
        System.out.println("The currently highest-scoring articles are:");
        List<Map<String, String>> articles = getArticles(jedis, 1);
        printArticles(articles);
        assert articles.size() >= 1;

        // 将文章1加入新组
        addGroups(jedis, articleId, new String[]{"new-group"});
        System.out.println("We added the article to a new group, other articles include:");
        articles = getGroupArticles(jedis, "new-group", 1);
        printArticles(articles);
        assert articles.size() >= 1;
    }



    /**
     * 发表文章。
     *
     * @param jedis 客户端连接
     * @param user  发表的用户
     * @param title 发表的标题
     * @param link  文章的链接
     * @return 文章的ID
     */
    public String postArticle(Jedis jedis, String user, String title, String link) {
        // 生成一个新的文章ID。
        String articleId = String.valueOf(jedis.incr("article:"));

        String voted = "voted:" + articleId;

        // 将发布文章的用户添加到文章的已投票用户名单里面，
        // 然后将这个名单的过期时间设置为一周。
        jedis.sadd(voted, user);
        jedis.expire(voted, ONE_WEEK_IN_SECONDS);

        long now = System.currentTimeMillis() / 1000;
        String article = "article:" + articleId;

        // 将文章信息存储到一个散列里面。
        HashMap<String, String> articleData = new HashMap<>();
        articleData.put("title", title);
        articleData.put("link", link);
        articleData.put("user", user);
        articleData.put("now", String.valueOf(now));
        articleData.put("votes", "1");
        jedis.hmset(article, articleData);

        // 将文章添加到根据发布时间排序的有序集合和根据评分排序的有序集合里面。
        jedis.zadd("score:", now + VOTE_SCORE, article);
        jedis.zadd("time:", now, article);

        return articleId;
    }

    /**
     * 给文章投票。
     *
     * @param jedis   客户端
     * @param user    用户名
     * @param article 要投票的文章，组成格式为"article:id"
     */
    public void articleVote(Jedis jedis, String user, String article) {
        // 计算文章的投票截止时间。
        long cutoff = (System.currentTimeMillis() / 1000) - ONE_WEEK_IN_SECONDS;

        // 检查是否还可以对文章进行投票
        //（虽然使用散列也可以获取文章的发布时间，
        // 但有序集合返回的文章发布时间为浮点数，
        // 可以不进行转换直接使用）。
        if (jedis.zscore("time:", article) < cutoff) {
            return;
        }

        // 从article:id标识符（identifier）里面取出文章的ID。
        String articleId = article.substring(article.indexOf(':') + 1);

        // 如果用户是第一次为这篇文章投票，那么增加这篇文章的投票数量和评分。
        // 从技术上来讲，要正确地实现投票功能，
        // 要将下面的SADD、ZINCRBY和HINCRBY三个命令放到一个事务里面执行。
        if (jedis.sadd("voted:" + articleId, user) == 1) {
            jedis.zincrby("score:", VOTE_SCORE, article);
            jedis.hincrBy(article, "votes", 1L);
        }
    }


    /**
     * 获取根据分数排名的文章列表。
     *
     * @param jedis 客户端
     * @param page  第几页
     * @return 文章列表
     */
    public List<Map<String, String>> getArticles(Jedis jedis, int page) {
        return getArticles(jedis, page, "score:");
    }

    /**
     * 获取根据某属性排序后的文章列表。
     *
     * @param jedis 客户端
     * @param page  第几页
     * @param order 排序属性
     * @return 文章列表
     */
    public List<Map<String, String>> getArticles(Jedis jedis, int page, String order) {
        // 设置获取文章的起始索引和结束索引。
        int start = (page - 1) * ARTICLES_PER_PAGE;
        int end = start + ARTICLES_PER_PAGE - 1;

        // 获取多个文章ID。
        Set<String> ids = jedis.zrevrange(order, start, end);

        // 根据文章ID获取文章的详细信息。
        List<Map<String, String>> articles = new ArrayList<>();
        for (String id : ids) {
            Map<String, String> articleData = jedis.hgetAll(id);
            articleData.put("id", id);
            articles.add(articleData);
        }

        return articles;
    }

    /**
     * 将高分文章添加到特别组中。
     *
     * @param jedis     客户端
     * @param articleId 文章ID
     * @param toAdd     待加入的组
     */
    public void addGroups(Jedis jedis, String articleId, String[] toAdd) {
        // 构建存储文章信息的键名。
        String article = "article:" + articleId;
        for (String group : toAdd) {
            // 将文章添加到它所属的群组里面。
            jedis.sadd("group:" + group, article);
        }
    }

    /**
     * 获取某组内，根据分数排序后的文章列表。
     *
     * @param jedis 客户端
     * @param group 组
     * @param page  第几页
     * @return 文章列表
     */
    public List<Map<String, String>> getGroupArticles(Jedis jedis, String group, int page) {
        return getGroupArticles(jedis, group, page, "score:");
    }

    /**
     * 获取某组内，经过排序后的文章列表。
     *
     * @param jedis 客户端
     * @param group 组
     * @param page  第几页
     * @param order 排序属性
     * @return 文章列表
     */
    public List<Map<String, String>> getGroupArticles(Jedis jedis, String group, int page, String order) {
        // 为每个群组的每种排列顺序都创建一个键。
        String key = order + group;
        // 检查是否有已缓存的排序结果，如果没有的话就现在进行排序。
        if (!jedis.exists(key)) {
            // 根据评分或者发布时间，对群组文章进行排序。
            ZParams params = new ZParams().aggregate(ZParams.Aggregate.MAX);
            jedis.zinterstore(key, params, "group:" + group, order);

            // 让Redis在60秒钟之后自动删除这个有序集合。
            jedis.expire(key, 60);
        }

        // 调用之前定义的get_articles()函数来进行分页并获取文章数据。
        return getArticles(jedis, page, key);
    }

    private void printArticles(List<Map<String, String>> articles) {
        for (Map<String, String> article : articles) {
            System.out.println("  id: " + article.get("id"));
            for (Map.Entry<String, String> entry : article.entrySet()) {
                if (entry.getKey().equals("id")) {
                    continue;
                }
                System.out.println("    " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }
}
