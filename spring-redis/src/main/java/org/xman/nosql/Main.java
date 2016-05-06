package org.xman.nosql;


import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:beans.xml");

        Consumer c1 = context.getBean("consumer", Consumer.class);
        c1.consume();

        Consumer c2 = context.getBean("consumer", Consumer.class);
        c2.consume();

        Consumer c3 = context.getBean("consumer", Consumer.class);
        c3.consume();
    }
}
