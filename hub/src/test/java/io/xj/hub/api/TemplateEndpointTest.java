// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.Template;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.dao.TemplateDAO;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubTestConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
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
import java.util.UUID;

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
  TemplateDAO templateDAO;
  private HubAccess hubAccess;
  private TemplateEndpoint subject;
  private Account account25;
  private Account account1;

  @Before
  public void setUp() throws AppException {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
        bind(TemplateDAO.class).toInstance(templateDAO);
      }
    }));

    Topology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    account1 = new Account()
      .id(UUID.randomUUID());
    hubAccess = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    account25 = new Account()
      .id(UUID.randomUUID());
    subject = injector.getInstance(TemplateEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws DAOException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Template template1 = new Template()
      .id(UUID.randomUUID())
      .accountId(account25.getId())
      .name("fonds")
      .embedKey("ABC");
    Template template2 = new Template()
      .id(UUID.randomUUID())
      .accountId(account25.getId())
      .name("trunk")
      .embedKey("DEF");
    Collection<Template> templates = ImmutableList.of(template1, template2);
    when(templateDAO.readMany(same(hubAccess), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(templates);

    Response result = subject.readMany(crc, account25.getId().toString());

    verify(templateDAO).readMany(same(hubAccess), eq(ImmutableList.of(account25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("templates", ImmutableList.of(template1.getId().toString(), template2.getId().toString()));
  }

  @Test
  public void readMany_forTemplateAndUser() throws DAOException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Template template1 = new Template()
      .id(UUID.randomUUID())
      .accountId(account25.getId())
      .name("fonds")
      .embedKey("ABC");
    Template template2 = new Template()
      .id(UUID.randomUUID())
      .accountId(account25.getId())
      .name("trunk")
      .embedKey("DEF");
    Collection<Template> templates = ImmutableList.of(template1, template2);
    when(templateDAO.readMany(same(hubAccess), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(templates);

    Response result = subject.readMany(crc, account25.getId().toString());

    verify(templateDAO).readMany(same(hubAccess), eq(ImmutableList.of(account25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("templates", ImmutableList.of(template1.getId().toString(), template2.getId().toString()));
  }

  @Test
  public void readOne() throws DAOException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Template template1 = new Template()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("fonds")
      .embedKey("ABC");
    when(templateDAO.readOne(same(hubAccess), eq(template1.getId()))).thenReturn(template1);

    Response result = subject.readOne(crc, template1.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    JsonapiPayload resultJsonapiPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class);
    assertPayload(resultJsonapiPayload)
      .hasDataOne("templates", template1.getId().toString());
  }
}
