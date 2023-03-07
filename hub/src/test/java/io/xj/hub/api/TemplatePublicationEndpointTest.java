// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.manager.TemplatePublicationManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.hub.tables.pojos.User;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.filestore.FileStoreProvider;
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
import java.util.UUID;

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
 * Hub can publish content for production fabrication https://www.pivotaltracker.com/story/show/180805580
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplatePublicationEndpointTest {
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  @Mock
  TemplatePublicationManager templatePublicationManager;
  @Mock
  FileStoreProvider fileStoreProvider;
  @Mock
  HubSqlStoreProvider sqlStoreProvider;
  @Mock
  HubIngestFactory ingestFactory;
  @Mock
  TemplateManager templateManager;
  private HubAccess access;
  private TemplatePublicationEndpoint subject;
  private Template template25;
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
    subject = new TemplatePublicationEndpoint(entityFactory, env, fileStoreProvider, sqlStoreProvider, ingestFactory, responseProvider, payloadFactory, templateManager, templatePublicationManager, jsonProvider);
  }

  @Test
  public void readManyForTemplate() throws ManagerException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    TemplatePublication templatePublication1 = buildTemplatePublication(template25, user1);
    TemplatePublication templatePublication2 = buildTemplatePublication(template25, user1);
    Collection<TemplatePublication> templatePublications = ImmutableList.of(templatePublication1, templatePublication2);
    when(templatePublicationManager.readMany(same(access), eq(ImmutableList.of(template25.getId()))))
      .thenReturn(templatePublications);

    var result = subject.readManyForTemplate(req, res, template25.getId().toString());

    verify(templatePublicationManager).readMany(same(access), eq(ImmutableList.of(template25.getId())));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertPayload(result.getBody())
      .hasDataMany("template-publications", ImmutableList.of(templatePublication1.getId().toString(), templatePublication2.getId().toString()));
  }

}
