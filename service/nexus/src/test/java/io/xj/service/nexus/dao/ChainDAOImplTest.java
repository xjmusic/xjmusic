// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.Segment;
import io.xj.User;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Value;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildAccount;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildChainBinding;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildHubClientAccess;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildInstrument;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildLibrary;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgram;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequence;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequenceBinding;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainDAOImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  HubClient hubClient;
  private NexusEntityStore test;
  private ChainDAO subject;
  private Account account1;
  private Library library1;
  private Library library2;
  private Chain chain1;
  private Chain chain2;
  private User user3;
  private HubContent hubContent;
  private ChainBindingDAO chainBindingDAO;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new NexusDAOModule()));
    chainBindingDAO = injector.getInstance(ChainBindingDAO.class);
    EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store
    test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // hub entities as basis
    account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("fish")
      .build();
    library1 = buildLibrary(account1, "test");
    library2 = buildLibrary(account1, "test");
    User user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    Program program1 = buildProgram(library2, Program.Type.Rhythm, Program.State.Published, "fonds", "C#", 0.286, 0.6);
    var sequence1 = buildProgramSequence(program1, 16, "epic beat part 1", 0.342, "C#", 0.286);
    var binding1_0 = buildProgramSequenceBinding(sequence1, 0);
    hubContent = new HubContent(ImmutableSet.of(
      program1,
      sequence1,
      binding1_0
    ));

    // Payload comprising Nexus entities
    chain1 = test.put(buildChain(account1, "school", Chain.Type.Production, Chain.State.Ready,
      Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    chain2 = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate,
      Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    // Instantiate the test subject
    subject = injector.getInstance(ChainDAO.class);
  }

  @Test
  public void create() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt());
  }

  @Test
  public void create_failsWithInvalidConfig() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setConfig("no type of config I've ever seen")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Key 'no type of config I've ever seen' may not be followed by token");

    subject.create(access, input);
  }

  @Test
  public void create_withEmbedKey() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setEmbedKey("my $% favorite THINGS")
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Chain result = subject.create(access, input);

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
    Chain first = buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    Chain second = buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things");
    when(hubClient.ingest(any(), eq(ImmutableSet.of(library2.getId())), eq(ImmutableSet.of()), eq(ImmutableSet.of())))
      .thenReturn(hubContent);
    HubClientAccess access = buildHubClientAccess("Admin");
    subject.create(access, first);

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain already exists with embed key 'my_favorite_things'");

    subject.create(access, second);
  }

  @Test
  public void create_PreviewType() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Preview)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(28, Objects.requireNonNull(result.getEmbedKey()).length());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Preview, result.getType());
  }


  @Test
  public void create_PreviewType_asArtist() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "Artist");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Preview)
      .setName("coconuts")
      .build();

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Preview, result.getType());
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_createdInDraftState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    Chain result = subject.create(access, input);

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
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Draft)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .build();

    Chain result = subject.create(access, input);

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
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .build();

    Chain result = subject.create(access, input);

    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(Chain.State.Draft, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt());
    assertEquals("", result.getStopAt());
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setName("coconuts")
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Account ID is required");

    subject.create(access, input);
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");
    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.readOne(access, chain1.getId());
  }

  @Test
  public void readOne() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Chain result = subject.readOne(access, chain2.getId());

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
    Chain chain = subject.create(HubClientAccess.internal(), buildChain(account1, "cats test", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "cats"));
    HubClientAccess access = HubClientAccess.unauthenticated();

    Chain result = subject.readOneByEmbedKey(access, "cats");

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
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");
    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.readOne(access, chain1.getId());
  }

  @Test
  public void readMany() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_excludesChainsInFabricateState() throws Exception {
    buildChain(account1, "sham", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null);
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User");

    Collection<Chain> result = subject.readMany(access, ImmutableList.of(account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readManyInState() throws Exception {
    Collection<Chain> result = subject.readManyInState(HubClientAccess.internal(), Chain.State.Fabricate);

    assertNotNull(result);
    assertEquals(1L, result.size());
    Chain result0 = result.iterator().next();

    assertEquals("bucket", result0.getName());
  }

  @Test
  public void readMany_SeesNothingOutsideOfAccount() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.readMany(access, ImmutableList.of(account1.getId()));
  }

  @Test
  public void update() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
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
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain previewChain = test.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Preview)
      .setState(Chain.State.Fabricate)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build());
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Preview)
      .setState(Chain.State.Fabricate)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-12T12:17:01.989941Z")
      .build();

    subject.update(access, previewChain.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), previewChain.getId());
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
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
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

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
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
    Chain chain3 = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
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

    Chain result = subject.readOne(HubClientAccess.internal(), chain3.getId());
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
    test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setEmbedKey("twenty_four_hours")
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain already exists with embed key 'twenty_four_hours'");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_okayWithEmbedKey() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Production, Chain.State.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    Chain input = Chain.newBuilder()
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

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
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
  public void update_cannotChangeType() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Preview)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Cannot modify Chain Type");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_outOfDraft_failsWithNoBindings() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "Admin");
    Chain chain = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    subject.update(access, chain.getId(), buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Ready, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
  }

  @Test
  public void updateState_outOfDraft_failsWithNoBindings() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    Chain chain = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    subject.updateState(access, chain.getId(), Chain.State.Ready);
  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
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
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("bucket")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2015-05-10T12:17:03.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("cannot change chain startAt time after it has segments");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_canChangeName_whenChainsHasSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
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
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Fabricate)
      .setType(Chain.Type.Production)
      .setStartAt("2015-05-10T12:17:02.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z")
      .build();

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
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
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .build();

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Complete, result.getState());
    assertEquals(Chain.Type.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt());
    assertEquals("", result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setName("coconuts")
      .setState(Chain.State.Fabricate)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Account ID is required");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setState(Chain.State.Draft)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Name is required");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_failsWithInvalidConfig() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setConfig("no type of config I've ever seen")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Complete)
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z")
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Key 'no type of config I've ever seen' may not be followed by token");

    subject.update(access, chain2.getId(), input);
  }

  @Test
  public void update_withValidConfig() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
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
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain input = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("coconuts")
      .setState(Chain.State.Complete)
      .setType(Chain.Type.Production)
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z")
      .build();

    subject.update(access, chain2.getId(), input);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(account1.getId(), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");

    subject.updateState(access, chain2.getId(), Chain.State.Complete);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Complete, result.getState());
  }

  @Test
  public void updateState_WithoutAccountAccess_Fails() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.updateState(access, chain2.getId(), Chain.State.Complete);
  }

  @Test
  public void updateState_WithAccountAccess() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    subject.updateState(access, chain2.getId(), Chain.State.Complete);

    Chain result = subject.readOne(HubClientAccess.internal(), chain2.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Complete, result.getState());
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutEngineerRole_ForProductionChain() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Engineer role is required");

    subject.updateState(access, chain2.getId(), Chain.State.Complete);
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User");
    Chain chain3 = test.put(buildChain(account1, "bucket", Chain.Type.Preview, Chain.State.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Engineer/Artist role is required");

    subject.updateState(access, chain3.getId(), Chain.State.Complete);
  }

  @Test
  public void update_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    Chain chain3 = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null)).toBuilder()
      .setState(Chain.State.Ready)
      .build();

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    subject.update(access, chain3.getId(), chain3);
  }

  @Test
  public void updateState_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    Chain chain3 = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    subject.updateState(access, chain3.getId(), Chain.State.Ready);
  }

  @Test
  public void updateState_outOfDraft_BoundToLibrary() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    Chain chain = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    library2 = buildLibrary(account1, "pajamas");
    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToSequence() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    library2 = test.put(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("pajamas")
      .build());
    Program program = buildProgram(library2, Program.Type.Rhythm, Program.State.Published, "fonds", "C#", 0.286, 0.6);
    Chain chain = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(program.getId())
      .setType(ChainBinding.Type.Program)
      .build());

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToMultipleSequences() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    library2 = buildLibrary(account1, "pajamas");
    Chain chain = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.put(buildChainBinding(chain, buildProgram(library2, Program.Type.Main, Program.State.Published, "fonds", "C#", 120.0, 0.6)));
    test.put(buildChainBinding(chain, buildProgram(library2, Program.Type.Macro, Program.State.Published, "trees A to B", "D#", 120.0, 0.6)));
    test.put(buildChainBinding(chain, buildProgram(library2, Program.Type.Macro, Program.State.Published, "trees B to A", "F", 120.0, 0.6)));
    test.put(buildChainBinding(chain, buildProgram(library2, Program.Type.Rhythm, Program.State.Published, "beets", "C", 120.0, 0.6)));

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToInstrument() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Artist,Engineer");
    user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    Chain chain = test.put(buildChain(account1, "bucket", Chain.Type.Production, Chain.State.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    library2 = buildLibrary(account1, "pajamas");
    test.put(buildChainBinding(chain, buildInstrument(library2, Instrument.Type.Harmonic, Instrument.State.Published, "fonds")));

    subject.updateState(access, chain.getId(), Chain.State.Ready);

    Chain result = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(Chain.State.Ready, result.getState());
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   [#170273871] Revived chain should always start now
   */
  @Test
  public void revive() throws Exception {
    Account account2 = buildAccount();
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1, account2), "User,Admin,Artist,Engineer");

    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(buildChainBinding(chain, buildLibrary(account1, "pajamas")));

    Chain result = subject.revive(access, chain.getId(), "Testing");

    assertNotNull(result);
    assertTrue(1000 > Instant.now().toEpochMilli() - Instant.parse(result.getStartAt()).toEpochMilli()); // [#170273871] Revived chain should always start now
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(account1.getId(), result.getAccountId());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals(Chain.Type.Production, result.getType());

    Chain priorChain = subject.readOne(HubClientAccess.internal(), chain.getId());
    assertNotNull(priorChain);
    assertEquals(Chain.State.Failed, priorChain.getState());
    assertEquals("", priorChain.getEmbedKey());
//  FUTURE assert for real message sent about work  org.junit.Assert.assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readManyPreviousDays(HubClientAccess.internal(), 1).size());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require exists chain of which to revived, throw error if not found.
   */
  @Test
  public void revive_failsIfNotExistPriorChain() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    failure.expect(DAOExistenceException.class);
    failure.expectMessage("does not exist");

    subject.revive(access, UUID.randomUUID().toString(), "Testing");
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#174898524] Artist can revive a Chain of any type
   */
  @Test
  public void revive_okOfAnyType() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Preview, Chain.State.Failed, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(buildChainBinding(chain, buildLibrary(account1, "pajamas")));

    subject.revive(access, chain.getId(), "Testing");
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#175137186] Artist can only revive Chain in fabricate, failed, or completed state
   */
  @Test
  public void revive_failsInDraftState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Preview, Chain.State.Draft, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(buildChainBinding(chain, buildLibrary(account1, "pajamas")));

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Can't revive a Chain unless it's in Fabricate, Complete, or Failed state");

    subject.revive(access, chain.getId(), "Testing");
  }

  /**
   [#160299309] Engineer wants a *revived* action
   [#175137186] Artist can only revive Chain in fabricate, failed, or completed state
   */
  @Test
  public void revive_failsInReadyState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Preview, Chain.State.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(buildChainBinding(chain, buildLibrary(account1, "pajamas")));

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Can't revive a Chain unless it's in Fabricate, Complete, or Failed state");

    subject.revive(access, chain.getId(), "Testing");
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_okayWithEngineerAccess() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "Engineer");
    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(buildChainBinding(chain, buildLibrary(account1, "pajamas")));

    Chain result = subject.revive(access, chain.getId(), "Testing");

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
    HubClientAccess access = buildHubClientAccess("Artist");
    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.put(buildChainBinding(chain, buildLibrary(account1, "pajamas")));

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("Account access is required");

    subject.revive(access, chain.getId(), "Testing");
  }

  /**
   [#160299309] Engineer wants a *revived* action, duplicates all ChainBindings, including ChainConfig, ChainInstrument, ChainLibrary, and ChainSequence
   */
  @Test
  public void revive_duplicatesAllChainBindings() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "Engineer");
    user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    Chain chain = test.put(buildChain(account1, "school", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky").toBuilder()
      .setConfig("outputFrameRate=35\noutputChannels=4")
      .build());
    test.put(buildChainBinding(chain, buildLibrary(account1, "pajamas")));
    test.put(buildChainBinding(chain, buildProgram(library1, Program.Type.Main, Program.State.Published, "fonds", "C#", 120.0, 0.6)));
    test.put(buildChainBinding(chain, buildProgram(library1, Program.Type.Macro, Program.State.Published, "trees A to B", "D#", 120.0, 0.6)));
    test.put(buildChainBinding(chain, buildProgram(library1, Program.Type.Macro, Program.State.Published, "trees B to A", "F", 120.0, 0.6)));
    test.put(buildChainBinding(chain, buildProgram(library1, Program.Type.Rhythm, Program.State.Published, "beets", "C", 120.0, 0.6)));
    test.put(buildChainBinding(chain, buildInstrument(library1, Instrument.Type.Harmonic, Instrument.State.Published, "fonds")));

    Chain result = subject.revive(access, chain.getId(), "Testing");

    assertNotNull(result);
    Collection<ChainBinding> bindings = chainBindingDAO.readMany(HubClientAccess.internal(), ImmutableList.of(result.getId()));
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBinding.Type.Instrument)).count());
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBinding.Type.Library)).count());
    assertEquals(4, bindings.stream().filter(binding -> binding.getType().equals(ChainBinding.Type.Program)).count());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
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
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
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

    Chain resultChain = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultChain);
    assertEquals(Chain.State.Fabricate, resultChain.getState());
    assertEquals(Segment.Type.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butLastSegmentNotDubbedSoChainNotComplete() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(5L)
      .setBeginAt(Value.formatIso8601UTC(Instant.now()))
      .setType(Segment.Type.NextMain)
      .setState(Segment.State.Dubbing)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892")
      .setOutputEncoder("wav")
      .build());

    Optional<Segment> result = subject.buildNextSegmentOrCompleteTheChain(access, chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));

    assertTrue(result.isEmpty());
    Chain resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(Chain.State.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.put(buildChainBinding(chain1, library1));
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
    Chain resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(Chain.State.Complete, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
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

    Chain resultFinal = subject.readOne(HubClientAccess.internal(), chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(Chain.State.Fabricate, resultFinal.getState());
    assertEquals(Segment.Type.Pending, result.getType());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsAlreadyHasNextSegment() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    chain1 = test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
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
    HubClientAccess access = buildHubClientAccess("Internal");
    Chain fromChain = test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    test.put(buildSegment(fromChain, 5, Segment.State.Crafted, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T14:03:38.000001Z"), "A major", 64, 0.52, 120.0, "chains-1-segments-9f7s89d8a7892.wav", "AAC"));

    Segment result = subject.buildNextSegmentOrCompleteTheChain(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z")).orElseThrow();

    assertEquals(fromChain.getId(), result.getChainId());
    assertEquals(6, result.getOffset());
    assertEquals("2014-08-12T14:03:38.000001Z", result.getBeginAt());
  }

  @Test
  public void buildNextSegmentOrComplete_newEmptyChain() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    test.put(buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    Chain fromChain = test.put(Chain.newBuilder()
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
      Chain result = subject.readOne(HubClientAccess.internal(), chain1.getId());
      assertNotNull(result);
      throw e;
    }
  }

}
