package org.xman.nosql;


import io.undertow.Undertow;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.xman.nosql.util.SslUtil;

import javax.net.ssl.SSLContext;

public class Main {

    public static void main(String[] args) throws Exception {

        String host = System.getProperty("rcs.host", "0.0.0.0");
        String port = System.getProperty("rcs.port", "8443");

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(SslUtil.getKeyManagers(), null, null);

        UndertowJaxrsServer server = new UndertowJaxrsServer().start(
                Undertow.builder().addHttpsListener(
                        Integer.parseInt(port),
                        host,
                        sslContext
                )
        );

        server.deploy(RestApplication.class, "/");
    }

}
