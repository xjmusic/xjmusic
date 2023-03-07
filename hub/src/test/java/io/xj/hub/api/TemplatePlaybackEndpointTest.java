// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.TemplatePlaybackManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.hub.tables.pojos.User;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplatePlaybackEndpointTest {
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  @Mock
  TemplatePlaybackManager templatePlaybackManager;
  @Mock
  private HubSqlStoreProvider sqlStoreProvider;
  private HubAccess access;
  private TemplatePlaybackEndpoint subject;
  private Template template25;
  private Template template1;
  private User user1;

  @Before
  public void setUp() throws AppException {
    var env = AppEnvironment.getDefault();
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubTopology.buildHubApiTopology(entityFactory);
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider(env);
    JsonapiResponseProvider responseProvider = new JsonapiResponseProviderImpl(apiUrlProvider);

    Account account1 = buildAccount("Testing");
    user1 = buildUser("Joe", "joe@email.com", "joe.jpg", "User,Artist");
    access = HubAccess.create(user1, UUID.randomUUID(), ImmutableList.of(account1));
    template25 = buildTemplate(account1, "Testing");
    template1 = buildTemplate(account1, "Testing");
    subject = new TemplatePlaybackEndpoint(templatePlaybackManager, sqlStoreProvider, responseProvider, payloadFactory, entityFactory);
  }

  @Test
  public void readManyForTemplate() throws ManagerException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    TemplatePlayback templatePlayback1 = buildTemplatePlayback(template25, user1);
    TemplatePlayback templatePlayback2 = buildTemplatePlayback(template25, user1);
    Collection<TemplatePlayback> templatePlaybacks = ImmutableList.of(templatePlayback1, templatePlayback2);
    when(templatePlaybackManager.readMany(same(access), eq(ImmutableList.of(template25.getId()))))
      .thenReturn(templatePlaybacks);

    var result = subject.readManyForTemplate(req, res, template25.getId().toString());

    verify(templatePlaybackManager).readMany(same(access), eq(ImmutableList.of(template25.getId())));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertPayload(result.getBody())
      .hasDataMany("template-playbacks", ImmutableList.of(templatePlayback1.getId().toString(), templatePlayback2.getId().toString()));
  }

  @Test
  public void readOneForUser() throws ManagerException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    TemplatePlayback templatePlayback1 = buildTemplatePlayback(template1, user1);
    when(templatePlaybackManager.readOneForUser(same(access), eq(user1.getId()))).thenReturn(Optional.of(templatePlayback1));

    var result = subject.readOneForUser(req, res, user1.getId().toString());

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    JsonapiPayload resultJsonapiPayload = result.getBody();
    assertPayload(resultJsonapiPayload)
      .hasDataOne("template-playbacks", templatePlayback1.getId().toString());
  }

  @Test
  public void readOneForUser_noneFound() throws ManagerException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    when(templatePlaybackManager.readOneForUser(same(access), eq(user1.getId()))).thenReturn(Optional.empty());

    var result = subject.readOneForUser(req, res, user1.getId().toString());

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }
}
