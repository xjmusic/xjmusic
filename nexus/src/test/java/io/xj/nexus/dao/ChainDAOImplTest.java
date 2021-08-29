// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.api.Template;
import io.xj.api.TemplateType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.util.Value;
import io.xj.nexus.NexusTestConfiguration;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildHubClientAccess;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildLibrary;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("HttpUrlsUsage")
@RunWith(MockitoJUnitRunner.class)
public class ChainDAOImplTest {
  private NexusEntityStore test;
  private ChainDAO subject;
  private Account account1;
  private Chain chain1;
  private Chain chain2;
  private Template template1;
  private Template template2;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new NexusDAOModule(), new EntityModule(), new NexusEntityStoreModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Config.class).toInstance(config);
          bind(Environment.class).toInstance(env);
        }
      }));
    var entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // hub entities as basis
    account1 = buildAccount("fish");
    buildLibrary(account1, "test");
    template1 = buildTemplate(account1, "Test Template 1", "test1");
    template2 = buildTemplate(account1, "Test Template 2", "test2");

    // Payload comprising Nexus entities
    chain1 = test.put(buildChain(account1, "school", TemplateType.PRODUCTION, ChainState.READY, template1,
      Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    chain2 = test.put(buildChain(account1, "bucket", TemplateType.PRODUCTION, ChainState.FABRICATE, template2,
      Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    // Instantiate the test subject
    subject = injector.getInstance(ChainDAO.class);
  }

  /**
   [#176285826] Nexus bootstraps Chains from JSON file on startup
   */
  @Test
  public void bootstrap() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.DRAFT)
      .type(TemplateType.PRODUCTION);

    var result = subject.bootstrap(access, TemplateType.PRODUCTION, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
  }

  @Test
  public void create() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.DRAFT)
      .type(TemplateType.PRODUCTION)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void create_typeNotSpecified_defaultsToPreview() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.DRAFT)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(TemplateType.PREVIEW, result.getType());
  }

  @Test
  public void create_withEmbedKey() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.DRAFT)
      .embedKey("my $% favorite THINGS")
      .type(TemplateType.PRODUCTION)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("my_favorite_things", result.getEmbedKey());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void create_withEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    var first = buildChain(account1, "bucket", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    var second = buildChain(account1, "bucket", TemplateType.PRODUCTION, ChainState.FABRICATE, template2, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    HubClientAccess access = buildHubClientAccess("Admin");
    subject.create(access, first);

    Exception thrown = assertThrows(DAOValidationException.class, () -> subject.create(access, second));

    assertEquals("Chain already exists with embed key 'my_favorite_things'!", thrown.getMessage());
  }

  @Test
  public void create_PreviewType() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.DRAFT)
      .type(TemplateType.PREVIEW)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(28, Objects.requireNonNull(result.getEmbedKey()).length());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals(TemplateType.PREVIEW, result.getType());
  }


  @Test
  public void create_PreviewType_asArtist() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "Artist");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .startAt("2009-08-12T12:17:02.527142Z")
      .state(ChainState.DRAFT)
      .type(TemplateType.PREVIEW)
      .name("coconuts");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals(TemplateType.PREVIEW, result.getType());
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_createdInDraftState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.DRAFT)
      .startAt("2009-08-12T12:17:02.527142Z");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.DRAFT)
      .type(TemplateType.PRODUCTION)
      .startAt("2009-08-12T12:17:02.527142Z");

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_FailsWithoutAccountID() {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .name("coconuts")
      .state(ChainState.DRAFT)
      .type(TemplateType.PRODUCTION)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.create(access, input));

    assertEquals("Account ID is required.", thrown.getMessage());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(new Account()
      .id(UUID.randomUUID())
    ), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.readOne(access, chain1.getId()));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    var result = subject.readOne(access, chain2.getId());

    assertNotNull(result);
    assertEquals(chain2.getId(), result.getId());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("bucket", result.getName());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt());
  }

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.
   */
  @Test
  public void readOne_byEmbedKey_unauthenticatedOk() throws Exception {
    var chain = subject.create(HubClientAccess.internal(), buildChain(account1, "cats test", TemplateType.PRODUCTION, ChainState.DRAFT, template1, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "cats"));
    HubClientAccess access = HubClientAccess.unauthenticated();

    var result = subject.readOneByEmbedKey(access, "cats");

    assertNotNull(result);
    assertEquals(chain.getId(), result.getId());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("cats test", result.getName());
    assertEquals(ChainState.DRAFT, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt());
    assertEquals("cats", result.getEmbedKey());
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(new Account()
      .id(UUID.randomUUID())
    ), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.readOne(access, chain1.getId()));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAllFabricating() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "Admin");
    test.put(buildChain(template2).state(ChainState.FABRICATE));
    test.put(buildChain(template2).state(ChainState.FABRICATE));
    test.put(buildChain(template2).state(ChainState.FABRICATE));
    test.put(buildChain(template2).state(ChainState.DRAFT));
    test.put(buildChain(template2).state(ChainState.COMPLETE));
    test.put(buildChain(template2).state(ChainState.READY));
    test.put(buildChain(template2).state(ChainState.FAILED));

    Collection<Chain> result = subject.readAllFabricating(access);

    assertEquals(4L, result.size());
  }

  @Test
  public void readAllFabricating_requiresTopLevelAccess() {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist");

    var e = assertThrows(DAOPrivilegeException.class, () -> subject.readAllFabricating(access));
    assertEquals("top-level access is required.", e.getMessage());
  }

  @Test
  public void readMany_excludesChainsInFabricateState() throws Exception {
    buildChain(account1, "sham", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null);
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readManyInState() throws Exception {
    Collection<Chain> result = subject.readManyInState(HubClientAccess.internal(), ChainState.FABRICATE);

    assertNotNull(result);
    assertEquals(1L, result.size());
    var result0 = result.iterator().next();

    assertEquals("bucket", result0.getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(new Account()
      .id(UUID.randomUUID())
    ), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.readMany(access, ImmutableList.of(account1.getId())));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  @Test
  public void update() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.COMPLETE)
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.COMPLETE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_cantChangeEndOfPreviewChain() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var previewChain = test.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PREVIEW)
      .state(ChainState.FABRICATE)
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-11T12:17:01.989941Z"));
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PREVIEW)
      .state(ChainState.FABRICATE)
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-12T12:17:01.989941Z");

    subject.update(access, previewChain.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), previewChain.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals(TemplateType.PREVIEW, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_addEmbedKey() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PRODUCTION)
      .embedKey("twenty %$** four HOURS")
      .state(ChainState.COMPLETE)
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("twenty_four_hours", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.COMPLETE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_removeEmbedKey() throws Exception {
    var chain3 = test.put(buildChain(account1, "bucket", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .embedKey(null)
      .type(TemplateType.PRODUCTION)
      .state(ChainState.COMPLETE)
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain3.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain3.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertNull(result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.COMPLETE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_addEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    test.put(buildChain(account1, "bucket", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PRODUCTION)
      .embedKey("twenty_four_hours")
      .state(ChainState.COMPLETE)
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-11T12:17:01.989941Z");

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Chain already exists with embed key 'twenty_four_hours'!", thrown.getMessage());
  }

  @Test
  public void update_okayWithEmbedKey() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var chain = test.put(buildChain(account1, "school", TemplateType.PRODUCTION, ChainState.READY, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.READY)
      .embedKey("jabberwocky")
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-11T12:17:01.989941Z");

    subject.update(access, chain.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.READY, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }


  @Test
  public void update_cannotChangeType() {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PREVIEW)
      .state(ChainState.COMPLETE)
      .startAt("2009-08-12T12:17:02.687327Z")
      .stopAt("2009-09-11T12:17:01.989941Z");

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Cannot modify Chain Type", thrown.getMessage());

  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    test.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain2.getId())
      .offset(5L)
      .state(SegmentState.CRAFTED)
      .key("A major")
      .total(64)
      .density(0.52)
      .tempo(120.0)
      .beginAt(Value.formatIso8601UTC(Instant.now()))
      .type(SegmentType.NEXTMAIN)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("bucket")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2015-05-10T12:17:03.527142Z")
      .stopAt("2015-06-09T12:17:01.047563Z");

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("cannot change chain startAt time after it has segments", thrown.getMessage());

  }

  @Test
  public void update_canChangeName_whenChainsHasSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    test.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .offset(5L)
      .state(SegmentState.CRAFTED)
      .key("A major")
      .total(64)
      .density(0.52)
      .tempo(120.0)
      .beginAt(Value.formatIso8601UTC(Instant.now()))
      .type(SegmentType.NEXTMAIN)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")

    );
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.FABRICATE)
      .type(TemplateType.PRODUCTION)
      .startAt("2015-05-10T12:17:02.527142Z")
      .stopAt("2015-06-09T12:17:01.047563Z");

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.COMPLETE)
      .startAt("2009-08-12T12:17:02.687327Z");

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.COMPLETE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertNull(result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .name("coconuts")
      .state(ChainState.FABRICATE)
      .type(TemplateType.PRODUCTION)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Account ID is required.", thrown.getMessage());

  }

  @Test
  public void update_FailsWithoutName() {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .state(ChainState.DRAFT)
      .type(TemplateType.PRODUCTION)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Name is required.", thrown.getMessage());

  }

  @Test
  public void update_CannotChangeAccount() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var input = new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("coconuts")
      .state(ChainState.COMPLETE)
      .type(TemplateType.PRODUCTION)
      .startAt("2009-08-12T12:17:02.527142Z")
      .stopAt("2009-09-11T12:17:01.047563Z");

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");

    subject.updateState(access, chain2.getId(), ChainState.COMPLETE);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(ChainState.COMPLETE, result.getState());
  }

  @Test
  public void updateState_WithoutAccountAccess_Fails() {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(new Account()
      .id(UUID.randomUUID())
    ), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.updateState(access, chain2.getId(), ChainState.COMPLETE));

    assertEquals("Account access is required.", thrown.getMessage());

  }

  @Test
  public void updateState_WithAccountAccess() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    subject.updateState(access, chain2.getId(), ChainState.COMPLETE);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(ChainState.COMPLETE, result.getState());
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutEngineerRole_ForProductionChain() {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.updateState(access, chain2.getId(), ChainState.COMPLETE));

    assertEquals("Engineer role is required.", thrown.getMessage());

  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User");
    var chain3 = test.put(buildChain(account1, "bucket", TemplateType.PREVIEW, ChainState.FABRICATE, template1, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.updateState(access, chain3.getId(), ChainState.COMPLETE));

    assertEquals("Engineer/Artist role is required.", thrown.getMessage());
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   [#170273871] Revived chain should always start now
   */
  @Test
  public void revive() throws Exception {
    var account2 = buildAccount();
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1, account2), "User,Admin,Artist,Engineer");

    var chain = test.put(buildChain(account1, "school", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));

    var result = subject.revive(access, chain.getId(), "Testing");

    assertNotNull(result);
    assertTrue(1000 > Instant.now().toEpochMilli() - Instant.parse(result.getStartAt()).toEpochMilli()); // [#170273871] Revived chain should always start now
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());

    var priorChain = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(priorChain);
    assertEquals(ChainState.FAILED, priorChain.getState());
    assertNull(priorChain.getEmbedKey());
//  FUTURE assert for real message sent about work  org.junit.Assert.assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readManyPreviousDays(HubClientAccess.internal(), 1).size());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require existing to be revived, throws error if not found.
   */
  @Test
  public void revive_failsIfNotExistPriorChain() {
    HubClientAccess access = buildHubClientAccess("Admin");

    Exception thrown = assertThrows(DAOExistenceException.class, () ->
      subject.revive(access, UUID.randomUUID(), "Testing"));

    assertTrue(thrown.getMessage().contains("does not exist"));
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#174898524] Artist can revive a Chain of any type
   */
  @Test
  public void revive_okOfAnyType() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var chain = test.put(buildChain(account1, "school", TemplateType.PREVIEW, ChainState.FAILED, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));

    subject.revive(access, chain.getId(), "Testing");
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#175137186] Artist can only revive Chain in fabricate, failed, or completed state
   */
  @Test
  public void revive_failsInDraftState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var chain = test.put(buildChain(account1, "school", TemplateType.PREVIEW, ChainState.DRAFT, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.revive(access, chain.getId(), "Testing"));

    assertEquals("Can't revive a Chain unless it's in Fabricate, Complete, or Failed state", thrown.getMessage());
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#175137186] Artist can only revive Chain in fabricate, failed, or completed state
   */
  @Test
  public void revive_failsInReadyState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    var chain = test.put(buildChain(account1, "school", TemplateType.PREVIEW, ChainState.READY, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.revive(access, chain.getId(), "Testing"));

    assertEquals("Can't revive a Chain unless it's in Fabricate, Complete, or Failed state", thrown.getMessage());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_okayWithEngineerAccess() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "Engineer");
    var chain = test.put(buildChain(account1, "school", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));

    var result = subject.revive(access, chain.getId(), "Testing");

    assertNotNull(result);
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals(TemplateType.PRODUCTION, result.getType());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_failsWithArtistAccess() throws Exception {
    HubClientAccess access = buildHubClientAccess("Artist");
    var chain = test.put(buildChain(account1, "school", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.revive(access, chain.getId(), "Testing"));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .offset(5L)
      .state(SegmentState.CRAFTING)
      .key("E minor")
      .total(64)
      .density(0.41)
      .type(SegmentType.NEXTMAIN)
      .beginAt("2014-02-14T12:03:40.000001Z")
      .endAt("2014-02-14T12:04:10.000001Z")
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T11:53:40.000001Z")).orElseThrow();

    assertEquals(chain1.getId(), result.getChainId());
    assertEquals(Long.valueOf(6), result.getOffset());
    assertEquals("2014-02-14T12:04:10.000001Z", result.getBeginAt());
    assertEquals(SegmentType.PENDING, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", TemplateType.PRODUCTION, ChainState.FABRICATE, template2, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .offset(5L)
      .type(SegmentType.NEXTMAIN)
      .beginAt("2014-02-14T12:03:40.000001Z")
      .endAt("2014-02-14T12:03:50.000001Z")
      .state(SegmentState.CRAFTING)
      .key("E minor")
      .total(64)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T13:53:50.000001Z")).orElseThrow();

    var resultChain = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultChain);
    assertEquals(ChainState.FABRICATE, resultChain.getState());
    assertEquals(SegmentType.PENDING, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", TemplateType.PRODUCTION, ChainState.FABRICATE, template2, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .type(SegmentType.NEXTMAIN)
      .beginAt("2014-02-14T14:03:35.000001Z")
      .endAt("2014-02-14T14:03:55.000001Z")
      .offset(5L)
      .state(SegmentState.DUBBED)
      .key("E minor")
      .total(64)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));

    assertTrue(result.isEmpty());
    var resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.COMPLETE, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .beginAt("2014-02-14T14:03:15.000001Z")
      .endAt("2014-02-14T14:03:25.000001Z")
      .offset(5L)
      .type(SegmentType.NEXTMAIN)
      .state(SegmentState.DUBBED)
      .key("E minor")
      .total(64)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z")).orElseThrow();

    var resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.FABRICATE, resultFinal.getState());
    assertEquals(SegmentType.PENDING, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsAlreadyHasNextSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .beginAt("2014-02-14T14:03:15.000001Z")
      .endAt("2014-02-14T14:05:40.000001Z") // after chain stop-at, that's what triggers the chain to be completed
      .offset(5L)
      .type(SegmentType.NEXTMAIN)
      .state(SegmentState.DUBBED)
      .key("E minor")
      .total(64)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertTrue(result.isEmpty());
  }

  @Test
  public void buildNextSegmentOrComplete_chainEndingInCraftedSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    var fromChain = test.put(buildChain(account1, "Test Print #2", TemplateType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    test.put(buildSegment(fromChain, 5, SegmentState.CRAFTED, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T14:03:38.000001Z"), "A major", 64, 0.52, 120.0, "chains-1-segments-9f7s89d8a7892.wav", "OGG"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z")).orElseThrow();

    assertEquals(fromChain.getId(), result.getChainId());
    assertEquals(Long.valueOf(6), result.getOffset());
    assertEquals("2014-08-12T14:03:38.000001Z", result.getBeginAt());
  }

  @Test
  public void buildNextSegmentOrComplete_newEmptyChain() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    test.put(buildChain(account1, "Test Print #2", TemplateType.PRODUCTION, ChainState.READY, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    var fromChain = test.put(new Chain()
      .id(UUID.randomUUID())
      .type(TemplateType.PRODUCTION)
      .startAt("2014-08-12T12:17:02.527142Z"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z")).orElseThrow();

    assertEquals(fromChain.getId(), result.getChainId());
    assertEquals(Long.valueOf(0), result.getOffset());
    assertEquals("2014-08-12T12:17:02.527142Z", result.getBeginAt());
  }

  @Test
  public void destroy() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");

    subject.destroy(access, chain1.getId());

    try {
      subject.readOne(HubClientAccess.internal(), chain1.getId());
      fail();
    } catch (DAOExistenceException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_SucceedsEvenWithChildren() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    buildLibrary(account1, "nerds");

    try {
      subject.destroy(access, chain1.getId());

    } catch (Exception e) {
      var result = subject.readOne(HubClientAccess.internal(), chain1.getId());
      assertNotNull(result);
      throw e;
    }
  }

}
