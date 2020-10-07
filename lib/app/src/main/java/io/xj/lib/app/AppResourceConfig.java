// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.inject.Injector;
import org.glassfish.jersey.server.ResourceConfig;
import org.reflections.Reflections;

import javax.ws.rs.Path;
import java.util.Set;

/**
 Jersey server resource configuration for Ocean Backend services,
 <p>
 Created by Charney Kaye on 2019/11/29
 Refactored by Charney Kaye on 2020/10/5
 */
public class AppResourceConfig extends ResourceConfig {

  /**
   Construct a new XJ Music service App ResourceConfig@param injector         Guice injector instance to bind via HK2 to Injector.class
   <p>
   Log all resources on application boot with an AppResourceEndpointLoggingListener
   */
  AppResourceConfig(Injector injector, Set<String> resourcePackages) {
    register(new AppResourceEndpointLoggingListener("/"));
    register(JacksonJsonProvider.class);
    for (String resPkg : resourcePackages)
      for (Class<?> resCls : new Reflections(resPkg).getTypesAnnotatedWith(Path.class))
        register(injector.getInstance(resCls));
  }

}
