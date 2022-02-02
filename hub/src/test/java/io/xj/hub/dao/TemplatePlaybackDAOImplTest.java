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
import io.xj.hub.tables.pojos.TemplatePlayback;
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

// future test: permissions of different users to readMany vs. of vs. update or delete templatePlaybacks

// FUTURE: any test that

@RunWith(MockitoJUnitRunner.class)
public class TemplatePlaybackDAOImplTest {
  private TemplatePlaybackDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private TemplatePlayback templatePlayback201;

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

    // Template "sandwich" has templatePlayback "jams" and templatePlayback "buns"
    fake.template1 = test.insert(buildTemplate(fake.account1, TemplateType.Preview, "sandwich", "sandwich55"));

    test.insert(buildTemplate(fake.account1, TemplateType.Preview, "Test Template", UUID.randomUUID().toString()));
    templatePlayback201 = test.insert(buildTemplatePlayback(fake.template1, fake.user2));

    // Instantiate the test subject
    testDAO = injector.getInstance(TemplatePlaybackDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create_alwaysTakesUserFromHubAccess() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    TemplatePlayback subject = buildTemplatePlayback(fake.template1, fake.user3); // user will be overridden by hub access user id

    TemplatePlayback result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void create_withoutSpecifyingUser() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    TemplatePlayback subject = new TemplatePlayback();
    subject.setId(UUID.randomUUID());
    subject.setTemplateId(fake.template1.getId());

    TemplatePlayback result = testDAO.create(hubAccess, subject);

    assertNotNull(result);
    assertEquals(fake.template1.getId(), result.getTemplateId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void create_cannotPlaybackProductionChain() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var template5 = test.insert(buildTemplate(fake.account1, TemplateType.Production, "test", UUID.randomUUID().toString()));

    TemplatePlayback subject = buildTemplatePlayback(template5, fake.user3); // user will be overridden by hub access user id

    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, subject));
    assertEquals("Preview-type Template is required", e.getMessage());
  }

  @Test
  public void update_notAllowed() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    TemplatePlayback subject = test.insert(buildTemplatePlayback(fake.template1, fake.user2));

    assertThrows(DAOException.class, () -> testDAO.update(hubAccess, subject.getId(), subject));
  }

  @Test
  public void create_archivesExistingForUser() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var priorTemplate = test.insert(buildTemplate(fake.account1, TemplateType.Preview, "Prior", UUID.randomUUID().toString()));

    var priorPlayback = test.insert(buildTemplatePlayback(priorTemplate, fake.user2));
    var subject = buildTemplatePlayback(fake.template1, fake.user2);

    testDAO.create(hubAccess, subject);

    assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, priorPlayback.getId()));
  }

  /**
   Should be able to load template even if user is playing two templates, or two users are playing one template #180124281
   */
  @Test
  public void create_archivesExistingForTemplate() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));

    var priorPlayback = test.insert(buildTemplatePlayback(fake.template1, fake.user3));
    var subject = buildTemplatePlayback(fake.template1, fake.user2);

    testDAO.create(hubAccess, subject);

    assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, priorPlayback.getId()));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    TemplatePlayback result = testDAO.readOne(hubAccess, templatePlayback201.getId());

    assertNotNull(result);
    assertEquals(templatePlayback201.getId(), result.getId());
    assertEquals(fake.user2.getId(), result.getUserId());
  }

  @Test
  public void readOneForUser() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));

    var result = testDAO.readOneForUser(hubAccess, fake.user2.getId());

    assertTrue(result.isPresent());
    assertEquals(templatePlayback201.getId(), result.get().getId());
    assertEquals(fake.user2.getId(), result.get().getUserId());
  }

  @Test
  public void readOneForUser_justCreated() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    test.insert(buildTemplatePlayback(fake.template1, fake.user3));

    var result = testDAO.readOneForUser(hubAccess, fake.user3.getId());

    assertTrue(result.isPresent());
    assertEquals(fake.user3.getId(), result.get().getUserId());
  }

  @Test
  public void readOneForUser_notIfOlderThanThreshold() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var olderPlayback = buildTemplatePlayback(fake.template1, fake.user3);
    olderPlayback.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(olderPlayback);

    var result = testDAO.readOneForUser(hubAccess, fake.user3.getId());

    assertTrue(result.isEmpty());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInTemplate() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")
    ), "User");

    var e = assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, templatePlayback201.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<TemplatePlayback> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void readMany_seesAdditional() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePlayback(fake.template1, fake.user3));

    Collection<TemplatePlayback> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_seesNoneOlderThanThreshold() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    var olderPlayback = buildTemplatePlayback(fake.template1, fake.user3);
    olderPlayback.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(olderPlayback);

    Collection<TemplatePlayback> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfTemplate() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

    Collection<TemplatePlayback> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.template1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    TemplatePlayback templatePlayback251 = buildTemplatePlayback(fake.template1, fake.user2);

    testDAO.destroy(hubAccess, templatePlayback251.getId());

    try {
      testDAO.readOne(HubAccess.internal(), templatePlayback251.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

}
