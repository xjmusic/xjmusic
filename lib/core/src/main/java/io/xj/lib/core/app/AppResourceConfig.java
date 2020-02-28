// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.app;

import com.google.inject.Injector;
import io.xj.lib.core.access.AccessLogFilterProvider;
import io.xj.lib.core.access.AccessTokenAuthFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Collection;

/**
 Jersey server resource configuration
 */
public class AppResourceConfig extends ResourceConfig {

  /**
   New ResourceConfig with Guice injector and specifeid packages

   @param injector for Guice-bound classes
   @param packages for JAX-RS resources
   */
  AppResourceConfig(Injector injector, Collection<String> packages) {

    // Register all resource packages
    packages(toStringArray(packages.toArray()));

    // register Guice injector for injection via HK2
    register(new AbstractBinder() {
      @Override
      protected void configure() {
        bind(injector).to(Injector.class);
      }
    });

    // register jackson json provider
    register(JacksonJsonProvider.class);

    // access log only registers if file succeeds to open for writing
    injector.getInstance(AccessLogFilterProvider.class).registerTo(this);

    // access control filter
    register(injector.getInstance(AccessTokenAuthFilter.class));
  }

  /**
   Convert array of objects to array of strings

   @param objects to convert to strings
   @return array of strings
   */
  private static String[] toStringArray(Object[] objects) {
    int len = objects.length;
    String[] strings = new String[len];
    for (int i = 0; i < len; i++)
      strings[i] = String.valueOf(objects[i]);
    return strings;
  }
}
