// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubException;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.*;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.tables.TemplateBinding.TEMPLATE_BINDING;
import static org.junit.Assert.*;

public class TemplateDAOImplTest {
  private TemplateDAO testDAO;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private Template template2a;
  private Template template1a;
  private Template template1b;

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

    // Account "palm tree" has template "leaves" and template "coconuts"
    fake.account1 = test.insert(buildAccount("palm tree"));
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));
    test.insert(buildAccountUser(fake.account1, fake.user2));
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
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
  public void create_shipKeyConvertedToLowercase() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setShipKey("dXUZhm");
    inputData.setAccountId(fake.account1.getId());

    Template result = testDAO.create(hubAccess, inputData);

    assertEquals("dxuzhm", result.getShipKey());
  }

  @Test
  public void create_shipKeyGeneratedLowercase() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Template result = testDAO.create(hubAccess, inputData);

    assertEquals(result.getShipKey(), result.getShipKey().toLowerCase(Locale.ROOT));
  }

  @Test
  public void create_hasDefaultTemplateConfig() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Template result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertTrue(result.getConfig().contains("deltaArcEnabled = true"));
  }

  @Test
  public void create_hasGeneratedShipKey() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Template result = testDAO.create(hubAccess, inputData);

    assertNotNull(result);
    assertEquals(9, result.getShipKey().length());
  }

  @Test
  public void create_cantHaveSameShipKeyAsExistingTemplate() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    test.insert(buildTemplate(fake.account1, "Prior", "key55"));
    Template inputData = buildTemplate(fake.account1, "New", "key55");

    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, inputData));
    assertEquals("Found Template with same Ship key", e.getMessage());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Template.
   */
  @Test
  public void create_asEngineer() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "Engineer");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());


    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, inputData));
    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void create_FailsWithoutAccountID() {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");


    var e = assertThrows(DAOException.class, () -> testDAO.create(hubAccess, inputData));
    assertEquals("Account ID is required.", e.getMessage());
  }

  @Test
  public void clone_includeBindings() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var boundLibrary = buildLibrary(fake.account1, "Test Library");
    var boundProgram = buildProgram(boundLibrary, ProgramType.Main, ProgramState.Published, "Test", "C", 120.0f, 0.6f);
    var boundInstrument = buildInstrument(boundLibrary, InstrumentType.Bass, InstrumentState.Published, "Test");
    Template inputData = new Template();
    inputData.setType(TemplateType.Preview);
    inputData.setAccountId(fake.account1.getId());
    inputData.setName("cannons fifty nine");
    test.insert(buildTemplateBinding(template1a, boundLibrary));
    test.insert(buildTemplateBinding(template1a, boundProgram));
    test.insert(buildTemplateBinding(template1a, boundInstrument));

    DAOCloner<Template> resultCloner = testDAO.clone(hubAccess, template1a.getId(), inputData);

    Template result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(TemplateType.Preview, result.getType());
    // Cloned TemplateBinding
    assertEquals(3, resultCloner.getChildClones().stream()
      .filter(e -> TemplateBinding.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(TEMPLATE_BINDING)
      .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(result.getId()))
      .and(TEMPLATE_BINDING.TARGET_ID.eq(boundLibrary.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(TEMPLATE_BINDING)
      .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(result.getId()))
      .and(TEMPLATE_BINDING.TARGET_ID.eq(boundProgram.getId()))
      .fetchOne(0, int.class));
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(TEMPLATE_BINDING)
      .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(result.getId()))
      .and(TEMPLATE_BINDING.TARGET_ID.eq(boundInstrument.getId()))
      .fetchOne(0, int.class));
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

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
  public void readChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1, fake.account2), "User");
    test.insert(buildTemplateBinding(template1a, buildProgram(buildLibrary(buildAccount("Test"), "test"), ProgramType.Detail, ProgramState.Published, "test", "C", 120.0f, 06f)));
    test.insert(buildTemplatePlayback(template1a, buildUser("Test", "test@test.com", "test.jpg", "User")));
    var legacy = buildTemplatePlayback(template1a, buildUser("Test2", "test2@test.com", "test2.jpg", "User"));
    legacy.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(legacy);

    Collection<Object> result = testDAO.readChildEntities(hubAccess, List.of(template1a.getId()), List.of("template-playbacks", "template-bindings"));

    assertEquals(2L, result.size());
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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User");

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
    var later = buildTemplatePlayback(template1a, fake.user3);
    later.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 12)).toLocalDateTime());
    test.insert(later);

    Collection<Template> result = testDAO.readAllPlaying(hubAccess);

    assertEquals(1L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");


    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Account ID is required.", e.getMessage());
  }

  @Test
  public void update() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setShipKey("embed5leaves");
    inputData.setAccountId(fake.account1.getId());

    testDAO.update(hubAccess, template1a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void update_toProductionTypeChain_asAdmin() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setType(TemplateType.Production);
    inputData.setShipKey("embed5leaves");
    inputData.setAccountId(fake.account1.getId());

    testDAO.update(hubAccess, template1a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals(TemplateType.Production, result.getType());
  }

  @Test
  public void update_toProductionTypeChain_cannotWithoutAdmin() {
    HubAccess hubAccess = HubAccess.create(fake.user3, List.of(fake.account1));
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setType(TemplateType.Production);
    inputData.setShipKey("embed5leaves");
    inputData.setAccountId(fake.account1.getId());

    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Engineer role is required", e.getMessage());
  }

  @Test
  public void update_cantHaveSameShipKeyAsExistingTemplate() throws HubException {
    HubAccess hubAccess = HubAccess.create("Admin");
    test.insert(buildTemplate(fake.account1, "Prior", "key55"));
    Template inputData = buildTemplate(fake.account1, "New", "key55");

    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Found Template with same Ship key", e.getMessage());
  }

  @Test
  public void update_FailsWithoutName() {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setAccountId(fake.account1.getId());


    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Name is required.", e.getMessage());
  }

  /**
   [#155089641] Engineer expects to be able to of and update a Template.
   */
  @Test
  public void update_asEngineer() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Engineer");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());

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
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "Engineer");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());


    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, template1a.getId(), inputData));
    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());

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
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account2.getId());

    testDAO.update(hubAccess, template2a.getId(), inputData);

    Template result = testDAO.readOne(HubAccess.internal(), template2a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account2.getId(), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("trunk");
    inputData.setAccountId(fake.account1.getId());

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
    test.insert(buildTemplateBinding(template1b, buildProgram(buildLibrary(buildAccount("Test"), "test"), ProgramType.Detail, ProgramState.Published, "test", "C", 120.0f, 06f)));
    test.insert(buildTemplatePlayback(template1a, buildUser("Test", "test@test.com", "test.jpg", "User")));
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
