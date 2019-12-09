// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import com.google.inject.Injector;
import io.xj.core.access.AccessLogFilterProvider;
import io.xj.core.access.AccessTokenAuthFilter;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

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
    packages(toStringArray(packages.toArray()));

    register(new ContainerLifecycleListener() {
      public void onStartup(Container container) {
        container.getApplicationHandler().getInjectionManager().register(new AbstractBinder() {
          @Override
          protected void configure() {
            bind(injector).to(Injector.class);
          }
        });
      }

      public void onReload(Container container) {
        // noop
      }

      public void onShutdown(Container container) {
        // noop
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
