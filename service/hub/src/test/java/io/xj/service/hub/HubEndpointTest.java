// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.rest_api.RestApiModule;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.testing.AssertPayload;
import io.xj.service.hub.work.WorkManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import static io.xj.service.hub.access.Access.CONTEXT_KEY;
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
  WorkManager workManager;
  @Mock
  DAO<Account> dao; // can be any class that extends Entity, we picked a simple one with no belongs-to
  //
  HubEndpoint subject;
  private PayloadFactory payloadFactory;

  @Before
  public void setUp() throws Exception {
    Config config = ConfigFactory.parseResources("test.conf")
      .withFallback(AppConfiguration.getDefault());
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new RestApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(WorkManager.class).toInstance(workManager);
      }
    }));
    payloadFactory = injector.getInstance(PayloadFactory.class);
    HubApp.buildApiTopology(payloadFactory);
    subject = new HubEndpoint(injector);
  }

  @Test
  public void create() throws RestApiException, ValueException, HubException {
    Access access = Access.internal();
    Payload payload = payloadFactory.newPayload()
      .setDataOne(payloadFactory.newPayloadObject()
        .setType(Account.class)
        .setAttribute("name", "test5"));
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(dao.newInstance()).thenReturn(new Account());
    Account createdAccount = Account.create("test5");
    when(dao.create(same(access), any(Account.class))).thenReturn(createdAccount);

    Response result = subject.create(crc, dao, payload);

    verify(dao, times(1)).create(same(access), any(Account.class));
    assertEquals(201, result.getStatus());
    Payload resultPayload = payloadFactory.deserialize(String.valueOf(result.getEntity()));
    (new AssertPayload(resultPayload)).hasDataOne(createdAccount);
  }

/*
FUTURE like the above create() test

  @Test
  public void readOne() {
  }

  @Test
  public void readMany() {
  }

  @Test
  public void testReadMany() {
  }

  @Test
  public void update() {
  }

  @Test
  public void delete() {
  }
*/
}
