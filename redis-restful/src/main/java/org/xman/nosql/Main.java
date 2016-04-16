package org.xman.nosql;


import io.undertow.Undertow;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.xman.nosql.util.SslUtil;

import javax.net.ssl.SSLContext;

public class Main {
    private static UndertowJaxrsServer server;

    public static void main(String[] args) throws Exception {
        String host = "0.0.0.0";
        int port = 8443;

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(SslUtil.getKeyManagers(), null, null);

        Undertow.Builder builder = Undertow.builder().addHttpsListener(port, host, sslContext);

        server = new UndertowJaxrsServer().start(builder);

        server.deploy(RestApplication.class, "/api");
    }

}
