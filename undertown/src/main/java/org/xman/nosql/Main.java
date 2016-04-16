package org.xman.nosql;


import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.xman.nosql.servlet.HelloServlet;

import javax.servlet.ServletException;
import java.io.File;

import static io.undertow.servlet.Servlets.*;

public class Main {
    public static final String MYAPP = "/";

    public static void main(String[] args) throws ServletException {
        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(Main.class.getClassLoader())
                .setContextPath(MYAPP)
                .setResourceManager(new FileResourceManager(new File("undertown/src/main/webapp"), 1024))
                .setDeploymentName("test")
                .addServlets(
                        servlet("HelloServlet", HelloServlet.class)
                                .addMapping("/hello")
                                .setLoadOnStartup(100)
                );

        DeploymentManager manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();

        HttpHandler servletHandler = manager.start();
        PathHandler path = Handlers.path(Handlers.redirect(MYAPP))
                .addPrefixPath(MYAPP, servletHandler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path)
                .build();
        server.start();
    }

}
