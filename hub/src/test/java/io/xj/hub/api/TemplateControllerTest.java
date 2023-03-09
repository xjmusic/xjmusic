// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TemplateControllerTest {
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  @Mock
  TemplateManager templateManager;
  @Mock
  private HubSqlStoreProvider sqlStoreProvider;
  private HubAccess access;
  private TemplateController subject;
  private Account account25;
  private Account account1;

  @BeforeEach
  public void setUp() throws AppException {
    var env = AppEnvironment.getDefault();
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubTopology.buildHubApiTopology(entityFactory);
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider(env);
    JsonapiResponseProvider responseProvider = new JsonapiResponseProviderImpl(apiUrlProvider);

    account1 = buildAccount("Testing Account 1");
    access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(account1), "User,Artist");
    account25 = buildAccount("Testing Account 25");
    subject = new TemplateController(entityFactory, sqlStoreProvider, responseProvider, payloadFactory, templateManager);
  }

  @Test
  public void readMany() throws ManagerException, IOException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account25, "fonds", "ABC");
    Template template2 = buildTemplate(account25, "trunk", "DEF");
    Collection<Template> templates = ImmutableList.of(template1, template2);
    when(templateManager.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(templates);

    var result = subject.readMany(req, res, account25.getId());

    verify(templateManager).readMany(same(access), eq(ImmutableList.of(account25.getId())));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertPayload(result.getBody())
      .hasDataMany("templates", ImmutableList.of(template1.getId().toString(), template2.getId().toString()));
  }

  @Test
  public void readMany_forTemplateAndUser() throws ManagerException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account25, "fonds", "ABC");
    Template template2 = buildTemplate(account25, "trunk", "DEF");
    Collection<Template> templates = ImmutableList.of(template1, template2);
    when(templateManager.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(templates);

    var result = subject.readMany(req, res, account25.getId());

    verify(templateManager).readMany(same(access), eq(ImmutableList.of(account25.getId())));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertPayload(result.getBody())
      .hasDataMany("templates", ImmutableList.of(template1.getId().toString(), template2.getId().toString()));
  }

  @Test
  public void readOne() throws ManagerException, IOException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account1, "fonds", "ABC");
    when(templateManager.readOne(same(access), eq(template1.getId()))).thenReturn(template1);

    var result = subject.readOne(req, res, template1.getId().toString(), "");

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    JsonapiPayload resultJsonapiPayload = result.getBody();
    assertPayload(resultJsonapiPayload)
      .hasDataOne("templates", template1.getId().toString());
  }

  /**
   * Hub can publish content for production fabrication https://www.pivotaltracker.com/story/show/180805580
   */
  @Test
  public void readOne_includingBindingsAndPlaybacksAndPublications() throws ManagerException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    var account1 = buildAccount("bananas");
    var user2 = buildUser("Amelie", "amelie@email.com", "https://pictures.com/amelie.gif", "Admin");
    var library3 = buildLibrary(account1, "Test Library");
    Template template4 = buildTemplate(account1, "fonds", "ABC");
    var templateBinding43 = buildTemplateBinding(template4, library3);
    var templatePlayback42 = buildTemplatePlayback(template4, user2);
    var templatePublication67 = buildTemplatePublication(template4, user2);
    when(templateManager.readOne(same(access), eq(template4.getId()))).thenReturn(template4);
    when(templateManager.readChildEntities(same(access), eq(List.of(template4.getId())), eq(List.of("template-bindings", "template-playbacks", "template-publications"))))
      .thenReturn(List.of(templateBinding43, templatePlayback42, templatePublication67));

    var result = subject.readOne(req, res, template4.getId().toString(), "template-bindings,template-playbacks,template-publications");

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    JsonapiPayload resultJsonapiPayload = result.getBody();
    assertPayload(resultJsonapiPayload).hasDataOne("templates", template4.getId().toString());
    assertPayload(resultJsonapiPayload).hasIncluded("template-bindings", List.of(templateBinding43));
    assertPayload(resultJsonapiPayload).hasIncluded("template-playbacks", List.of(templatePlayback42));
    assertPayload(resultJsonapiPayload).hasIncluded("template-publications", List.of(templatePublication67));
  }

  @Test
  public void readOne_byShipKey() throws ManagerException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account1, "fonds", "ABC");
    when(templateManager.readOneByShipKey(same(access), eq("ABC"))).thenReturn(Optional.of(template1));

    var result = subject.readOne(req, res, "ABC", "");

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    JsonapiPayload resultJsonapiPayload = result.getBody();
    assertPayload(resultJsonapiPayload)
      .hasDataOne("templates", template1.getId().toString());
  }


  /**
   * Preview template functionality is dope (not wack) https://www.pivotaltracker.com/story/show/183576743
   */
  @Test
  public void readAllPlaying() throws ManagerException, JsonapiException {
    access = HubAccess.internal();
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    var user1 = buildUser("Jim", "jim@email.com", "https://pictures.com/jim.jpg", "Artist");
    var template1 = buildTemplate(account25, "fonds", "ABC");
    when(templateManager.readOnePlayingForUser(same(access), eq(user1.getId()))).thenReturn(Optional.of(template1));

    var result = subject.readAllPlaying(req, res, user1.getId());

    verify(templateManager).readOnePlayingForUser(same(access), eq(user1.getId()));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertPayload(result.getBody())
      .hasDataOne("templates", template1.getId().toString());
  }
}
