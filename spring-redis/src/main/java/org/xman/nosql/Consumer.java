package org.xman.nosql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public class Consumer extends Thread {

    // inject the actual template
    @Autowired
    private RedisTemplate<String, String> template;

    public void setTemplate(RedisTemplate<String, String> template) {
        this.template = template;
    }

    private boolean running = true;

    @Override
    public void run() {
        while (running) {
            try {
                System.out.println(this.getName());
                String message = template.boundListOps("topic:moments:pub:pending").rightPop(0, TimeUnit.SECONDS);

                System.out.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void consume() {
        this.start();
    }
}