// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.exception.CoreException;
import io.xj.core.external.AmazonProvider;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.Account;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainBindingType;
import io.xj.core.model.ChainConfig;
import io.xj.core.model.ChainConfigType;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentState;
import io.xj.core.model.User;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.Assert;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.core.testing.InternalResources;
import io.xj.core.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChainIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  @Mock
  WorkManager workManager;
  private ChainDAO testDAO;
  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private Injector injector;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
          bind(WorkManager.class).toInstance(workManager);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // segment waveform config
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Account "fish" has chain "school" and chain "bucket"
    fake.account1 = test.insert(Account.create("fish"));
    fake.library1 = test.insert(Library.create(fake.account1, "test"));
    fake.library2 = test.insert(Library.create(fake.account1, "test"));
    fake.chain1 = test.insert(Chain.create(fake.account1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    test.insert(ChainBinding.create(fake.chain1, fake.library1));
    fake.chain2 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    test.insert(ChainBinding.create(fake.chain2, fake.library1));

    // Account "boat" has no chains
    test.insert(Account.create("boat"));

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_withEmbedKey() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setEmbedKey("my $% favorite THINGS")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("my_favorite_things", result.getEmbedKey());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_withEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things"));
    failure.expect(CoreException.class);
    failure.expectMessage("Found Existing Chain with this embed_key");

    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setEmbedKey("my_favorite_things")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_PreviewType() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setType("Preview")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Preview, result.getType());
    // future test: time of startAt to stopAt relative to [#190] specs
//    assertEquals("2009-08-12 12:17:02.527142", result.getStartAt().toString());
//    assertEquals("2009-09-11 12:17:01.047563", result.getStopAt().toString());
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_createdInDraftState() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2009-08-12T12:17:02.527142Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithInvalidState() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("bullshit state")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(CoreException.class);
    failure.expectMessage("'bullshit state' is not a valid state");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fake.chain1.getId());
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Chain result = testDAO.readOne(access, fake.chain2.getId());

    assertNotNull(result);
    assertEquals(fake.chain2.getId(), result.getId());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("bucket", result.getName());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
  }

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.
   */
  @Test
  public void readOne_byEmbedKey_unauthenticatedOk() throws Exception {
    Chain chain = test.insert(Chain.create(fake.account1, "cats test", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "cats"));
    Access access = Access.unauthenticated();

    Chain result = testDAO.readOne(access, "cats");

    assertNotNull(result);
    assertEquals(chain.getId(), result.getId());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals("cats test", result.getName());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
    assertEquals("cats", result.getEmbedKey());
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fake.chain1.getId());
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Chain> result = testDAO.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_excludesChainsInEraseState() throws Exception {
    test.insert(Chain.create(fake.account1, "sham", ChainType.Production, ChainState.Erase, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Chain> result = testDAO.readMany(access, ImmutableList.of(fake.account1.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAllInState() throws Exception {
    Collection<Chain> result = testDAO.readAllInState(Access.internal(), ChainState.Fabricate);

    assertNotNull(result);
    assertEquals(1L, result.size());
    Chain result0 = result.iterator().next();

    assertEquals("bucket", result0.getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Chain> result = testDAO.readMany(access, ImmutableList.of(fake.chain1.getId()));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void update() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, fake.chain2.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_addEmbedKey() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setEmbedKey("twenty %$** four HOURS")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, fake.chain2.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("twenty_four_hours", result.getEmbedKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_removeEmbedKey() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setEmbedKey("")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, fake.chain3.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), fake.chain3.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertNull(result.getEmbedKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_addEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours"));
    failure.expect(CoreException.class);
    failure.expectMessage("Found Existing Chain with this embed_key");
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setEmbedKey("twenty_four_hours")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, fake.chain2.getId(), inputData);
  }

  @Test
  public void update_okayWithEmbedKey() throws Exception {
    Access access = Access.create("Admin");
    Chain chain = test.insert(Chain.create(fake.account1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.insert(ChainBinding.create(chain, fake.library2));
    test.insert(ChainBinding.create(chain, fake.library2));
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Ready")
      .setEmbedKey("jabberwocky")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, chain.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), chain.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Ready, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }


  @Test
  public void update_cannotChangeType() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Preview")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, fake.chain2.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasSegment() throws Exception {
    Access access = Access.create("Admin");
    test.insert(ChainBinding.create(fake.chain2, fake.library2));
    test.insert(Segment.create()
      .setChainId(fake.chain2.getId())
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setBeginAtInstant(InternalResources.now())
      .setTypeEnum(FabricatorType.NextMain)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("bucket")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2015-05-10T12:17:03.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z");

    failure.expect(CoreException.class);
    failure.expectMessage("cannot change chain startAt time after it has segments");

    testDAO.update(access, fake.chain2.getId(), inputData);
  }

  @Test
  public void update_canChangeName_whenChainsHasSegment() throws Exception {
    Access access = Access.create("Admin");
    test.insert(ChainBinding.create(fake.chain2, fake.library2));
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setBeginAtInstant(InternalResources.now())
      .setTypeEnum(FabricatorType.NextMain)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2015-05-10T12:18:02.527142Z")
      .setUpdatedAt("2015-05-10T12:18:32.527142Z"));
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setState("Fabricate")
      .setType("Production")
      .setStartAt("2015-05-10T12:17:02.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z");

    testDAO.update(access, fake.chain2.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z");

    testDAO.update(access, fake.chain2.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setName("coconuts")
      .setState("Fabricate")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, fake.chain2.getId(), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, fake.chain2.getId(), inputData);
  }

  @Test
  public void update_CannotChangeAccount() throws Exception {
    Access access = Access.create("Admin");
    Chain inputData = Chain.create()
      .setAccountId(fake.account1.getId())
      .setName("coconuts")
      .setState("Complete")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    testDAO.update(access, fake.chain2.getId(), inputData);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals(fake.account1.getId(), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    Access access = Access.create("Internal");

    testDAO.updateState(access, fake.chain2.getId(), ChainState.Complete);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals(ChainState.Complete, result.getState());
  }

  @Test
  public void updateState_WithoutAccountAccess_Fails() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.updateState(access, fake.chain2.getId(), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Engineer");

    testDAO.updateState(access, fake.chain2.getId(), ChainState.Complete);

    Chain result = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(result);
    assertEquals(ChainState.Complete, result.getState());
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutEngineerRole_ForProductionChain() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    failure.expect(CoreException.class);
    failure.expectMessage("Engineer role is required");

    testDAO.updateState(access, fake.chain2.getId(), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Preview, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    failure.expect(CoreException.class);
    failure.expectMessage("Artist or Engineer role is required");

    testDAO.updateState(access, fake.chain3.getId(), ChainState.Complete);
  }

  @Test
  public void updateState_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist,Engineer");

    failure.expect(CoreException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    testDAO.updateState(access, fake.chain3.getId(), ChainState.Ready);
  }

  @Test
  public void updateState_outOfDraft_BoundToLibrary() throws Exception {
    Chain chain = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    fake.library2 = test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()));
    test.insert(ChainBinding.create(chain, fake.library2));

    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist,Engineer");

    testDAO.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToSequence() throws Exception {
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    fake.library2 = test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()));
    Program program = Program.create(fake.user3, fake.library2, ProgramType.Rhythm, ProgramState.Published, "fonds", "C#", 0.286, 0.6);
    Chain chain = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(chain, program));
    test.insert(program);

    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist,Engineer");

    testDAO.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToMultipleSequences() throws Exception {
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    fake.library2 = test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()));
    Chain chain = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library2, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6))));
    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "trees A to B", "D#", 120.0, 0.6))));
    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library2, ProgramType.Macro, ProgramState.Published, "trees B to A", "F", 120.0, 0.6))));
    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library2, ProgramType.Rhythm, ProgramState.Published, "beets", "C", 120.0, 0.6))));

    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist,Engineer");

    testDAO.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToInstrument() throws Exception {
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    Chain chain = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null));
    fake.library2 = test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()));
    test.insert(ChainBinding.create(chain, test.insert(Instrument.create(fake.user3, fake.library2, InstrumentType.Harmonic, InstrumentState.Published, "fonds"))));

    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist,Engineer");

    testDAO.updateState(access, chain.getId(), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), chain.getId());
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   */
  @Test
  public void revive() throws Exception {
    fake.account2 = Account.create();
    Access access = Access.create(ImmutableList.of(fake.account1, fake.account2), "User,Admin,Artist,Engineer");

    Chain chain = test.insert(Chain.create(fake.account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.insert(ChainBinding.create(chain, test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()))));

    Chain result = testDAO.revive(access, chain.getId());

    assertNotNull(result);
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    verify(workManager).startChainFabrication(result.getId());

    Chain priorChain = testDAO.readOne(Access.internal(), chain.getId());
    assertNotNull(priorChain);
    assertEquals(ChainState.Failed, priorChain.getState());
    assertNull(priorChain.getEmbedKey());
    verify(workManager).stopChainFabrication(priorChain.getId());

    assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readAllPreviousDays(Access.internal(), 1).size());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require exists chain of which to revived, throw error if not found.
   */
  @Test
  public void revive_failsIfNotExistPriorChain() throws Exception {
    Access access = Access.create("Admin");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.revive(access, UUID.randomUUID());
  }

  /**
   [#160299309] Engineer wants a *revived* action, throw error if trying to revived of chain that is not production in fabricate state
   */
  @Test
  public void revive_failsIfNotFabricateState() throws Exception {
    Access access = Access.create("Admin");
    Chain chain = test.insert(Chain.create(fake.account1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.insert(ChainBinding.create(chain, test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()))));
    failure.expect(CoreException.class);
    failure.expectMessage("Only a Fabricate-state Chain can be revived.");

    testDAO.revive(access, chain.getId());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_okayWithEngineerAccess() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Engineer");
    Chain chain = test.insert(Chain.create(fake.account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.insert(ChainBinding.create(chain, test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()))));

    Chain result = testDAO.revive(access, chain.getId());

    assertNotNull(result);
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_failsWithArtistAccess() throws Exception {
    Access access = Access.create("Artist");
    Chain chain = test.insert(Chain.create(fake.account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.insert(ChainBinding.create(chain, test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()))));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.revive(access, chain.getId());
  }

  /**
   [#160299309] Engineer wants a *revived* action, duplicates all ChainBindings, including ChainConfig, ChainInstrument, ChainLibrary, and ChainSequence
   */
  @Test
  public void revive_duplicatesAllChainBindings() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "Engineer");
    fake.user3 = test.insert(User.create("jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    Chain chain = test.insert(Chain.create(fake.account1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky"));
    test.insert(ChainConfig.create(chain, ChainConfigType.OutputFrameRate, "1,4,35"));
    test.insert(ChainConfig.create(chain, ChainConfigType.OutputChannels, "2,83,4"));
    test.insert(ChainBinding.create(chain, test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()))));

    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6))));
    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library1, ProgramType.Macro, ProgramState.Published, "trees A to B", "D#", 120.0, 0.6))));
    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library1, ProgramType.Macro, ProgramState.Published, "trees B to A", "F", 120.0, 0.6))));
    test.insert(ChainBinding.create(chain, test.insert(Program.create(fake.user3, fake.library1, ProgramType.Rhythm, ProgramState.Published, "beets", "C", 120.0, 0.6))));
    test.insert(ChainBinding.create(chain, test.insert(Instrument.create(fake.user3, fake.library1, InstrumentType.Harmonic, InstrumentState.Published, "fonds"))));

    Chain result = testDAO.revive(access, chain.getId());

    assertNotNull(result);
    assertEquals(2, injector.getInstance(ChainConfigDAO.class).readMany(Access.internal(), ImmutableList.of(result.getId())).size());
    ChainBindingDAO chainBindingDao = injector.getInstance(ChainBindingDAO.class);
    Collection<ChainBinding> bindings = chainBindingDao.readMany(Access.internal(), ImmutableList.of(result.getId()));
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBindingType.Instrument)).count());
    assertEquals(1, bindings.stream().filter(binding -> binding.getType().equals(ChainBindingType.Library)).count());
    assertEquals(4, bindings.stream().filter(binding -> binding.getType().equals(ChainBindingType.Program)).count());
  }

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   */
  @Test
  public void checkAndReviveAll() throws Exception {
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(5L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setBeginAtInstant(InternalResources.now())
      .setTypeEnum(FabricatorType.NextMain)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2015-05-10T12:18:02.527142Z")
      .setUpdatedAt("2015-05-10T12:18:32.527142Z"));
    test.insert(Library.create(fake.account1, "pajamas", InternalResources.now()));

    Collection<Chain> results = testDAO.checkAndReviveAll(Access.internal());

    assertEquals(1, results.size());
    Chain result = results.iterator().next();
    assertEquals("bucket", result.getName());
    assertNull(result.getEmbedKey());
    assertEquals(fake.account1.getId(), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    verify(workManager).startChainFabrication(result.getId());

    Chain priorChain = testDAO.readOne(Access.internal(), fake.chain2.getId());
    assertNotNull(priorChain);
    assertEquals(ChainState.Failed, priorChain.getState());
    assertNull(priorChain.getEmbedKey());
    verify(workManager).stopChainFabrication(priorChain.getId());

    assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readAllPreviousDays(Access.internal(), 1).size());
  }


  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment() throws Exception {
    Access access = Access.create("Internal");
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.insert(ChainBinding.create(fake.chain1, fake.library2));
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTypeEnum(FabricatorType.NextMain)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:04:10.000001Z")
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fake.chain1, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T11:53:40.000001Z"));

    assertTrue(result.isPresent());
    assertEquals(fake.chain1.getId(), result.get().getChainId());
    assertEquals(Long.valueOf(6), result.get().getOffset());
    assertEquals("2014-02-14T12:04:10.000001Z", result.get().getBeginAt().toString());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    Access access = Access.create("Internal");
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(5L)
      .setTypeEnum(FabricatorType.NextMain)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:03:50.000001Z")
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fake.chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T13:53:50.000001Z"));

    assertTrue(result.isPresent());
    Chain resultFinal = testDAO.readOne(Access.internal(), fake.chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butLastSegmentNotDubbedSoChainNotComplete() throws Exception {
    Access access = Access.create("Internal");
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(5L)
      .setBeginAtInstant(InternalResources.now())
      .setTypeEnum(FabricatorType.NextMain)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fake.chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));
    assertTrue(result.isEmpty());
    Chain resultFinal = testDAO.readOne(Access.internal(), fake.chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    Access access = Access.create("Internal");
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.insert(ChainBinding.create(fake.chain1, fake.library1));
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setTypeEnum(FabricatorType.NextMain)
      .setBeginAt("2014-02-14T14:03:35.000001Z")
      .setEndAt("2014-02-14T14:03:55.000001Z")
      .setOffset(5L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fake.chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));
    assertTrue(result.isEmpty());
    Chain resultFinal = testDAO.readOne(Access.internal(), fake.chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.Complete, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    Access access = Access.create("Internal");
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    fake.chain1.setCreatedAt("2014-02-14T14:03:15.000001Z").setUpdatedAt("2014-02-14T14:03:45.000001Z");
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setBeginAt("2014-02-14T14:03:15.000001Z")
      .setEndAt("2014-02-14T14:03:25.000001Z")
      .setOffset(5L)
      .setTypeEnum(FabricatorType.NextMain)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fake.chain1, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));
    assertTrue(result.isPresent());
    Chain resultFinal = testDAO.readOne(Access.internal(), fake.chain1.getId());
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsAlreadyHasNextSegment() throws Exception {
    Access access = Access.create("Internal");
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null));
    test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setBeginAt("2014-02-14T14:03:15.000001Z")
      .setEndAt("2014-02-14T14:05:40.000001Z") // after chain stop-at, that's what triggers the chain to be completed
      .setOffset(5L)
      .setTypeEnum(FabricatorType.NextMain)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fake.chain1, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertFalse(result.isPresent());
  }

  @Test
  public void buildNextSegmentOrComplete_chainEndingInCraftedSegment() throws Exception {
    Access access = Access.create("Internal");
    Chain fromChain = test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    test.insert(Segment.create(fromChain, 5, SegmentState.Crafted, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T14:03:38.000001Z"), "A major", 64, 0.52, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertTrue(result.isPresent());
    assertEquals(fromChain.getId(), result.get().getChainId());
    assertEquals(Long.valueOf(6), result.get().getOffset());
    assertEquals("2014-08-12T14:03:38.000001Z", result.get().getBeginAt().toString());
  }

  @Test
  public void buildNextSegmentOrComplete_newEmptyChain() throws Exception {
    Access access = Access.create("Internal");
    test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));

    Chain fromChain = Chain.create();
    fromChain.setStartAt("2014-08-12T12:17:02.527142Z");
    fromChain.setStopAt(null);
    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertTrue(result.isPresent());
    assertEquals(fromChain.getId(), result.get().getChainId());
    assertEquals(Long.valueOf(0), result.get().getOffset());
    assertEquals("2014-08-12T12:17:02.527142Z", result.get().getBeginAt().toString());
  }

  @Test
  public void destroy() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, fake.chain1.getId());

    Assert.assertNotExist(testDAO, fake.chain1.getId());
  }

  @Test
  public void destroy_SucceedsEvenWithChildren() throws Exception {
    Access access = Access.create("Admin");
    test.insert(Library.create(fake.account1, "nerds", InternalResources.now()));

    try {
      testDAO.destroy(access, fake.chain1.getId());

    } catch (Exception e) {
      Chain result = testDAO.readOne(Access.internal(), fake.chain1.getId());
      assertNotNull(result);
      throw e;
    }
  }

  @Test
  public void erase_inDraftState() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    Access access = Access.create("Admin");

    testDAO.erase(access, fake.chain3.getId());

    Chain result = testDAO.readOne(Access.internal(), fake.chain3.getId());
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
  }

  @Test
  public void erase_inCompleteState() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Complete, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    Access access = Access.create("Admin");

    testDAO.erase(access, fake.chain3.getId());

    Chain result = testDAO.readOne(Access.internal(), fake.chain3.getId());
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
  }

  /**
   [#150279533] Engineer expects when Chain is set to erase state, its `embed_key` is set to null immediately, in order to prevent public consumption of a chain in erase state, and enable a new chain to be createdimmediately with that `embed_key`
   */
  @Test
  public void erase_removesEmbedKey() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Complete, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "play_me"));
    Access access = Access.create("Admin");

    testDAO.erase(access, fake.chain3.getId());

    Chain result = testDAO.readOne(Access.internal(), fake.chain3.getId());
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
    assertNull(result.getEmbedKey());
  }

  @Test
  public void erase_inFailedState() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Failed, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    Access access = Access.create("Admin");

    testDAO.erase(access, fake.chain3.getId());

    Chain result = testDAO.readOne(Access.internal(), fake.chain3.getId());
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
  }

  @Test
  public void erase_failsInFabricateState() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    Access access = Access.create("Admin");

    failure.expect(CoreException.class);
    failure.expectMessage("transition to Erase not in allowed (Fabricate,Failed,Complete)");

    testDAO.erase(access, fake.chain3.getId());
  }

  @Test
  public void erase_failsInReadyState() throws Exception {
    fake.chain3 = test.insert(Chain.create(fake.account1, "bucket", ChainType.Production, ChainState.Ready, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null));
    Access access = Access.create("Admin");

    failure.expect(CoreException.class);
    failure.expectMessage("transition to Erase not in allowed (Draft,Ready,Fabricate)");

    testDAO.erase(access, fake.chain3.getId());
  }

}
