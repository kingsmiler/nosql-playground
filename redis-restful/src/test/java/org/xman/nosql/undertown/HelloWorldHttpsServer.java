package org.xman.nosql.undertown;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

public class HelloWorldHttpsServer {

    public static void main(String[] args) throws Exception {

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(getKeyManagers(), null, null);

        Undertow.builder().addHttpsListener(8443, "0.0.0.0", sslContext)
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().put(
                                Headers.CONTENT_TYPE,
                                "text/plain");

                        exchange.getResponseSender().send("Hello World");
                    }
                }).build().start();
    }

    private static KeyManager[] getKeyManagers() {
        String pass = "lib.pass";
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            keyStore.load(new FileInputStream("keystore.jks"), pass.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());

            keyManagerFactory.init(keyStore, pass.toCharArray());

            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}