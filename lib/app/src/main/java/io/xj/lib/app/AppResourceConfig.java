// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import com.google.inject.Injector;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Set;

/**
 Jersey server resource configuration for Ocean Backend services,
 uses HK2 to directly bind instance of Guice injector to Injector.class
 to make Guice injection available from within JAX-RS resources.
 Created by Charney Kaye on 2019/11/29
 */
public class AppResourceConfig extends ResourceConfig {

  /**
   Construct a new XJ Music service App ResourceConfig

   @param injector         Guice injector instance to bind via HK2 to Injector.class
   @param resourcePackages packages containing JAX-RS resources to add to ResourceConfig
   */
  AppResourceConfig(Injector injector, Set<String> resourcePackages) {

    // use HK2 to directly bind instance of Guice injector to Injector.class
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(injector).to(Injector.class);
      }
    });

    // Log all resources on application boot
    register(new AppResourceEndpointLoggingListener("/"));

    // add packages
    packages(toStringArray(resourcePackages));
  }


  /**
   Convert array of objects to array of strings

   @param packages to convert to strings
   @return array of strings
   */
  private static String[] toStringArray(Set<String> packages) {
    Object[] objects = packages.toArray();
    int len = objects.length;
    String[] strings = new String[len];
    for (int i = 0; i < len; i++)
      strings[i] = String.valueOf(objects[i]);
    return strings;
  }

}
