// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.Library;
import io.xj.Program;
import io.xj.Segment;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.util.Value;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@SuppressWarnings("HttpUrlsUsage")
@RunWith(MockitoJUnitRunner.class)
public class ChainDAOImplTest {

  @Mock
  HubClient hubClient;
  private NexusEntityStore test;
  private ChainDAO subject;
  private Account account1;
  private Library library1;
  private Library library2;
  private Chain chain1;
  private Chain chain2;
  private HubContent hubContent;
  private ChainBindingDAO chainBindingDAO;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = AppConfiguration.inject(config, env, ImmutableSet.of(new NexusDAOModule()));
    chainBindingDAO = injector.getInstance(ChainBindingDAO.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // hub entities as basis
    account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("fish")
      .build();
    library1 = NexusIntegrationTestingFixtures.makeLibrary(account1, "test");
    library2 = NexusIntegrationTestingFixtures.makeLibrary(account1, "test");
    NexusIntegrationTestingFixtures.makeUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    Program program1 = NexusIntegrationTestingFixtures.makeProgram(library2, Program.Type.Rhythm, Program.State.Published, "fonds", "C#", 0.286, 0.6);
    var sequence1 = NexusIntegrationTestingFixtures.makeSequence(program1, 16, "epic beat part 1", 0.342, "C#", 0.286);
    var binding1_0 = NexusIntegrationTestingFixtures.makeBinding(sequence1, 0);
    hubContent = new HubContent(ImmutableSet.of(
      program1,
      sequence1,
      binding1_0
    ));

    // Payload comprising Nexus entities
    chain1 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Production, Chain.State.Ready,
      Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    chain2 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate,
      Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    // Instantiate the test subject
    subject = injector.getInstance(ChainDAO.class);
  }

  /**
   [#176285826] Nexus bootstraps Chains from JSON file on startup
   */
  @Test
  public void bootstrap() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .build();
    var inputBinding = ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setTargetId(library2.getId())
      .setType(ChainBinding.Type.Library)
      .build();

    var result = subject.bootstrap(access, input, ImmutableList.of(inputBinding));

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
  }

  @Test
  public void create() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void create_failsWithInvalidConfig() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setConfig("no type of config I've ever seen")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () -> subject.create(access, input));

    assertTrue(thrown.getMessage()
      .contains("Key 'no type of config I've ever seen' may not be followed by token"));
  }

  @Test
  public void create_withEmbedKey() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setEmbedKey("my $% favorite THINGS")
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("my_favorite_things", result.getEmbedKey());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void create_withEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    var first = NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    var second = NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    when(hubClient.ingest(any(), eq(ImmutableSet.of(library2.getId())), eq(ImmutableSet.of()), eq(ImmutableSet.of())))
      .thenReturn(hubContent);
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    subject.create(access, first);

    Exception thrown = assertThrows(DAOValidationException.class, () -> subject.create(access, second));

    assertEquals("Chain already exists with embed key 'my_favorite_things'!", thrown.getMessage());
  }

  @Test
  public void create_PreviewType() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Preview)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(28, Objects.requireNonNull(result.getEmbedKey()).length());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Preview, result.getType());
  }


  @Test
  public void create_PreviewType_asArtist() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "Artist");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Preview)
      .setName("coconuts")
      .build();

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Preview, result.getType());
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_createdInDraftState() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Draft)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .build();

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("", result.getStopAt());
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .build();

    var result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("", result.getStopAt());
  }

  @Test
  public void create_FailsWithoutAccountID() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.create(access, input));

    assertEquals("Account ID is required.", thrown.getMessage());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.readOne(access, chain1.getId()));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    var result = subject.readOne(access, chain2.getId());

    assertNotNull(result);
    assertEquals(chain2.getId(), result.getId());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("bucket", result.getName());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt());
  }

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.
   */
  @Test
  public void readOne_byEmbedKey_unauthenticatedOk() throws Exception {
    var chain = subject.create(HubClientAccess.internal(), NexusIntegrationTestingFixtures.makeChain(account1, "cats test", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "cats"));
    HubClientAccess access = HubClientAccess.unauthenticated();

    var result = subject.readOneByEmbedKey(access, "cats");

    assertNotNull(result);
    assertEquals(chain.getId(), result.getId());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("cats test", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt());
    assertEquals("cats", result.getEmbedKey());
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.readOne(access, chain1.getId()));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  @Test
  public void readMany() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_excludesChainsInFabricateState() throws Exception {
    NexusIntegrationTestingFixtures.makeChain(account1, "sham", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null);
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readManyInState() throws Exception {
    Collection<Chain> result = subject.readManyInState(HubClientAccess.internal(), Chain.State.Fabricate);

    assertNotNull(result);
    assertEquals(1L, result.size());
    var result0 = result.iterator().next();

    assertEquals("bucket", result0.getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.readMany(access, ImmutableList.of(account1.getId())));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  @Test
  public void update() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Complete, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_cantChangeEndOfPreviewChain() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var previewChain = test.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Preview)
      .setState(Chain.State.Fabricate)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build());
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Preview)
      .setState(Chain.State.Fabricate)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-12T12:17:01.989941Z")
      .build();

    subject.update(access, previewChain.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), previewChain.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals(Chain.Type.Preview, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_addEmbedKey() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setEmbedKey("twenty %$** four HOURS")
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("twenty_four_hours", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Complete, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_removeEmbedKey() throws Exception {
    var chain3 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .clearEmbedKey()
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    subject.update(access, chain3.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain3.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Complete, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }

  @Test
  public void update_addEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setEmbedKey("twenty_four_hours")
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Chain already exists with embed key 'twenty_four_hours'!", thrown.getMessage());
  }

  @Test
  public void update_okayWithEmbedKey() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Production, Chain.State.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Ready)
      .setEmbedKey("jabberwocky")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    subject.update(access, chain.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Ready, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt());
  }


  @Test
  public void update_cannotChangeType() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Preview)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Cannot modify Chain Type", thrown.getMessage());

  }

  @Test
  public void update_outOfDraft_failsWithNoBindings() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "Admin");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain.getId(), NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Ready, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null)));

    assertEquals("Chain must be bound to at least one Library, Sequence, or Instrument", thrown.getMessage());

  }

  @Test
  public void updateState_outOfDraft_failsWithNoBindings() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.updateState(access, chain.getId(), Chain.State.Ready));

    assertEquals("Chain must be bound to at least one Library, Sequence, or Instrument", thrown.getMessage());
  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasSegment() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setTargetId(library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setOffset(5L)
      .setState(Segment.State.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setBeginAt(Value.formatIso8601UTC(Instant.now()))
      .setType(Segment.Type.NextMain)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("bucket")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2015-05-10T12:17:03.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("cannot change chain startAt time after it has segments", thrown.getMessage());

  }

  @Test
  public void update_canChangeName_whenChainsHasSegment() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setTargetId(library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setState(Segment.State.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setBeginAt(Value.formatIso8601UTC(Instant.now()))
      .setType(Segment.Type.NextMain)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build()
    );
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Fabricate)
      .setType(Chain.Type.Production)
      .setStartAt("2015-05-10T12:17:02.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z")
      .build();

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .build();

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Complete, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("", result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setName("coconuts")
      .setState(Chain.State.Fabricate)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Account ID is required.", thrown.getMessage());

  }

  @Test
  public void update_FailsWithoutName() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertEquals("Name is required.", thrown.getMessage());

  }

  @Test
  public void update_failsWithInvalidConfig() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setConfig("no type of config I've ever seen")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain2.getId(), input));

    assertTrue(thrown.getMessage()
      .contains("Key 'no type of config I've ever seen' may not be followed by token"));

  }

  @Test
  public void update_withValidConfig() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setConfig("outputContainer=\"WAV\"")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_CannotChangeAccount() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Complete)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    subject.update(access, chain2.getId(), input);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");

    subject.updateState(access, chain2.getId(), Chain.State.Complete);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Complete, result.getState());
  }

  @Test
  public void updateState_WithoutAccountAccess_Fails() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.updateState(access, chain2.getId(), Chain.State.Complete));

    assertEquals("Account access is required.", thrown.getMessage());

  }

  @Test
  public void updateState_WithAccountAccess() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    subject.updateState(access, chain2.getId(), Chain.State.Complete);

    var result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Complete, result.getState());
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutEngineerRole_ForProductionChain() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User");

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.updateState(access, chain2.getId(), Chain.State.Complete));

    assertEquals("Engineer role is required.", thrown.getMessage());

  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User");
    var chain3 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Preview, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.updateState(access, chain3.getId(), Chain.State.Complete));

    assertEquals("Engineer/Artist role is required.", thrown.getMessage());
  }

  @Test
  public void update_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    var chain3 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null)).toBuilder()
      .setState(Chain.State.Ready)
      .build();

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.update(access, chain3.getId(), chain3));

    assertEquals("Chain must be bound to at least one Library, Sequence, or Instrument", thrown.getMessage());
  }

  @Test
  public void updateState_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    var chain3 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));

    Exception thrown = assertThrows(DAOValidationException.class, () ->
      subject.updateState(access, chain3.getId(), Chain.State.Ready));

    assertEquals("Chain must be bound to at least one Library, Sequence, or Instrument", thrown.getMessage());
  }

  @Test
  public void updateState_outOfDraft_BoundToLibrary() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    library2 = NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas");
    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    var result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToSequence() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    NexusIntegrationTestingFixtures.makeUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    library2 = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("pajamas")
      .build();
    Program program = NexusIntegrationTestingFixtures.makeProgram(library2, Program.Type.Rhythm, Program.State.Published, "fonds", "C#", 0.286, 0.6);
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(program.getId())
      .setType(ChainBinding.Type.Program)
      .build());

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    var result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToMultipleSequences() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    NexusIntegrationTestingFixtures.makeUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    library2 = NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library2, Program.Type.Main, Program.State.Published, "fonds", "C#", 120.0, 0.6)));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library2, Program.Type.Macro, Program.State.Published, "trees A to B", "D#", 120.0, 0.6)));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library2, Program.Type.Macro, Program.State.Published, "trees B to A", "F", 120.0, 0.6)));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library2, Program.Type.Rhythm, Program.State.Published, "beets", "C", 120.0, 0.6)));

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    var result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToInstrument() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    NexusIntegrationTestingFixtures.makeUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    library2 = NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas");
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeInstrument(library2, Instrument.Type.Pad, Instrument.State.Published, "fonds")));

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    var result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   [#170273871] Revived chain should always start now
   */
  @Test
  public void revive() throws Exception {
    var account2 = NexusIntegrationTestingFixtures.makeAccount();
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1, account2), "User,Admin,Artist,Engineer");

    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas")));

    var result = subject.revive(access, chain.getId(), "Testing");

    assertNotNull(result);
    assertTrue(1000 > Instant.now().toEpochMilli() - Instant.parse(result.getStartAt()).toEpochMilli()); // [#170273871] Revived chain should always start now
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals(Chain.Type.Production, result.getType());

    var priorChain = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(priorChain);
    assertEquals(Chain.State.Failed, priorChain.getState());
    assertEquals("", priorChain.getEmbedKey());
//  FUTURE assert for real message sent about work  org.junit.Assert.assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readManyPreviousDays(HubClientAccess.internal(), 1).size());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require exists chain of which to revived, throw error if not found.
   */
  @Test
  public void revive_failsIfNotExistPriorChain() {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");

    Exception thrown = assertThrows(DAOExistenceException.class, () ->
      subject.revive(access, UUID.randomUUID().toString(), "Testing"));

    assertTrue(thrown.getMessage().contains("does not exist"));
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#174898524] Artist can revive a Chain of any type
   */
  @Test
  public void revive_okOfAnyType() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Preview, Chain.State.Failed, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas")));

    subject.revive(access, chain.getId(), "Testing");
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#175137186] Artist can only revive Chain in fabricate, failed, or completed state
   */
  @Test
  public void revive_failsInDraftState() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Preview, Chain.State.Draft, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas")));

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
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Preview, Chain.State.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas")));

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.revive(access, chain.getId(), "Testing"));

    assertEquals("Can't revive a Chain unless it's in Fabricate, Complete, or Failed state", thrown.getMessage());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_okayWithEngineerAccess() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "Engineer");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas")));

    var result = subject.revive(access, chain.getId(), "Testing");

    assertNotNull(result);
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_failsWithArtistAccess() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Artist");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas")));

    Exception thrown = assertThrows(DAOPrivilegeException.class, () ->
      subject.revive(access, chain.getId(), "Testing"));

    assertEquals("Account access is required.", thrown.getMessage());
  }

  /**
   [#160299309] Engineer wants a *revived* action, duplicates all ChainBindings, including ChainConfig, ChainInstrument, ChainLibrary, and ChainSequence
   */
  @Test
  public void revive_duplicatesAllChainBindings() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess(ImmutableList.of(account1), "Engineer");
    NexusIntegrationTestingFixtures.makeUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    var chain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky").toBuilder()
      .setConfig("outputFrameRate=35\noutputChannels=4")
      .build());
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeLibrary(account1, "pajamas")));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library1, Program.Type.Main, Program.State.Published, "fonds", "C#", 120.0, 0.6)));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library1, Program.Type.Macro, Program.State.Published, "trees A to B", "D#", 120.0, 0.6)));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library1, Program.Type.Macro, Program.State.Published, "trees B to A", "F", 120.0, 0.6)));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeProgram(library1, Program.Type.Rhythm, Program.State.Published, "beets", "C", 120.0, 0.6)));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain, NexusIntegrationTestingFixtures.makeInstrument(library1, Instrument.Type.Pad, Instrument.State.Published, "fonds")));

    var result = subject.revive(access, chain.getId(), "Testing");

    assertNotNull(result);
    Collection<ChainBinding> bindings = chainBindingDAO.readMany(HubClientAccess.internal(), ImmutableList.of(result.getId()));
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBinding.Type.Instrument)).count());
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBinding.Type.Library)).count());
    assertEquals(4, bindings.stream().filter(binding -> binding.getType().equals(ChainBinding.Type.Program)).count());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");
    chain1 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setTargetId(library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setState(Segment.State.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setType(Segment.Type.NextMain)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:04:10.000001Z")
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T11:53:40.000001Z")).orElseThrow();

    assertEquals(chain1.getId(), result.getChainId());
    assertEquals(6, result.getOffset());
    assertEquals("2014-02-14T12:04:10.000001Z", result.getBeginAt());
    assertEquals(Segment.Type.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");
    chain1 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setType(Segment.Type.NextMain)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:03:50.000001Z")
      .setState(Segment.State.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T13:53:50.000001Z")).orElseThrow();

    var resultChain = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultChain);
    assertEquals(Chain.State.Fabricate, resultChain.getState());
    assertEquals(Segment.Type.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");
    chain1 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(NexusIntegrationTestingFixtures.makeBinding(chain1, library1));
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setType(Segment.Type.NextMain)
      .setBeginAt("2014-02-14T14:03:35.000001Z")
      .setEndAt("2014-02-14T14:03:55.000001Z")
      .setOffset(5L)
      .setState(Segment.State.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));

    assertTrue(result.isEmpty());
    var resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(Chain.State.Complete, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");
    chain1 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setBeginAt("2014-02-14T14:03:15.000001Z")
      .setEndAt("2014-02-14T14:03:25.000001Z")
      .setOffset(5L)
      .setType(Segment.Type.NextMain)
      .setState(Segment.State.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z")).orElseThrow();

    var resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(Chain.State.Fabricate, resultFinal.getState());
    assertEquals(Segment.Type.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsAlreadyHasNextSegment() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");
    chain1 = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setBeginAt("2014-02-14T14:03:15.000001Z")
      .setEndAt("2014-02-14T14:05:40.000001Z") // after chain stop-at, that's what triggers the chain to be completed
      .setOffset(5L)
      .setType(Segment.Type.NextMain)
      .setState(Segment.State.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertTrue(result.isEmpty());
  }

  @Test
  public void buildNextSegmentOrComplete_chainEndingInCraftedSegment() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");
    var fromChain = test.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    test.put(NexusIntegrationTestingFixtures.makeSegment(fromChain, 5, Segment.State.Crafted, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T14:03:38.000001Z"), "A major", 64, 0.52, 120.0, "chains-1-segments-9f7s89d8a7892.wav", "AAC"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z")).orElseThrow();

    assertEquals(fromChain.getId(), result.getChainId());
    assertEquals(6, result.getOffset());
    assertEquals("2014-08-12T14:03:38.000001Z", result.getBeginAt());
  }

  @Test
  public void buildNextSegmentOrComplete_newEmptyChain() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Internal");
    test.put(NexusIntegrationTestingFixtures.makeChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    var fromChain = test.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setType(Chain.Type.Production)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .build());

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z")).orElseThrow();

    assertEquals(fromChain.getId(), result.getChainId());
    assertEquals(0, result.getOffset());
    assertEquals("2014-08-12T12:17:02.527142Z", result.getBeginAt());
  }

  @Test
  public void destroy() throws Exception {
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");

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
    HubClientAccess access = NexusIntegrationTestingFixtures.makeHubClientAccess("Admin");
    NexusIntegrationTestingFixtures.makeLibrary(account1, "nerds");

    try {
      subject.destroy(access, chain1.getId());

    } catch (Exception e) {
      var result = subject.readOne(HubClientAccess.internal(), chain1.getId());
      assertNotNull(result);
      throw e;
    }
  }

}
