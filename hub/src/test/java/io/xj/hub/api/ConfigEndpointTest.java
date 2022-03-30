// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.hub.persistence.HubDatabaseProvider;
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

@RunWith(MockitoJUnitRunner.class)
public class ConfigEndpointTest {
  private ConfigEndpoint subject;

  @Mock
  ContainerRequestContext crc;

  @Mock
  ApiUrlProvider apiUrlProvider;

  @Mock
  HubDatabaseProvider hubDatabaseProvider;

  @Before
  public void setUp() throws AppException, JsonapiException {
    var injector = Guice.createInjector(Modules.override(new JsonapiModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        super.configure();
        bind(ApiUrlProvider.class).toInstance(apiUrlProvider);
        bind(HubDatabaseProvider.class).toInstance(hubDatabaseProvider);
      }
    }));
    HubTopology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    subject = injector.getInstance(ConfigEndpoint.class);
  }

  /**
   https://www.pivotaltracker.com/story/show/175771083 Enums should not have unrecognized values
   */
  @Test
  public void getConfig() {
    Response result = subject.getConfig(crc);

    assertEquals(200, result.getStatus());
    assertFalse(String.valueOf(result.getEntity()).contains("UNRECOGNIZED"));
    assertTrue(result.hasEntity());
  }

}
