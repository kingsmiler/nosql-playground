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
     * REDIS 弹出主键
     */
    private String popKey;

    public Topic(String name, String key, String popKey) {
        this.name = name;
        this.key = key;
        this.popKey = popKey;

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
