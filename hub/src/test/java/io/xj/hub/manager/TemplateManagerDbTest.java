// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubException;
import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.app.AppEnvironment;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrument;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplatePlayback;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.tables.TemplateBinding.TEMPLATE_BINDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TemplateManagerDbTest {
  @Mock
  private PreviewNexusAdmin previewNexusAdmin;
  private TemplateManager testManager;
  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;
  private Template template2a;
  private Template template1a;
  private Template template1b;
  private TemplatePlaybackManager templatePlaybackManager;

  @BeforeEach
  public void setUp() throws Exception {
    var env = AppEnvironment.getDefault();
    test = HubIntegrationTestFactory.build(env);
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
    TemplateBindingManager templateBindingManager = new TemplateBindingManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    templatePlaybackManager = new TemplatePlaybackManagerImpl(test.getEnv(), test.getEntityFactory(), test.getSqlStoreProvider(), previewNexusAdmin);
    TemplatePublicationManager templatePublicationManager = new TemplatePublicationManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    testManager = new TemplateManagerImpl(test.getEnv(), test.getEntityFactory(), test.getSqlStoreProvider(), templateBindingManager, templatePlaybackManager, templatePublicationManager);
  }

  @AfterEach
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create_shipKeyConvertedToLowercase() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setShipKey("dX_UZ_hm");
    inputData.setAccountId(fake.account1.getId());

    Template result = testManager.create(access, inputData);

    assertEquals("dx_uz_hm", result.getShipKey());
  }

  @Test
  public void create_shipKeyGeneratedLowercase() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Template result = testManager.create(access, inputData);

    assertEquals(result.getShipKey(), result.getShipKey().toLowerCase(Locale.ROOT));
  }

  @Test
  public void create_hasDefaultTemplateConfig() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Template result = testManager.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertTrue(result.getConfig().contains("deltaArcEnabled = true"));
  }

  @Test
  public void create_hasGeneratedShipKey() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Template result = testManager.create(access, inputData);

    assertNotNull(result);
    assertEquals(9, result.getShipKey().length());
  }

  @Test
  public void create_cantHaveSameShipKeyAsExistingTemplate() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    test.insert(buildTemplate(fake.account1, "Prior", "key55"));
    Template inputData = buildTemplate(fake.account1, "New", "key55");

    var e = assertThrows(ManagerException.class, () -> testManager.create(access, inputData));
    assertEquals("Found Template with same Ship key", e.getMessage());
  }

  /**
   * Engineer expects to be able to of and update a Template. https://www.pivotaltracker.com/story/show/155089641
   */
  @Test
  public void create_asEngineer() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Engineer");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());

    Template result = testManager.create(
      access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  /**
   * Engineer expects to be able to of and update a Template. https://www.pivotaltracker.com/story/show/155089641
   */
  @Test
  public void create_asEngineer_failsWithoutAccountAccess() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "Engineer");
    Template inputData = new Template();
    inputData.setName("coconuts");
    inputData.setAccountId(fake.account1.getId());


    var e = assertThrows(ManagerException.class, () -> testManager.create(access, inputData));
    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void create_FailsWithoutAccountID() {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("coconuts");


    var e = assertThrows(ManagerException.class, () -> testManager.create(access, inputData));
    assertEquals("Account ID is required.", e.getMessage());
  }

  @Test
  public void clone_includeBindings() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    var boundLibrary = buildLibrary(fake.account1, "Test Library");
    var boundProgram = buildProgram(boundLibrary, ProgramType.Main, ProgramState.Published, "Test", "C", 120.0f, 0.6f);
    var boundInstrument = buildInstrument(boundLibrary, InstrumentType.Bass, InstrumentMode.Event, InstrumentState.Published, "Test");
    Template inputData = new Template();
    inputData.setType(TemplateType.Preview);
    inputData.setAccountId(fake.account1.getId());
    inputData.setName("cannons fifty nine");
    test.insert(buildTemplateBinding(template1a, boundLibrary));
    test.insert(buildTemplateBinding(template1a, boundProgram));
    test.insert(buildTemplateBinding(template1a, boundInstrument));

    ManagerCloner<Template> resultCloner = testManager.clone(access, template1a.getId(), inputData);

    Template result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertNotEquals(template1a.getShipKey(), result.getShipKey());
    assertEquals(TemplateType.Preview, result.getType());
    // Cloned TemplateBinding
    assertEquals(3, resultCloner.getChildClones().stream()
      .filter(e -> TemplateBinding.class.equals(e.getClass())).count());
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1),
        selectCount.from(TEMPLATE_BINDING)
          .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(result.getId()))
          .and(TEMPLATE_BINDING.TARGET_ID.eq(boundLibrary.getId()))
          .fetchOne(0, int.class));
    }
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1),
        selectCount.from(TEMPLATE_BINDING)
          .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(result.getId()))
          .and(TEMPLATE_BINDING.TARGET_ID.eq(boundProgram.getId()))
          .fetchOne(0, int.class));
    }
    try (var selectCount = test.getDSL().selectCount()) {
      assertEquals(Integer.valueOf(1),
        selectCount.from(TEMPLATE_BINDING)
          .where(TEMPLATE_BINDING.TEMPLATE_ID.eq(result.getId()))
          .and(TEMPLATE_BINDING.TARGET_ID.eq(boundInstrument.getId()))
          .fetchOne(0, int.class));
    }
  }

  /**
   * Lab cloned template is always Preview-type and has new ship key if unspecified https://www.pivotaltracker.com/story/show/181054239
   */
  @Test
  public void clone_alwaysPreviewWithNewShipKey() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Template inputData = new Template();
    inputData.setType(TemplateType.Production);
    inputData.setAccountId(fake.account1.getId());
    inputData.setName("cannons fifty nine");

    ManagerCloner<Template> resultCloner = testManager.clone(access, template1a.getId(), inputData);

    Template result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals("cannons fifty nine", result.getName());
    assertEquals("embed5leaves2", result.getShipKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(TemplateType.Preview, result.getType());
  }

  /**
   * Lab cloned template has specified ship key https://www.pivotaltracker.com/story/show/181054239
   */
  @Test
  public void clone_hasSpecifiedShipKey() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, UUID.randomUUID(), ImmutableList.of(fake.account1));
    Template inputData = new Template();
    inputData.setType(TemplateType.Production);
    inputData.setShipKey("new2ship5key");
    inputData.setAccountId(fake.account1.getId());
    inputData.setName("cannons fifty nine");

    ManagerCloner<Template> resultCloner = testManager.clone(access, template1a.getId(), inputData);

    Template result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals("cannons fifty nine", result.getName());
    assertEquals("new2ship5key", result.getShipKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(TemplateType.Preview, result.getType());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    Template result = testManager.readOne(access, template1b.getId());

    assertNotNull(result);
    assertEquals(template1b.getId(), result.getId());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User");

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(access, fake.account1.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User");

    Collection<Template> result = testManager.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
    Iterator<Template> resultIt = result.iterator();
    assertEquals("leaves", resultIt.next().getName());
    assertEquals("coconuts", resultIt.next().getName());
  }

  @Test
  public void readChildEntities() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1, fake.account2), "User");
    test.insert(buildTemplateBinding(template1a, buildProgram(buildLibrary(buildAccount("Test"), "test"), ProgramType.Detail, ProgramState.Published, "test", "C", 120.0f, 06f)));
    test.insert(buildTemplatePlayback(template1a, buildUser("Test", "test@test.com", "test.jpg", "User")));
    var legacy = buildTemplatePlayback(template1a, buildUser("Test2", "test2@test.com", "test2.jpg", "User"));
    legacy.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(legacy);

    Collection<Object> result = testManager.readChildEntities(access, List.of(template1a.getId()), List.of("template-playbacks", "template-bindings"));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_fromAllAccounts() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1, fake.account2), "User");

    Collection<Template> result = testManager.readMany(access, Lists.newArrayList());

    assertEquals(4L, result.size());
    Iterator<Template> it = result.iterator();
    assertEquals("leaves", it.next().getName());
    assertEquals("coconuts", it.next().getName());
    assertEquals("helm", it.next().getName());
    assertEquals("sail", it.next().getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "User");

    Collection<Template> result = testManager.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(0L, result.size());
  }


  @Test
  public void readAllPlaying() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePlayback(template1a, fake.user3));
    test.insert(buildTemplatePlayback(template1a, fake.user2));

    Collection<Template> result = testManager.readAllPlaying(access);

    assertEquals(2L, result.size());
  }

  @Test
  public void readAllPlaying_noneOlderThanThreshold() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePlayback(template1a, fake.user2));
    var later = buildTemplatePlayback(template1a, fake.user3);
    later.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 12)).toLocalDateTime());
    test.insert(later);

    Collection<Template> result = testManager.readAllPlaying(access);

    assertEquals(1L, result.size());
  }

  @Test
  public void update_FailsWithoutAccountID() {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");


    var e = assertThrows(ManagerException.class, () -> testManager.update(access, template1a.getId(), inputData));
    assertEquals("Account ID is required.", e.getMessage());
  }

  @Test
  public void update() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setShipKey("embed5leaves");
    inputData.setAccountId(fake.account1.getId());

    testManager.update(access, template1a.getId(), inputData);

    Template result = testManager.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void update_toProductionTypeChain_asAdmin() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setType(TemplateType.Production);
    inputData.setShipKey("embed5leaves");
    inputData.setAccountId(fake.account1.getId());

    testManager.update(access, template1a.getId(), inputData);

    Template result = testManager.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals(TemplateType.Production, result.getType());
  }

  @Test
  public void update_toProductionTypeChain_cannotWithoutAdmin() {
    HubAccess access = HubAccess.create(fake.user3, List.of(fake.account1));
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setType(TemplateType.Production);
    inputData.setShipKey("embed5leaves");
    inputData.setAccountId(fake.account1.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.update(access, template1a.getId(), inputData));
    assertEquals("Engineer role is required", e.getMessage());
  }

  @Test
  public void update_cantHaveSameShipKeyAsExistingTemplate() throws HubException {
    HubAccess access = HubAccess.create("Admin");
    test.insert(buildTemplate(fake.account1, "Prior", "key55"));
    Template inputData = buildTemplate(fake.account1, "New", "key55");

    var e = assertThrows(ManagerException.class, () -> testManager.update(access, template1a.getId(), inputData));
    assertEquals("Found Template with same Ship key", e.getMessage());
  }

  @Test
  public void update_FailsWithoutName() {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setAccountId(fake.account1.getId());


    var e = assertThrows(ManagerException.class, () -> testManager.update(access, template1a.getId(), inputData));
    assertEquals("Name is required.", e.getMessage());
  }

  /**
   * Engineer expects to be able to of and update a Template. https://www.pivotaltracker.com/story/show/155089641
   */
  @Test
  public void update_asEngineer() throws Exception {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "Engineer");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());

    testManager.update(access, template1a.getId(), inputData);

    Template result = testManager.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  /**
   * Engineer expects to be able to of and update a Template. https://www.pivotaltracker.com/story/show/155089641
   */
  @Test
  public void update_asEngineer_failsWithoutAccountAccess() {
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(buildAccount("Testing")), "Engineer");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account1.getId());


    var e = assertThrows(ManagerException.class, () -> testManager.update(access, template1a.getId(), inputData));
    assertEquals("Account does not exist", e.getMessage());
  }

  @Test
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(UUID.randomUUID());

    assertThrows(ManagerException.class, () -> testManager.update(access, template1a.getId(), inputData));

    Template result = testManager.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setAccountId(fake.account2.getId());

    testManager.update(access, template2a.getId(), inputData);

    Template result = testManager.readOne(HubAccess.internal(), template2a.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.account2.getId(), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("trunk");
    inputData.setAccountId(fake.account1.getId());

    testManager.update(access, template1a.getId(), inputData);

    Template result = testManager.readOne(HubAccess.internal(), template1a.getId());
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void update_toProductionTemplateDeletesPlaybacks() throws Exception {
    HubAccess access = HubAccess.create("Admin");
    Template inputData = new Template();
    inputData.setName("cannons");
    inputData.setType(TemplateType.Production);
    inputData.setShipKey("embed5leaves");
    inputData.setAccountId(fake.account1.getId());
    TemplatePlayback playback = test.insert(buildTemplatePlayback(template1a, fake.user2));

    testManager.update(access, template1a.getId(), inputData);

    TemplatePlayback result = templatePlaybackManager.readOne(HubAccess.internal(), playback.getId());
    assertNull(result);
  }

  @Test
  public void delete() throws Exception {
    HubAccess access = HubAccess.create("Admin");

    testManager.destroy(access, template1a.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(HubAccess.internal(), template1a.getId()));
    assertTrue(e.getMessage().contains("does not exist"), "Record should not exist");
  }

  @Test
  public void delete_okayEvenWithBindingsAndPlayback() throws Exception {
    test.insert(buildTemplateBinding(template1b, buildProgram(buildLibrary(buildAccount("Test"), "test"), ProgramType.Detail, ProgramState.Published, "test", "C", 120.0f, 06f)));
    test.insert(buildTemplatePlayback(template1a, buildUser("Test", "test@test.com", "test.jpg", "User")));
    HubAccess access = HubAccess.create("Admin");

    testManager.destroy(access, template1a.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(HubAccess.internal(), template1a.getId()));
    assertTrue(e.getMessage().contains("does not exist"), "Record should not exist");
  }

  /**
   * Artist should be able to delete Preview Templates https://www.pivotaltracker.com/story/show/181227134
   *
   * @throws Exception on failure
   */
  @Test
  public void delete_artistHasPermissionForPreviewTemplate() throws Exception {
    test.insert(buildTemplateBinding(template1b, buildProgram(buildLibrary(buildAccount("Test"), "test"), ProgramType.Detail, ProgramState.Published, "test", "C", 120.0f, 06f)));
    test.insert(buildTemplatePlayback(template1a, buildUser("Test", "test@test.com", "test.jpg", "User")));
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User,Artist");

    testManager.destroy(access, template1a.getId());

    var e = assertThrows(ManagerException.class, () -> testManager.readOne(HubAccess.internal(), template1a.getId()));
    assertTrue(e.getMessage().contains("does not exist"), "Record should not exist");
  }

  /**
   * Artist should NOT be able to delete Production Templates https://www.pivotaltracker.com/story/show/181227134
   *
   * @throws Exception on failure
   */
  @Test
  public void delete_artistCannotDeleteProductionTemplate() throws Exception {
    var productionTemplate = test.insert(buildTemplate(fake.account1, TemplateType.Production, "can't touch this", "no_touching"));
    HubAccess access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(fake.account1), "User,Artist");

    var e = assertThrows(ManagerException.class, () -> testManager.destroy(access, productionTemplate.getId()));
    assertEquals("top-level access is required", e.getMessage());
  }

  /**
   * Preview Nexus reads one currently-playing preview template for the authenticated user
   * https://www.pivotaltracker.com/story/show/183576743
   */
  @Test
  public void readOnePlayingForUser() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Admin");
    test.insert(buildTemplatePlayback(template1a, fake.user2));

    Optional<Template> result = testManager.readOnePlayingForUser(access, fake.user2.getId());

    assertTrue(result.isPresent());
  }

  /**
   * Preview Nexus deactivates for the authenticated user when no template is playing
   * https://www.pivotaltracker.com/story/show/183576743
   */
  @Test
  public void readOnePlayingForUser_noneIfNonePlaying() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Admin");

    Optional<Template> result = testManager.readOnePlayingForUser(access, fake.user2.getId());

    assertTrue(result.isEmpty());
  }

  /**
   * Preview Nexus deactivates for the authenticated user when template playback is expired
   * https://www.pivotaltracker.com/story/show/183576743
   */
  @Test
  public void readOnePlayingForUser_noneIfPlaybackExpired() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Admin");
    var playback = buildTemplatePlayback(template1a, fake.user2);
    playback.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(playback);

    Optional<Template> result = testManager.readOnePlayingForUser(access, fake.user2.getId());

    assertTrue(result.isEmpty());
  }

  /**
   * Preview Nexus deactivates for the authenticated user when template playback is expired
   * https://www.pivotaltracker.com/story/show/183576743
   */
  @Test
  public void readOnePlayingForUser_byInternalAccess() throws Exception {
    HubAccess access = HubAccess.internal();
    var playback = buildTemplatePlayback(template1a, fake.user2);
    playback.setCreatedAt(Timestamp.from(Instant.now().minusSeconds(60 * 60 * 24)).toLocalDateTime());
    test.insert(playback);

    Optional<Template> result = testManager.readOnePlayingForUser(access, fake.user2.getId());

    assertTrue(result.isEmpty());
  }
}
