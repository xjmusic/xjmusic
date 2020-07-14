// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubClientModule;
import io.xj.service.hub.digest.HubDigestModule;
import io.xj.service.hub.entity.Account;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.NexusDAOModule;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.persistence.NexusEntityStoreModule;
import io.xj.service.nexus.testing.NexusTestConfiguration;
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

import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static io.xj.service.hub.client.HubClientAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ChainDAO chainDAO;
  private HubClientAccess access;
  private ChainEndpoint subject;
  private Account account25;

  @Before
  public void setUp() throws AppException {
    Config config = NexusTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of((Modules.override(
      new FileStoreModule(),
      new HubClientModule(),
      new HubDigestModule(),
      new MixerModule(),
      new NexusDAOModule(),
      new NexusEntityStoreModule(),
      new JsonApiModule()
    ).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ChainDAO.class).toInstance(chainDAO);
        }
      }))));
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    NexusApp.buildApiTopology(injector.getInstance(EntityFactory.class));

    account25 = Account.create();
    access = HubClientAccess.create(ImmutableList.of(account25), "User,Artist");
    subject = new ChainEndpoint(injector);
    injector.injectMembers(subject);
  }

  @Test
  public void readAll() throws IOException, JsonApiException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chain1;
    Chain chain2;
    chain1 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    chain2 = Chain.create(account25, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-09-12T12:17:02.527142Z"), null, null);
    Collection<Chain> chains = ImmutableList.of(chain1, chain2);
    when(chainDAO.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(chains);

    Response result = subject.readAll(crc, account25.getId());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class))
      .hasDataMany("chains", ImmutableList.of(chain1.getId().toString(), chain2.getId().toString()));
  }

  @Test
  public void readOne() throws IOException, JsonApiException, NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Account account25 = Account.create();
    Chain chain17 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    when(chainDAO.readOne(same(access), eq(chain17.getId()))).thenReturn(chain17);

    Response result = subject.readOne(crc, chain17.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class))
      .hasDataOne("chains", chain17.getId().toString());
  }

  @Test
  public void delete() throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Account account25 = Account.create();
    Chain chain17 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");

    Response result = subject.delete(crc, chain17.getId());

    assertEquals(204, result.getStatus());
    assertFalse(result.hasEntity());
    verify(chainDAO, times(1)).destroy(same(access), eq(chain17.getId()));
  }

}
