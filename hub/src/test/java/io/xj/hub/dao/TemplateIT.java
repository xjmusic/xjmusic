// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.Program;
import io.xj.api.Template;
import io.xj.api.TemplateType;
import io.xj.api.User;
import io.xj.hub.HubException;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.HubTestConfiguration;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplatePlayback;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TemplateIT {
  private TemplateDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private Template template2a;
  private Template template1a;
  private Template template1b;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "palm tree" has template "leaves" and template "coconuts"
    fake.account1 = test.insert(buildAccount("palm tree"));
    fake.user2 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(buildAccountUser(fake.account1, fake.user2));
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    test.insert(buildAccountUser(fake.account1, fake.user3));
    template1a = test.insert(buildTemplate(fake.account1, "leaves", "embed5leaves"));
    template1b = test.insert(buildTemplate(fake.account1, "coconuts", "embed5coconuts"));

    // Account "boat" has template "helm" and template "sail"
    fake.account2 = test.insert(buildAccount("boat"));
    template2a = test.insert(buildTemplate(fake.account2, "helm", UUID.randomUUID().toString()));
    test.insert(buildTemplate(fake.account2, "sail", UUID.randomUUID().toString()));

    // Instantiate the test subject
    testDAO = injector.getInstance(TemplateDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create_hasDefaultTemplateConfig() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("coconuts")
      .accountId(fake.account1.getId());

    Template result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertTrue(result.getConfig().contains("choiceDeltaEnabled = true"));
  }

  @Test
  public void create_hasGeneratedEmbedKey() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("coconuts")
      .accountId(fake.account1.getId());

    Template result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(9, result.getEmbedKey().length());
  }

  @Test
  public void create_cantHaveSameEmbedKeyAsExistingTemplate() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    test.insert(buildTemplate(fake.account1, "Prior", UUID.randomUUID().toString()).embedKey("key55"));
    Template inputData = buildTemplate(fake.account1, "New", UUID.randomUUID().toString()).embedKey("key55");

    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, inputData));
    assertEquals("Found Template with same Embed Key", e.getMessage());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Template.
   */
  @Test
  public void create_asEngineer() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Template inputData = new Template()
      .name("coconuts")
      .accountId(fake.account1.getId());

    Template result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Template.
   */
  @Test
  public void create_asEngineer_failsWithoutAccountAccess() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "Engineer");
    Template inputData = new Template()
      .name("coconuts")
      .accountId(fake.account1.getId());


    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, inputData));
    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void create_FailsWithoutAccountID() {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("coconuts");


    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, inputData));
    assertEquals("io.xj.lib.util.ValueException: Account ID is required.", e.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Template result = testDAO.readOne(hubAccess, template1b.getId());

    assertNotNull(result);
    assertEquals(template1b.getId(), result.getId());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");

    var e = assertThrows(DAOException.class, () -> testDAO.readOne(hubAccess, fake.account1.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Collection<Template> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
    Iterator<Template> resultIt = result.iterator();
    assertEquals("leaves", resultIt.next().getName());
    assertEquals("coconuts", resultIt.next().getName());
  }

  @Test
  public void readMany_fromAllAccounts() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1, fake.account2), "User");

    Collection<Template> result = testDAO.readMany(hubAccess, Lists.newArrayList());

    assertEquals(4L, result.size());
    Iterator<Template> it = result.iterator();
    assertEquals("leaves", it.next().getName());
    assertEquals("coconuts", it.next().getName());
    assertEquals("helm", it.next().getName());
    assertEquals("sail", it.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "User");

    Collection<Template> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.account1.getId()));

    assertEquals(0L, result.size());
  }


  @Test
  public void readAllPlaying() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePlayback(template1a, fake.user3));
    test.insert(buildTemplatePlayback(template1a, fake.user2));

    Collection<Template> result = testDAO.readAllPlaying(hubAccess);

    assertEquals(2L, result.size());
  }

  @Test
  public void readAllPlaying_noneOlderThanThreshold() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePlayback(template1a, fake.user2));
    test.insert(buildTemplatePlayback(template1a, fake.user3).createdAt(Date.from(Instant.now().minusSeconds(60 * 60 * 12))));

    Collection<Template> result = testDAO.readAllPlaying(hubAccess);

    assertEquals(1L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("cannons");


    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("io.xj.lib.util.ValueException: Account ID is required.", e.getMessage());
  }

  @Test
  public void update() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("cannons")
      .embedKey("embed5leaves")
      .accountId(fake.account1.getId());

    testDAO.update(hubAccess, template1a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void update_toProductionTypeChain_asAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("cannons")
      .type(TemplateType.PRODUCTION)
      .embedKey("embed5leaves")
      .accountId(fake.account1.getId());

    testDAO.update(hubAccess, template1a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals(TemplateType.PRODUCTION, result.getType());
  }

  @Test
  public void update_toProductionTypeChain_cannotWithoutAdmin() {
    HubAccess hubAccess = HubAccess.create(fake.user2, List.of(fake.account1),"User");
    Template inputData = new Template()
      .name("cannons")
      .type(TemplateType.PRODUCTION)
      .embedKey("embed5leaves")
      .accountId(fake.account1.getId());

    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Engineer role required", e.getMessage());
  }

  @Test
  public void update_cantHaveSameEmbedKeyAsExistingTemplate() throws HubException {
    HubAccess hubAccess = HubAccess.create("Admin");
    test.insert(buildTemplate(fake.account1, "Prior", UUID.randomUUID().toString()).embedKey("key55"));
    Template inputData = buildTemplate(fake.account1, "New", UUID.randomUUID().toString()).embedKey("key55");

    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Found Template with same Embed Key", e.getMessage());
  }

  @Test
  public void update_FailsWithoutName() {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .accountId(fake.account1.getId());


    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("io.xj.lib.util.ValueException: Name is required.", e.getMessage());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Template.
   */
  @Test
  public void update_asEngineer() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Template inputData = new Template()
      .name("cannons")
      .accountId(fake.account1.getId());

    testDAO.update(hubAccess, template1a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Template.
   */
  @Test
  public void update_asEngineer_failsWithoutAccountAccess() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(new Account()
      .id(UUID.randomUUID())), "Engineer");
    Template inputData = new Template()
      .name("cannons")
      .accountId(fake.account1.getId());


    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("cannons")
      .accountId(fake.account1.getId());

    try {
      testDAO.update(hubAccess, template1a.getId(), inputData);

    } catch (Exception e) {
      Template result = testDAO.readOne(HubAccess.internal(), template1a.getId());
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(template1a.getId(), result.getAccountId());
      assertSame(HubException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("cannons")
      .accountId(fake.account2.getId());

    testDAO.update(hubAccess, template2a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template2a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account2.getId(), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template()
      .name("trunk")
      .accountId(fake.account1.getId());

    testDAO.update(hubAccess, template1a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    testDAO.destroy(hubAccess, template1a.getId());

    try {
      testDAO.readOne(HubAccess.internal(), template1a.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void delete_okayEvenWithBindingsAndPlayback() throws Exception {
    test.insert(buildTemplateBinding(template1b, new Program().id(UUID.randomUUID())));
    test.insert(buildTemplatePlayback(template1a, new User().id(UUID.randomUUID())));
    HubAccess hubAccess = HubAccess.create("Admin");

    testDAO.destroy(hubAccess, template1a.getId());

    try {
      testDAO.readOne(HubAccess.internal(), template1a.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

}
