// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;


import io.xj.hub.HubTopology;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiResponseProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.jsonapi.PayloadDataType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Objects;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ConfigEndpointTest {
  private ConfigEndpoint subject;

  @Mock
  ApiUrlProvider apiUrlProvider;

  @Mock
  HubSqlStoreProvider sqlStoreProvider;

  @Before
  public void setUp() throws AppException, JsonapiException {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var env = AppEnvironment.getDefault();
    var responseProvider = new JsonapiResponseProviderImpl(apiUrlProvider);
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    var payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    subject = new ConfigEndpoint(env, sqlStoreProvider, responseProvider, payloadFactory, entityFactory);
  }

  /**
   * Enums should not have unrecognized values https://www.pivotaltracker.com/story/show/175771083
   */
  @Test
  public void getConfig() {
    var result = subject.getConfig();

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(Objects.requireNonNull(result.getBody()).getDataType(), PayloadDataType.One);
  }

}
