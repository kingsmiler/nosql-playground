package org.xman.nosql;


import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.xman.nosql.util.SslUtil;

import javax.net.ssl.SSLContext;

import static io.undertow.servlet.Servlets.*;

public class RestMain {

    public static void main(String[] args) throws Exception {
        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(RestMain.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("test")
                .addServlets(
                        servlet("Resteasy", HttpServletDispatcher.class)
                                .addMapping("/*")
                                .setLoadOnStartup(100)
                                .addInitParam("javax.ws.rs.Application", "org.xman.nosql.RestApplication")
                );

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        HttpHandler servletHandler = manager.start();
        PathHandler path = Handlers.path(Handlers.redirect("/"))
                .addPrefixPath("/", servletHandler);

        String host = "0.0.0.0";
        int port = 8443;

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(SslUtil.getKeyManagers(), null, null);

        Undertow server = Undertow.builder()
                .setHandler(path)
                .addHttpsListener(port, host, sslContext)
                .build();
        server.start();
    }
}
