// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Chain;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.mixer.MixerModule;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.NexusDAOModule;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubClientModule;
import io.xj.nexus.persistence.NexusEntityStoreModule;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static io.xj.nexus.hub_client.client.HubClientAccess.CONTEXT_KEY;
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
  @Mock
  Environment env;
  private HubClientAccess access;
  private ChainEndpoint subject;
  private Account account25;
  private JsonProviderImpl jsonProvider;

  @Before
  public void setUp() throws AppException {
    Config config = NexusTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, env, ImmutableSet.of((Modules.override(
      new FileStoreModule(),
      new HubClientModule(),
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
    Topology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    Topology.buildNexusApiTopology(injector.getInstance(EntityFactory.class));

    account25 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account25), "User,Artist");
    subject = injector.getInstance(ChainEndpoint.class);
    jsonProvider = injector.getInstance(JsonProviderImpl.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws IOException, JsonApiException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chain1;
    Chain chain2;
    chain1 = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account25.getId())
      .setName("Test Print #1")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .build();
    chain2 = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account25.getId())
      .setName("Test Print #2")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2014-09-12T12:17:02.527142Z")
      .build();
    Collection<Chain> chains = ImmutableList.of(chain1, chain2);
    when(chainDAO.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(chains);

    Response result = subject.readMany(crc, account25.getId());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(jsonProvider.getObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("chains", ImmutableList.of(chain1.getId(), chain2.getId()));
  }

  @Test
  public void readOne() throws IOException, JsonApiException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    var account25 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var chain17 = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account25.getId())
      .setName("Test Print #1")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Ready)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setEmbedKey("test_print")
      .build();
    when(chainDAO.readOne(same(access), eq(chain17.getId()))).thenReturn(chain17);

    Response result = subject.readOne(crc, chain17.getId());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(jsonProvider.getObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataOne("chains", chain17.getId());
  }

  @Test
  public void delete() throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    var account25 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var chain17 = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account25.getId())
      .setName("Test Print #1")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Ready)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .setEmbedKey("test_print")
      .build();

    Response result = subject.delete(crc, chain17.getId());

    assertEquals(204, result.getStatus());
    assertFalse(result.hasEntity());
    verify(chainDAO, times(1)).destroy(same(access), eq(chain17.getId()));
  }

}
