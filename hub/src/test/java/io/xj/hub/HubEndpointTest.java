// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAO;
import io.xj.hub.dao.DAOException;
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.AssertPayload;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  DAO<Account> dao; // can be any class that, we picked a simple one with no belongs-to
  //
  HubJsonapiEndpoint subject;
  private JsonapiPayloadFactory payloadFactory;

  @Before
  public void setUp() throws Exception {
    Config config = AppConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new JsonapiModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    payloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    subject = injector.getInstance(HubJsonapiEndpoint.class);
  }

  @Test
  public void create() throws JsonapiException, ValueException, DAOException {
    HubAccess hubAccess = HubAccess.internal();
    JsonapiPayload jsonapiPayload = payloadFactory.newJsonapiPayload()
      .setDataOne(payloadFactory.newPayloadObject()
        .setType(Account.class)
        .setAttribute("name", "test5"));
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    when(dao.newInstance()).thenReturn(buildAccount("Testing"));
    var createdAccount = buildAccount("Testing");
    createdAccount.setName("test5");
    when(dao.create(same(hubAccess), any(Account.class))).thenReturn(createdAccount);

    Response result = subject.create(crc, dao, jsonapiPayload);

    verify(dao, times(1)).create(same(hubAccess), any(Account.class));
    assertEquals(201, result.getStatus());
    JsonapiPayload resultJsonapiPayload = payloadFactory.deserialize(String.valueOf(result.getEntity()));
    (new AssertPayload(resultJsonapiPayload)).hasDataOne(createdAccount);
  }
}
