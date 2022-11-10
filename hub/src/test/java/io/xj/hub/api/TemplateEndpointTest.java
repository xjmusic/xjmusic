// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplatePlayback;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplatePublication;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplateEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  TemplateManager templateManager;
  private HubAccess access;
  private TemplateEndpoint subject;
  private Account account25;
  private Account account1;

  @Before
  public void setUp() throws AppException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(TemplateManager.class).toInstance(templateManager);
      }
    }));

    HubTopology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    account1 = buildAccount("Testing Account 1");
    access = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    account25 = buildAccount("Testing Account 25");
    subject = injector.getInstance(TemplateEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account25, "fonds", "ABC");
    Template template2 = buildTemplate(account25, "trunk", "DEF");
    Collection<Template> templates = ImmutableList.of(template1, template2);
    when(templateManager.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(templates);

    Response result = subject.readMany(crc, account25.getId());

    verify(templateManager).readMany(same(access), eq(ImmutableList.of(account25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("templates", ImmutableList.of(template1.getId().toString(), template2.getId().toString()));
  }

  @Test
  public void readMany_forTemplateAndUser() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account25, "fonds", "ABC");
    Template template2 = buildTemplate(account25, "trunk", "DEF");
    Collection<Template> templates = ImmutableList.of(template1, template2);
    when(templateManager.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(templates);

    Response result = subject.readMany(crc, account25.getId());

    verify(templateManager).readMany(same(access), eq(ImmutableList.of(account25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("templates", ImmutableList.of(template1.getId().toString(), template2.getId().toString()));
  }

  @Test
  public void readOne() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account1, "fonds", "ABC");
    when(templateManager.readOne(same(access), eq(template1.getId()))).thenReturn(template1);

    Response result = subject.readOne(crc, template1.getId().toString(), "");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    JsonapiPayload resultJsonapiPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class);
    assertPayload(resultJsonapiPayload)
      .hasDataOne("templates", template1.getId().toString());
  }

  /**
   Hub can publish content for production fabrication https://www.pivotaltracker.com/story/show/180805580
   */
  @Test
  public void readOne_includingBindingsAndPlaybacksAndPublications() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
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

    Response result = subject.readOne(crc, template4.getId().toString(), "template-bindings,template-playbacks,template-publications");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    JsonapiPayload resultJsonapiPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class);
    assertPayload(resultJsonapiPayload).hasDataOne("templates", template4.getId().toString());
    assertPayload(resultJsonapiPayload).hasIncluded("template-bindings", List.of(templateBinding43));
    assertPayload(resultJsonapiPayload).hasIncluded("template-playbacks", List.of(templatePlayback42));
    assertPayload(resultJsonapiPayload).hasIncluded("template-publications", List.of(templatePublication67));
  }

  @Test
  public void readOne_byShipKey() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Template template1 = buildTemplate(account1, "fonds", "ABC");
    when(templateManager.readOneByShipKey(same(access), eq("ABC"))).thenReturn(Optional.of(template1));

    Response result = subject.readOne(crc, "ABC", "");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    JsonapiPayload resultJsonapiPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class);
    assertPayload(resultJsonapiPayload)
      .hasDataOne("templates", template1.getId().toString());
  }


  /**
   Preview template functionality is dope (not wack) https://www.pivotaltracker.com/story/show/183576743
   */
  @Test
  public void readAllPlaying() throws ManagerException, IOException, JsonapiException {
    access = HubAccess.internal();
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    var user1 = buildUser("Jim", "jim@email.com", "https://pictures.com/jim.jpg", "Artist");
    var template1 = buildTemplate(account25, "fonds", "ABC");
    when(templateManager.readOnePlayingForUser(same(access), eq(user1.getId()))).thenReturn(Optional.of(template1));

    Response result = subject.readAllPlaying(crc, user1.getId());

    verify(templateManager).readOnePlayingForUser(same(access), eq(user1.getId()));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataOne("templates", template1.getId().toString());
  }
}
