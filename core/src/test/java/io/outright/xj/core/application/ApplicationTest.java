// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application;

import io.outright.xj.core.application.server.HttpServerProvider;
import io.outright.xj.core.application.server.LogFilterProvider;
import io.outright.xj.core.application.server.ResourceConfigProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.filter.LoggingFilter;
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

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest extends Mockito {
  @Mock private ResourceConfigProvider resourceConfigProvider;
  @Mock private ResourceConfig resourceConfig;
  @Mock private HttpServerProvider httpServerProvider;
  @Mock private HttpServer httpServer;
  @Mock private LogFilterProvider logFilterProvider;
  @Mock private LoggingFilter loggingFilter;
  private Injector injector;

  @Test
  public void Start() throws IOException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);
    when(resourceConfigProvider.get())
      .thenReturn(resourceConfig);

    Application app = injector.getInstance(Application.class);
    app.Start();

    verify(httpServerProvider).setup(URI.create("http://0.0.0.0:80/"), resourceConfig);
    verify(httpServer).start();
  }

  @Test(expected = IOException.class)
  public void Start_Failed() throws IOException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);
    doThrow(new IOException("testing: application start failed"))
      .when(httpServer).start();
    when(resourceConfigProvider.get())
      .thenReturn(resourceConfig);

    Application app = injector.getInstance(Application.class);
    app.Start();

    verify(httpServerProvider).setup(URI.create("http://0.0.0.0:80/"), resourceConfig);
    verify(httpServer).start();
  }

  @Test
  public void Configure_AccessLogEntitiesMaxSize() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.maxsize", "5");
    when(resourceConfigProvider.get())
      .thenReturn(resourceConfig);
    when(logFilterProvider.get())
      .thenReturn(loggingFilter);

    Application app = injector.getInstance(Application.class);
    app.Configure("one", "two", "three");

    verify(resourceConfig).register(loggingFilter);
    verify(resourceConfigProvider).get();
    verify(logFilterProvider).setup("/tmp/access.log", 5);
    verify(resourceConfigProvider).setup("io.outright.xj.core.application.resource", "one", "two", "three");
  }

  @Test
  public void Configure_EmptyPackages() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.maxsize", "5");
    when(resourceConfigProvider.get())
      .thenReturn(resourceConfig);
    when(logFilterProvider.get())
      .thenReturn(loggingFilter);

    Application app = injector.getInstance(Application.class);
    app.Configure();

    verify(resourceConfig).register(loggingFilter);
    verify(resourceConfigProvider).get();
    verify(logFilterProvider).setup("/tmp/access.log", 5);
    verify(resourceConfigProvider).setup("io.outright.xj.core.application.resource");
  }

  @Test
  public void Configure_AccessLogEntitiesAll() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.all", "true");
    when(resourceConfigProvider.get())
      .thenReturn(resourceConfig);
    when(logFilterProvider.get())
      .thenReturn(loggingFilter);

    Application app = injector.getInstance(Application.class);
    app.Configure("one", "two", "three");

    verify(resourceConfig).register(loggingFilter);
    verify(resourceConfigProvider).get();
    verify(logFilterProvider).setup("/tmp/access.log", true);
    verify(resourceConfigProvider).setup("io.outright.xj.core.application.resource", "one", "two", "three");
  }

  @Test
  public void Configure_ThrowsErrorOpeningAccessLog() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.maxsize", "5");
    doThrow(new IOException("testing: application configuration fails to open access log for writing"))
      .when(logFilterProvider).setup("/tmp/access.log", 5);

    Application app = injector.getInstance(Application.class);
    app.Configure("one", "two", "three");

    verify(logFilterProvider).setup("/tmp/access.log", 5);
    verify(resourceConfigProvider).setup("io.outright.xj.core.application.resource", "one", "two", "three");
  }

  @Test
  public void Stop() throws IOException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);

    Application app = injector.getInstance(Application.class);
    app.Stop();

    verify(httpServer).shutdownNow();
  }

  @Test
  public void BaseURI_Default() {
    Application app = injector.getInstance(Application.class);
    assert app.BaseURI().equals("http://0.0.0.0:80/");
  }

  @Test
  public void BaseURI_CustomHost() {
    System.setProperty("app.host","special");
    Application app = injector.getInstance(Application.class);
    assert app.BaseURI().equals("http://special:80/");
  }

  @Test
  public void BaseURI_CustomPort() {
    System.setProperty("app.port","7000");
    Application app = injector.getInstance(Application.class);
    assert app.BaseURI().equals("http://0.0.0.0:7000/");
  }

  @Before
  public void setup() throws Exception {
    createInjector();
  }

  @After
  public void cleanup() {
    System.clearProperty("app.port");
    System.clearProperty("app.host");
    System.clearProperty("log.access.entities.all");
    System.clearProperty("log.access.entities.maxsize");
    System.clearProperty("log.access.filename");
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new ApplicationModule()).with(
        new AbstractModule() {
          @Override
          public void configure() {
            bind(Application.class).to(ApplicationImpl.class);
            bind(HttpServerProvider.class).toInstance(httpServerProvider);
            bind(ResourceConfigProvider.class).toInstance(resourceConfigProvider);
            bind(LogFilterProvider.class).toInstance(logFilterProvider);
          }
        }));
  }
}
