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
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.manager.TemplatePublicationManager;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePublication;
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

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 Hub can publish content for production fabrication #180805580
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplatePublicationEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  TemplatePublicationManager templatePublicationManager;
  private HubAccess hubAccess;
  private TemplatePublicationEndpoint subject;
  private Template template25;
  private User user1;

  @Before
  public void setUp() throws AppException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(TemplatePublicationManager.class).toInstance(templatePublicationManager);
      }
    }));

    HubTopology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    Account account1 = buildAccount("Testing");
    user1 = buildUser("Joe", "joe@email.com", "joe.jpg", "User,Artist");
    hubAccess = HubAccess.create(user1, ImmutableList.of(account1));
    template25 = buildTemplate(account1, "Testing");
    subject = injector.getInstance(TemplatePublicationEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readManyForTemplate() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    TemplatePublication templatePublication1 = buildTemplatePublication(template25, user1);
    TemplatePublication templatePublication2 = buildTemplatePublication(template25, user1);
    Collection<TemplatePublication> templatePublications = ImmutableList.of(templatePublication1, templatePublication2);
    when(templatePublicationManager.readMany(same(hubAccess), eq(ImmutableList.of(template25.getId()))))
      .thenReturn(templatePublications);

    Response result = subject.readManyForTemplate(crc, template25.getId().toString());

    verify(templatePublicationManager).readMany(same(hubAccess), eq(ImmutableList.of(template25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("template-publications", ImmutableList.of(templatePublication1.getId().toString(), templatePublication2.getId().toString()));
  }

}
