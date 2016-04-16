package org.xman.nosql.undertown;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.xman.nosql.util.SslUtil;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

public class HelloWorldHttpsServer {

    public static void main(String[] args) throws Exception {

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(SslUtil.getKeyManagers(), null, null);

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



}