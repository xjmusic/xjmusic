// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.api;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.ApiUrlProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import static io.xj.service.hub.client.HubClientAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubConfigEndpointTest {
  @Mock
  ContainerRequestContext crc;

  @Mock
  ApiUrlProvider apiUrlProvider;

  private HubClientAccess access;
  private HubConfigEndpoint subject;
  private Account account25;

  @Before
  public void setUp() throws AppException, JsonApiException {
    Config config = HubTestConfiguration.getDefault();
    doReturn("http://audio.xj.io/").when(apiUrlProvider).getAudioBaseUrl();
    doReturn("http://app.xj.io/").when(apiUrlProvider).getAppBaseUrl();
    doReturn("http://player.xj.io/").when(apiUrlProvider).getPlayerBaseUrl();
    doReturn("http://ship.xj.io/").when(apiUrlProvider).getSegmentBaseUrl();
    Injector injector = Guice.createInjector(Modules.override(new JsonApiModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        super.configure();
        bind(Config.class).toInstance(config);
        bind(ApiUrlProvider.class).toInstance(apiUrlProvider);
      }
    }));
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    subject = injector.getInstance(HubConfigEndpoint.class);
  }

  @Test
  public void getConfig() throws JsonApiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);

    Response result = subject.getConfig(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
  }

}
