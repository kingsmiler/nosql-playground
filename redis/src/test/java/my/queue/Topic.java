package my.queue;

public class Topic {
    /**
     * 名称
     */
    private String name;
    /**
     * REDIS 主键
     */
    private String key;
    /**
     * REDIS 弹出的列表的主键
     */
    private String popKey;

    Topic(String name) {
        this.name = name;

        this.name = name;
        this.key = "redis:topic:" + name;
        this.popKey = key + ":trash";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPopKey() {
        return popKey;
    }

    public void setPopKey(String popKey) {
        this.popKey = popKey;
    }
}
