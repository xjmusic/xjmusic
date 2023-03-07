// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubTopology;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.UserManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Account;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiResponseProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthEndpointTest {
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  private HubAccess access;
  private AuthEndpoint subject;
  @Mock
  GoogleProvider authGoogleProvider;
  @Mock
  HubSqlStoreProvider sqlStoreProvider;
  @Mock
  UserManager userManager;

  @Before
  public void setUp() throws AppException {
    var env = AppEnvironment.getDefault();
    JsonProvider jsonProvider = new JsonProviderImpl();
    var apiUrlProvider = new ApiUrlProvider(env);
    var responseProvider = new JsonapiResponseProviderImpl(apiUrlProvider);
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    var payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);

    Account account1 = buildAccount("Testing");
    access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(account1), "User,Artist");
    subject = new AuthEndpoint(apiUrlProvider, env, entityFactory, authGoogleProvider, sqlStoreProvider, responseProvider, payloadFactory, userManager);
  }

  @Test
  public void getCurrentAuthentication() throws IOException {
    when(req.getAttribute(eq(CONTEXT_KEY))).thenReturn(access);

    ResponseEntity<HubAccess> result = subject.getCurrentAuthentication(req, res);

    assertEquals(HttpStatus.ACCEPTED, result.getStatusCode());
    assertEquals(access.getUserId(), Objects.requireNonNull(result.getBody()).getUserId());
  }
}
