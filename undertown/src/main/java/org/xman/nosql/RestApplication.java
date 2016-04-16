package org.xman.nosql;


import org.xman.nosql.api.HelloResource;

import java.util.HashSet;
import java.util.Set;

public class RestApplication extends javax.ws.rs.core.Application {

    @Override
    public Set<Class<?>> getClasses() {

        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(HelloResource.class);
        return classes;
    }
}
