// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.HubException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ChainDAO;
import io.xj.service.hub.digest.DigestModule;
import io.xj.service.hub.generation.GenerationModule;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.InternalResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;

import static io.xj.service.hub.access.Access.CONTEXT_KEY;
import static io.xj.service.hub.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ChainDAO chainDAO;
  private Access access;
  private ChainEndpoint subject;
  private Account account25;

  @Before
  public void setUp() throws AppException {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of((Modules.override(new HubModule(), new DigestModule(), new GenerationModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ChainDAO.class).toInstance(chainDAO);
        }
      }))));
    HubApp.buildApiTopology(injector.getInstance(PayloadFactory.class));

    account25 = Account.create();
    access = Access.create(ImmutableList.of(account25), "User,Artist");
    subject = new ChainEndpoint(injector);
    injector.injectMembers(subject);
  }

  @Test
  public void readAll() throws HubException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chain1;
    Chain chain2;
    chain1 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    chain2 = Chain.create(account25, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-09-12T12:17:02.527142Z"), null, null);
    Collection<Chain> chains = ImmutableList.of(chain1, chain2);
    when(chainDAO.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(chains);

    Response result = subject.readAll(crc, account25.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(InternalResources.deserializePayload(result.getEntity()))
      .hasDataMany("chains", ImmutableList.of(chain1.getId().toString(), chain2.getId().toString()));
  }

  @Test
  public void readOne() throws HubException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Account account25 = Account.create();
    Chain chain17 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    when(chainDAO.readOne(same(access), eq(chain17.getId()))).thenReturn(chain17);

    Response result = subject.readOne(crc, chain17.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(InternalResources.deserializePayload(result.getEntity()))
      .hasDataOne("chains", chain17.getId().toString());
  }

}
