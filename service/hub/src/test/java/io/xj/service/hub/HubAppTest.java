// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.App;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.rest_api.RestApiModule;
import io.xj.service.hub.access.AccessControlProvider;
import io.xj.service.hub.dao.PlatformMessageDAO;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HubAppTest {
  @Mock
  private PlatformMessageDAO platformMessageDAO;
  @Mock
  private SQLDatabaseProvider sqlDatabaseProvider;
  @Mock
  private AccessControlProvider accessControlProvider;
  //
  private App subject;

  @Before
  public void setUp() throws Exception {
    Config config = AppConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(1903))
      .withValue("prometheus.enabled", ConfigValueFactory.fromAnyRef(true));
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new RestApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(PlatformMessageDAO.class).toInstance(platformMessageDAO);
        bind(SQLDatabaseProvider.class).toInstance(sqlDatabaseProvider);
        bind(AccessControlProvider.class).toInstance(accessControlProvider);
      }
    }));
    subject = new HubApp(ImmutableSet.of(), injector);
    subject.start();
  }

  @After
  public void tearDown() {
    subject.stop();
  }

  @Test
  public void checkApp() throws Exception {
    HttpClient client = new HttpClient();
    client.start();

    ContentResponse res = client.GET("http://localhost:1903/o2");

    assertEquals(200, res.getStatus());
    client.stop();

    assertEquals("HubApp", subject.getName());
  }
}
