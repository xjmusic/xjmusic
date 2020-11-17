// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.UserRole;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static io.xj.lib.util.Assert.assertSameItems;
import static io.xj.service.hub.access.HubAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthEndpointTest {
  @Mock
  ContainerRequestContext crc;
  private HubAccess hubAccess;
  private AuthEndpoint subject;
  private Account account1;

  @Before
  public void setUp() throws AppException {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule()));
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    hubAccess = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    subject = injector.getInstance(AuthEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void getCurrentAuthentication() throws IOException, ValueException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);

    Response result = subject.getCurrentAuthentication(crc);

    assertEquals(202, result.getStatus());
    assertTrue(result.hasEntity());
    HubClientAccess resultAccess = new ObjectMapper().readValue(String.valueOf(result.getEntity()), HubClientAccess.class);
    assertSameItems(ImmutableSet.of(UserRole.Type.User, UserRole.Type.Artist), resultAccess.getRoleTypes());
    assertSameItems(ImmutableList.of(account1.getId()), resultAccess.getAccountIds());
  }
}
