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
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.dao.TemplatePlaybackDAO;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.hub.tables.pojos.User;
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
import java.util.Optional;

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
  private HubAccess hubAccess;
  private TemplatePlaybackEndpoint subject;
  private Template template25;
  private Template template1;
  private User user1;
  @Mock
  ContainerRequestContext crc;
  @Mock
  TemplatePlaybackDAO templatePlaybackDAO;

  @Before
  public void setUp() throws AppException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(TemplatePlaybackDAO.class).toInstance(templatePlaybackDAO);
      }
    }));

    HubTopology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    Account account1 = buildAccount("Testing");
    user1 = buildUser("Joe", "joe@email.com", "joe.jpg", "User,Artist");
    hubAccess = HubAccess.create(user1, ImmutableList.of(account1));
    template25 = buildTemplate(account1, "Testing");
    template1 = buildTemplate(account1, "Testing");
    subject = injector.getInstance(TemplatePlaybackEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readManyForTemplate() throws DAOException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    TemplatePlayback templatePlayback1 = buildTemplatePlayback(template25, user1);
    TemplatePlayback templatePlayback2 = buildTemplatePlayback(template25, user1);
    Collection<TemplatePlayback> templatePlaybacks = ImmutableList.of(templatePlayback1, templatePlayback2);
    when(templatePlaybackDAO.readMany(same(hubAccess), eq(ImmutableList.of(template25.getId()))))
      .thenReturn(templatePlaybacks);

    Response result = subject.readManyForTemplate(crc, template25.getId().toString());

    verify(templatePlaybackDAO).readMany(same(hubAccess), eq(ImmutableList.of(template25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("template-playbacks", ImmutableList.of(templatePlayback1.getId().toString(), templatePlayback2.getId().toString()));
  }

  @Test
  public void readOneForUser() throws DAOException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    TemplatePlayback templatePlayback1 = buildTemplatePlayback(template1, user1);
    when(templatePlaybackDAO.readOneForUser(same(hubAccess), eq(user1.getId()))).thenReturn(Optional.of(templatePlayback1));

    Response result = subject.readOneForUser(crc, user1.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    JsonapiPayload resultJsonapiPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class);
    assertPayload(resultJsonapiPayload)
      .hasDataOne("template-playbacks", templatePlayback1.getId().toString());
  }


  @Test
  public void readOneForUser_noneFound() throws DAOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    when(templatePlaybackDAO.readOneForUser(same(hubAccess), eq(user1.getId()))).thenReturn(Optional.empty());

    Response result = subject.readOneForUser(crc, user1.getId().toString());

    assertEquals(204, result.getStatus());
    assertFalse(result.hasEntity());
  }
}
