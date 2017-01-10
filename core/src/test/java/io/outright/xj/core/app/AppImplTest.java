// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app;

import io.outright.xj.core.app.access.AccessTokenAuthFilter;
import io.outright.xj.core.app.access.AccessLogFilterProvider;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.server.HttpServerProvider;
import io.outright.xj.core.app.server.ResourceConfigProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AppImplTest extends Mockito {
  @Mock private ResourceConfigProvider resourceConfigProvider;
  @Mock private ResourceConfig resourceConfig;
  @Mock private HttpServerProvider httpServerProvider;
  @Mock private HttpServer httpServer;
  @Mock private AccessLogFilterProvider accessLogFilterProvider;
  @Mock private AccessTokenAuthFilter accessTokenAuthFilter;
  private Injector injector;
  private App app;

  @Before
  public void setup() throws Exception {
    createInjector();
    app = injector.getInstance(App.class);
  }

  @After
  public void cleanup() {
    app = null;
    System.clearProperty("app.port");
    System.clearProperty("app.host");
    System.clearProperty("log.access.entities.all");
    System.clearProperty("log.access.entities.maxsize");
    System.clearProperty("log.access.filename");
  }

  @Test
  public void ConfigureAndStart() throws IOException, ConfigException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);
    when(resourceConfigProvider.get("io.outright.xj.core.app.resource"))
      .thenReturn(resourceConfig);

    app.configure();
    app.start();

    verify(httpServerProvider).configure(URI.create("http://0.0.0.0:80/"), resourceConfig);
    verify(httpServer).start();
  }

  @Test(expected = ConfigException.class)
  public void Start_FailureToConfigureFirst() throws IOException, ConfigException {
    app.start();
  }

  @Test(expected = IOException.class)
  public void Start_IOFailure() throws IOException, ConfigException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);
    doThrow(new IOException("this is a test of application start failed"))
      .when(httpServer).start();
    when(resourceConfigProvider.get("io.outright.xj.core.app.resource"))
      .thenReturn(resourceConfig);

    app.configure();
    app.start();

    verify(httpServerProvider).configure(URI.create("http://0.0.0.0:80/"), resourceConfig);
    verify(httpServer).start();
  }

  @Test
  public void Configure_EmptyPackages() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.maxsize", "5");
    when(resourceConfigProvider.get("io.outright.xj.core.app.resource"))
      .thenReturn(resourceConfig);

    app.configure();

    verify(accessLogFilterProvider).registerTo(resourceConfig);
    verify(resourceConfig).register(accessTokenAuthFilter);
    verify(resourceConfigProvider).get("io.outright.xj.core.app.resource");
  }

  @Test
  public void Configure_AccessLogEntitiesMaxSize() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.maxsize", "5");
    when(resourceConfigProvider.get("io.outright.xj.core.app.resource", "one", "two", "three"))
      .thenReturn(resourceConfig);

    app.configure("one", "two", "three");

    verify(accessLogFilterProvider).registerTo(resourceConfig);
    verify(resourceConfig).register(accessTokenAuthFilter);
    verify(resourceConfigProvider).get("io.outright.xj.core.app.resource", "one", "two", "three");
  }

  @Test
  public void Configure_AccessLogEntitiesAll() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.all", "true");
    when(resourceConfigProvider.get("io.outright.xj.core.app.resource", "one", "two", "three"))
      .thenReturn(resourceConfig);

    app.configure("one", "two", "three");

    verify(accessLogFilterProvider).registerTo(resourceConfig);
    verify(resourceConfig).register(accessTokenAuthFilter);
    verify(resourceConfigProvider).get("io.outright.xj.core.app.resource", "one", "two", "three");
  }

  @Test
  public void Stop() throws IOException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);

    app.stop();

    verify(httpServer).shutdownNow();
  }

  @Test
  public void BaseURI_Default() {
    assertEquals("http://0.0.0.0:80/", app.baseURI());
  }

  @Test
  public void BaseURI_CustomHost() {
    System.setProperty("app.host","special");
    app = injector.getInstance(App.class);
    assertEquals("http://special:80/", app.baseURI());
  }

  @Test
  public void BaseURI_CustomPort() {
    System.setProperty("app.port","7000");
    app = injector.getInstance(App.class);
    assertEquals("http://0.0.0.0:7000/", app.baseURI());
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
        new AbstractModule() {
          @Override
          public void configure() {
            bind(App.class).to(AppImpl.class);
            bind(AccessTokenAuthFilter.class).toInstance(accessTokenAuthFilter);
            bind(HttpServerProvider.class).toInstance(httpServerProvider);
            bind(ResourceConfigProvider.class).toInstance(resourceConfigProvider);
            bind(AccessLogFilterProvider.class).toInstance(accessLogFilterProvider);
          }
        }));
  }
}
