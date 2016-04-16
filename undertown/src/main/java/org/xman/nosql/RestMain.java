package org.xman.nosql;


import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.xman.nosql.servlet.HelloServlet;

import javax.servlet.ServletException;
import java.io.File;

import static io.undertow.servlet.Servlets.*;

public class RestMain {
    public static final String MYAPP = "/";

    public static void main(String[] args) throws ServletException {
        DeploymentInfo servletBuilder = deployment()
                .setClassLoader(RestMain.class.getClassLoader())
                .setContextPath(MYAPP)
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
        PathHandler path = Handlers.path(Handlers.redirect(MYAPP))
                .addPrefixPath(MYAPP, servletHandler);
        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path)
                .build();
        server.start();
    }

}
