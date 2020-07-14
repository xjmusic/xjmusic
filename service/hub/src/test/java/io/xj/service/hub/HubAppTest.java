// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.App;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.service.hub.access.HubAccessControlProvider;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HubAppTest {
  @Mock
  private HubDatabaseProvider hubDatabaseProvider;
  @Mock
  private HubAccessControlProvider hubAccessControlProvider;
  private App subject;
  private CloseableHttpClient httpClient;

  @Before
  public void setUp() throws Exception {
    httpClient = HttpClients.createDefault();
    Config config = AppConfiguration.getDefault()
      .withValue("audio.baseUrl", ConfigValueFactory.fromAnyRef(""))
      .withValue("segment.baseUrl", ConfigValueFactory.fromAnyRef(""))
      .withValue("app.port", ConfigValueFactory.fromAnyRef(1903))
      .withValue("prometheus.enabled", ConfigValueFactory.fromAnyRef(true));
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new JsonApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(HubDatabaseProvider.class).toInstance(hubDatabaseProvider);
        bind(HubAccessControlProvider.class).toInstance(hubAccessControlProvider);
      }
    }));
    subject = new HubApp(ImmutableSet.of(), injector);
    subject.start();
  }

  @After
  public void tearDown() throws IOException {
    subject.finish();
    httpClient.close();
  }

  @Test
  public void checkApp() throws Exception {
    HttpGet request = new HttpGet(new URI("http://localhost:1903/o2"));
    CloseableHttpResponse result = httpClient.execute(request);

    assertEquals(200, result.getStatusLine().getStatusCode());

    assertEquals("HubApp", subject.getName());
  }
}
