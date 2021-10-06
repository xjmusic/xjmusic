// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ConfigEndpointTest {
  @Mock
  ContainerRequestContext crc;

  @Mock
  ApiUrlProvider apiUrlProvider;

  private ConfigEndpoint subject;

  @Before
  public void setUp() throws AppException, JsonapiException {
    doReturn("http://audio.xj.io/").when(apiUrlProvider).getAudioBaseUrl();
    doReturn("http://app.xj.io/").when(apiUrlProvider).getAppBaseUrl();
    doReturn("http://player.xj.io/").when(apiUrlProvider).getPlayerBaseUrl();
    doReturn("http://ship.xj.io/").when(apiUrlProvider).getShipBaseUrl();
    var injector = Guice.createInjector(Modules.override(new JsonapiModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        super.configure();
        bind(ApiUrlProvider.class).toInstance(apiUrlProvider);
      }
    }));
    HubTopology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    subject = injector.getInstance(ConfigEndpoint.class);
  }

  /**
   [#175771083] Enums should not have unrecognized values
   */
  @Test
  public void getConfig() {
    Response result = subject.getConfig(crc);

    assertEquals(200, result.getStatus());
    assertFalse(String.valueOf(result.getEntity()).contains("UNRECOGNIZED"));
    assertTrue(result.hasEntity());
  }

}
