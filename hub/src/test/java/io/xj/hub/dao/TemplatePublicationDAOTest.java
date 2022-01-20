// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or delete templatePublications

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class TemplatePublicationDAOTest {
  private TemplatePublicationDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private TemplatePublication templatePublication201;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "User, Artist"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Template "sandwich" has templatePublication "jams" and templatePublication "buns"
    fake.template1 = test.insert(buildTemplate(fake.account1, TemplateType.Preview, "sandwich", "sandwich55"));

    test.insert(buildTemplate(fake.account1, TemplateType.Preview, "Test Template", UUID.randomUUID().toString()));
    templatePublication201 = test.insert(buildTemplatePublication(fake.template1, fake.user2));

    // Instantiate the test subject
    testDAO = injector.getInstance(TemplatePublicationDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create_alwaysTakesUserFromHubAccess() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    TemplatePublication subject = buildTemplatePublication(fake.template1, fake.user3); // user will be overridden by hub access user id

    TemplatePublication result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void create_withoutSpecifyingUser() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    TemplatePublication subject = new TemplatePublication();
    subject.setId(UUID.randomUUID());
    subject.setTemplateId(fake.template1.getId());

    TemplatePublication result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void create_cannotPublicationProductionChain() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var template5 = test.insert(buildTemplate(fake.account1, TemplateType.Production, "test", UUID.randomUUID().toString()));

    TemplatePublication subject = buildTemplatePublication(template5, fake.user3); // user will be overridden by hub access user id

    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, subject));
    assertEquals("Preview-type Template is required", e.getMessage());
  }

  @Test
  public void update_notAllowed() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    TemplatePublication subject = test.insert(buildTemplatePublication(fake.template1, fake.user2));

    assertThrows(DAOException.class, () -> testDAO.update(hubAccess, subject.getId(), subject));
  }

  /**
   Should be able to load template even if user is playing two templates, or two users are playing one template #180124281
   */
  @Test
  public void create_archivesExistingForTemplate() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));

    var priorPublication = test.insert(buildTemplatePublication(fake.template1, fake.user3));
    var subject = buildTemplatePublication(fake.template1, fake.user2);

    testDAO.create(hubAccess, subject);

    assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, priorPublication.getId()));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    TemplatePublication result = testDAO.readOne(hubAccess, templatePublication201.getId());

    assertNotNull(result);
    assertEquals(templatePublication201.getId(), result.getId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void readOneForUser() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));

    var result = testDAO.readOneForUser(hubAccess, fake.user2.getId());

    assertTrue(result.isPresent());
    assertEquals(templatePublication201.getId(), result.get().getId());
    assertEquals(fake.user2.getId(), result.get().getUserId());
  }

  @Test
  public void readOneForUser_justCreated() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    test.insert(buildTemplatePublication(fake.template1, fake.user3));

    var result = testDAO.readOneForUser(hubAccess, fake.user3.getId());

    assertTrue(result.isPresent());
    assertEquals(fake.user3.getId(), result.get().getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInTemplate() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")
    ), "User");

    var e = assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, templatePublication201.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<TemplatePublication> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void readMany_seesAdditional() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePublication(fake.template1, fake.user3));

    Collection<TemplatePublication> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfTemplate() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

    Collection<TemplatePublication> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    TemplatePublication templatePublication251 = buildTemplatePublication(fake.template1, fake.user2);

    var e = assertThrows(DAOException.class, () -> testDAO.destroy(hubAccess, templatePublication251.getId()));
    assertEquals("Cannot delete template publication!", e.getMessage());
  }

}
