// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.Account;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.AssertPayload;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.dao.DAOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static io.xj.service.hub.access.HubAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
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
  HubEndpoint subject;
  private PayloadFactory payloadFactory;

  @Before
  public void setUp() throws Exception {
    Config config = ConfigFactory.parseResources("test.conf")
      .withFallback(AppConfiguration.getDefault());
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new JsonApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
      }
    }));
    payloadFactory = injector.getInstance(PayloadFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    subject = injector.getInstance(HubEndpoint.class);
  }

  @Test
  public void create() throws JsonApiException, ValueException, DAOException {
    HubAccess hubAccess = HubAccess.internal();
    Payload payload = payloadFactory.newPayload()
      .setDataOne(payloadFactory.newPayloadObject()
        .setType(Account.class)
        .setAttribute("name", "test5"));
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    when(dao.newInstance()).thenReturn(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build());
    var createdAccount = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("test5")
      .build();
    when(dao.create(same(hubAccess), any(Account.class))).thenReturn(createdAccount);

    Response result = subject.create(crc, dao, payload);

    verify(dao, times(1)).create(same(hubAccess), any(Account.class));
    assertEquals(201, result.getStatus());
    Payload resultPayload = payloadFactory.deserialize(String.valueOf(result.getEntity()));
    (new AssertPayload(resultPayload)).hasDataOne(createdAccount);
  }
}
