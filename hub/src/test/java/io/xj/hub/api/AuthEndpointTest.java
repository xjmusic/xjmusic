// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubTestConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthEndpointTest {
  @Mock
  ContainerRequestContext crc;
  private HubAccess hubAccess;
  private AuthEndpoint subject;

  @Before
  public void setUp() throws AppException {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    Topology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    Account account1 = new Account()
      .id(UUID.randomUUID())
      ;
    hubAccess = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    subject = injector.getInstance(AuthEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void getCurrentAuthentication() {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);

    Response result = subject.getCurrentAuthentication(crc);

    assertEquals(202, result.getStatus());
    assertTrue(result.hasEntity());
  }
}
