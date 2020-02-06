// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.core.CoreModule;
import io.xj.core.testing.AppTestConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class AppTest {
  private App subject;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(9873));
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule()));
    subject = new App(ImmutableList.of(), injector);
    subject.start();
  }

  @After
  public void tearDown() {
    subject.stop();
  }

  @Test
  public void healthEndpoint() throws Exception {
    HttpClient httpClient = HttpClients.createMinimal();
    HttpGet httpRequest = new HttpGet("http://localhost:9873/o2");
    httpRequest.setConfig(RequestConfig.custom()
      .setConnectionRequestTimeout(5000)
      .setConnectTimeout(5000)
      .setSocketTimeout(5000)
      .build());
    httpRequest.setHeader("Connection", "close");

    HttpResponse response = httpClient.execute(httpRequest);

    assertEquals(204, response.getStatusLine().getStatusCode());
  }
}

/*

FUTURE app tests

  @Test
  public void ConfigureAndStart() throws IOException, CoreException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);
    when(resourceConfigProvider.get("io.xj.core.common"))
      .thenReturn(resourceConfig);

    app.configureServer();
    app.start();

    verify(httpServerProvider).configure(URI.create("http://0.0.0.0:80/"), resourceConfig);
    verify(httpServer).start();
  }

  @Test(expected = CoreException.class)
  public void Start_FailureToConfigureFirst() throws IOException, CoreException {
    app.start();
  }

  @Test(expected = IOException.class)
  public void Start_IOFailure() throws IOException, CoreException {
    when(httpServerProvider.get())
      .thenReturn(httpServer);
    doThrow(new IOException("this is a test create application start failed"))
      .when(httpServer).start();
    when(resourceConfigProvider.get("io.xj.core.common"))
      .thenReturn(resourceConfig);

    app.configureServer();
    app.start();

    verify(httpServerProvider).configure(URI.create("http://0.0.0.0:80/"), resourceConfig);
    verify(httpServer).start();
  }

  @Test
  public void Configure_EmptyPackages() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.maxsize", "5");
    when(resourceConfigProvider.get("io.xj.core.common"))
      .thenReturn(resourceConfig);

    app.configureServer();

    verify(accessLogFilterProvider).registerTo(resourceConfig);
    verify(resourceConfig).register(accessTokenAuthFilter);
    verify(resourceConfigProvider).get("io.xj.core.common");
  }

  @Test
  public void Configure_AccessLogEntitiesMaxSize() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.maxsize", "5");
    when(resourceConfigProvider.get("io.xj.core.common", "one", "two", "three"))
      .thenReturn(resourceConfig);

    app.configureServer("one", "two", "three");

    verify(accessLogFilterProvider).registerTo(resourceConfig);
    verify(resourceConfig).register(accessTokenAuthFilter);
    verify(resourceConfigProvider).get("io.xj.core.common", "one", "two", "three");
  }

  @Test
  public void Configure_AccessLogEntitiesAll() throws IOException {
    System.setProperty("log.access.filename", "/tmp/access.log");
    System.setProperty("log.access.entities.all", "true");
    when(resourceConfigProvider.get("io.xj.core.common", "one", "two", "three"))
      .thenReturn(resourceConfig);

    app.configureServer("one", "two", "three");

    verify(accessLogFilterProvider).registerTo(resourceConfig);
    verify(resourceConfig).register(accessTokenAuthFilter);
    verify(resourceConfigProvider).get("io.xj.core.common", "one", "two", "three");
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
    System.setProperty("app.host", "special");
    app = injector.getInstance(App.class);
    assertEquals("http://special:80/", app.baseURI());
  }

  @Test
  public void BaseURI_CustomPort() {
    System.setProperty("app.port", "7000");
    app = injector.getInstance(App.class);
    assertEquals("http://0.0.0.0:7000/", app.baseURI());
  }

*/
