// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.work.WorkManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChainIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  @Mock
  WorkManager workManager;
  private ChainDAO testDAO;

  @Before
  public void setUp() throws Exception {
    reset();
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
          bind(WorkManager.class).toInstance(workManager);
        }
      }));

    // segment waveform config
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Account "fish" has chain "school" and chain "bucket"
    insert(newAccount(1, "fish"));
    insert(newChain(1, 1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null, now(), newChainBinding("Library", 1)));
    insert(newChain(2, 1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now(), newChainBinding("Library", 1)));

    // Account "boat" has no chains
    insert(newAccount(2, "boat"));

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainDAO.class);
  }

  @After
  public void tearDown() {
    System.clearProperty("segment.file.bucket");
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_withEmbedKey() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setState("Draft")
      .setEmbedKey("my $% favorite THINGS")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("my_favorite_things", result.getEmbedKey());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_withEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "my_favorite_things", now()));
    failure.expect(CoreException.class);
    failure.expectMessage("Found Existing Chain with this embed_key");

    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
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
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setState("Draft")
      .setType("Preview")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Preview, result.getType());
    // future test: time from startAt to stopAt relative to [#190] specs
//    assertEquals("2009-08-12 12:17:02.527142", result.getStartAt().toString());
//    assertEquals("2009-09-11 12:17:01.047563", result.getStopAt().toString());
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_createdInDraftState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain(BigInteger.valueOf(27))
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");
    inputData.add(newChainBinding("Library", 2));

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2009-08-12T12:17:02.527142Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("coconuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.527142Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
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
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
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
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Chain result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
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
    insert(newChain(102, 1, "cats test", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "cats", now()));
    Access access = Access.unauthenticated();

    Chain result = testDAO.readOne(access, "cats");

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(102L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("cats test", result.getName());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
    assertEquals("cats", result.getEmbedKey());
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Chain> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_excludesChainsInEraseState() throws Exception {
    insert(newChain(17, 1, "sham", ChainType.Production, ChainState.Erase, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Chain> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

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
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    Collection<Chain> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_addEmbedKey() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setType("Production")
      .setEmbedKey("twenty %$** four HOURS")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("twenty_four_hours", result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_removeEmbedKey() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours", now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setEmbedKey("")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertNull(result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_addEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "twenty_four_hours", now()));
    failure.expect(CoreException.class);
    failure.expectMessage("Found Existing Chain with this embed_key");

    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setType("Production")
      .setEmbedKey("twenty_four_hours")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_okayWithEmbedKey() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    insert(newChain(274, 1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky", now(), newChainBinding("Library", 2)));
    Chain inputData = chainFactory.newChain(BigInteger.valueOf(27))
      .setAccountId(BigInteger.valueOf(274L))
      .setName("coconuts")
      .setType("Production")
      .setState("Ready")
      .setEmbedKey("jabberwocky")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");
    inputData.add(newChainBinding("Library", 2));

    testDAO.update(access, BigInteger.valueOf(274L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(274L));
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Ready, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }


  @Test
  public void update_cannotChangeType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setType("Preview")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z")
      .setStopAt("2009-09-11T12:17:01.989941Z");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertEquals("2009-09-11T12:17:01.989941Z", result.getStopAt().toString());
  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain(BigInteger.valueOf(27))
      .setAccountId(BigInteger.valueOf(1L))
      .setName("bucket")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2015-05-10T12:17:03.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z");
    inputData.add(newChainBinding("Library", 2));
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(2))
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    failure.expect(CoreException.class);
    failure.expectMessage("cannot change chain startAt time after it has segments");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_canChangeName_whenChainsHasSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain(BigInteger.valueOf(27))
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setState("Fabricate")
      .setType("Production")
      .setStartAt("2015-05-10T12:17:02.527142Z")
      .setStopAt("2015-06-09T12:17:01.047563Z");
    inputData.add(newChainBinding("Library", 2));
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(2))
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2015-05-10T12:18:02.527142Z")
      .setUpdatedAt("2015-05-10T12:18:32.527142Z"));

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2015-05-10T12:17:02.527142Z", result.getStartAt().toString());
    assertEquals("2015-06-09T12:17:01.047563Z", result.getStopAt().toString());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("coconuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12T12:17:02.687327Z");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("coconuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("2009-08-12T12:17:02.687327Z", result.getStartAt().toString());
    assertNull(result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setName("coconuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(1L))
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_CannotChangeAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = chainFactory.newChain()
      .setAccountId(BigInteger.valueOf(75L))
      .setName("coconuts")
      .setState("Complete")
      .setType("Production")
      .setStartAt("2009-08-12T12:17:02.527142Z")
      .setStopAt("2009-09-11T12:17:01.047563Z");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));

    testDAO.updateState(access, BigInteger.valueOf(2L), ChainState.Complete);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals(ChainState.Complete, result.getState());
  }

  @Test
  public void updateState_WithoutAccountAccess_Fails() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "54"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.updateState(access, BigInteger.valueOf(2L), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, BigInteger.valueOf(2L), ChainState.Complete);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals(ChainState.Complete, result.getState());
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutEngineerRole_ForProductionChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("Engineer role is required");

    testDAO.updateState(access, BigInteger.valueOf(2L), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Preview, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("Artist or Engineer role is required");

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Complete);
  }

  @Test
  public void updateState_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist,Engineer",
      "accounts", "1"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Ready);
  }

  @Test
  public void updateState_outOfDraft_BoundToLibrary() throws Exception {
    Chain chain = newChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null, now());
    insert(newLibrary(3, 1, "pajamas", now()));
    chain.add(newChainBinding("Library", 3));
    insert(chain);

    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist,Engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToSequence() throws Exception {
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(newLibrary(3, 1, "pajamas", now()));
    Program program = newProgram(3, 3, 3, ProgramType.Rhythm, ProgramState.Published, "fonds", "C#", 0.286, now());
    Chain chain = newChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null, now());
    chain.add(newChainBinding(program));
    insert(chain);
    insert(program);

    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist,Engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToMultipleSequences() throws Exception {
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    insert(newLibrary(3, 1, "pajamas", now()));
    Chain chain = newChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null, now());
    chain.add(newChainBinding(insert(newProgram(3, 3, 3, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()))));
    chain.add(newChainBinding(insert(newProgram(4, 3, 3, ProgramType.Macro, ProgramState.Published, "trees A to B", "D#", 120.0, now()))));
    chain.add(newChainBinding(insert(newProgram(5, 3, 3, ProgramType.Macro, ProgramState.Published, "trees B to A", "F", 120.0, now()))));
    chain.add(newChainBinding(insert(newProgram(6, 3, 3, ProgramType.Rhythm, ProgramState.Published, "beets", "C", 120.0, now()))));
    insert(chain);

    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist,Engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToInstrument() throws Exception {
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    Chain chain = newChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), null, null, now());
    insert(newLibrary(3, 1, "pajamas", now()));
    chain.add(newChainBinding(insert(newInstrument(3, 3, 3, InstrumentType.Harmonic, InstrumentState.Published, "fonds", now()))));
    insert(chain);

    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist,Engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Ready);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Ready, result.getState());
  }

  /**
   [#160299309] Engineer wants a *revived* action for a live production chain, in case the chain has become stuck, in order to ensure the Chain remains in an operable state.
   */
  @Test
  public void revive() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Admin,Artist,Engineer",
      "accounts", "1,2"
    ));

    Chain chain = newChain(274, 1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky", now());
    chain.add(newChainBinding(insert(newLibrary(3, 1, "pajamas", now()))));
    insert(chain);

    Chain result = testDAO.revive(access, BigInteger.valueOf(274L));

    assertNotNull(result);
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    verify(workManager).startChainFabrication(result.getId());

    Chain priorChain = testDAO.readOne(Access.internal(), BigInteger.valueOf(274L));
    assertNotNull(priorChain);
    assertEquals(ChainState.Failed, priorChain.getState());
    assertNull(priorChain.getEmbedKey());
    verify(workManager).stopChainFabrication(priorChain.getId());

    assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readAllPreviousDays(Access.internal(), 1).size());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require exists chain from which to revived, throw error if not found.
   */
  @Test
  public void revive_failsIfNotExistPriorChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.revive(access, BigInteger.valueOf(274L));
  }

  /**
   [#160299309] Engineer wants a *revived* action, throw error if trying to revived from chain that is not production in fabricate state
   */
  @Test
  public void revive_failsIfNotFabricateState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain chain = newChain(274, 1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky", now());
    chain.add(newChainBinding(insert(newLibrary(3, 1, "pajamas", now()))));
    insert(chain);
    failure.expect(CoreException.class);
    failure.expectMessage("Only a Fabricate-state Chain can be revived.");

    testDAO.revive(access, BigInteger.valueOf(274L));
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_okayWithEngineerAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "1"
    ));
    Chain chain = newChain(274, 1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky", now());
    chain.add(newChainBinding(insert(newLibrary(3, 1, "pajamas", now()))));
    insert(chain);

    Chain result = testDAO.revive(access, BigInteger.valueOf(274L));

    assertNotNull(result);
    assertEquals("school", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
  }

  /**
   [#160299309] Engineer wants a *revived* action, require engineer access or top level
   */
  @Test
  public void revive_failsWithArtistAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist"
    ));
    Chain chain = newChain(274, 1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky", now());
    chain.add(newChainBinding(insert(newLibrary(3, 1, "pajamas", now()))));
    insert(chain);
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.revive(access, BigInteger.valueOf(274L));
  }

  /**
   [#160299309] Engineer wants a *revived* action, duplicates all ChainBindings, including ChainConfig, ChainInstrument, ChainLibrary, and ChainSequence
   */
  @Test
  public void revive_duplicatesAllChainBindings() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "1"
    ));
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif"));
    Chain chain = newChain(274, 1, "school", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), "jabberwocky", now());
    chain.add(newChainConfig(ChainConfigType.OutputFrameRate, "1,4,35"));
    chain.add(newChainConfig(ChainConfigType.OutputChannels, "2,83,4"));
    chain.add(newChainBinding(insert(newLibrary(3, 1, "pajamas", now()))));

    chain.add(newChainBinding(insert(newProgram(3, 3, 3, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()))));
    chain.add(newChainBinding(insert(newProgram(4, 3, 3, ProgramType.Macro, ProgramState.Published, "trees A to B", "D#", 120.0, now()))));
    chain.add(newChainBinding(insert(newProgram(5, 3, 3, ProgramType.Macro, ProgramState.Published, "trees B to A", "F", 120.0, now()))));
    chain.add(newChainBinding(insert(newProgram(6, 3, 3, ProgramType.Rhythm, ProgramState.Published, "beets", "C", 120.0, now()))));
    chain.add(newChainBinding(insert(newInstrument(3, 3, 3, InstrumentType.Harmonic, InstrumentState.Published, "fonds", now()))));
    insert(chain);

    Chain result = testDAO.revive(access, BigInteger.valueOf(274L));

    assertNotNull(result);
    assertEquals(2, result.getConfigs().size());
    assertEquals(1, result.getBindings().stream().filter(binding -> "Instrument".equals(binding.getTargetClass())).count());
    assertEquals(1, result.getBindings().stream().filter(binding -> "Library".equals(binding.getTargetClass())).count());
    assertEquals(4, result.getBindings().stream().filter(binding -> "Program".equals(binding.getTargetClass())).count());
  }

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   */
  @Test
  public void checkAndReviveAll() throws Exception {
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(2))
      .setOffset(5L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("A major")
      .setTotal(64)
      .setDensity(0.52)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2015-05-10T12:18:02.527142Z")
      .setUpdatedAt("2015-05-10T12:18:32.527142Z"));
    insert(newLibrary(3, 1, "pajamas", now()));

    Collection<Chain> results = testDAO.checkAndReviveAll(Access.internal());

    assertEquals(1, results.size());
    Chain result = results.iterator().next();
    assertEquals("bucket", result.getName());
    assertNull(result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    verify(workManager).startChainFabrication(result.getId());

    Chain priorChain = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(priorChain);
    assertEquals(ChainState.Failed, priorChain.getState());
    assertNull(priorChain.getEmbedKey());
    verify(workManager).stopChainFabrication(priorChain.getId());

    assertEquals(1, injector.getInstance(PlatformMessageDAO.class).readAllPreviousDays(Access.internal(), 1).size());
  }


  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    Chain fromChain = insert(newChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null, now(), newChainBinding("Library", 2)));
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(12))
      .setOffset(5L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:04:10.000001Z")
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T11:53:40.000001Z"));

    assertTrue(result.isPresent());
    assertEquals(BigInteger.valueOf(12L), result.get().getChainId());
    assertEquals(Long.valueOf(6), result.get().getOffset());
    assertEquals("2014-02-14T12:04:10.000001Z", result.get().getBeginAt().toString());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    insert(newChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null, now()));
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(12))
      .setOffset(5L)
      .setBeginAt("2014-02-14T12:03:40.000001Z")
      .setEndAt("2014-02-14T12:03:50.000001Z")
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Chain fromChain = chainFactory.newChain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAt("2014-02-14T12:03:40.000001Z");
    fromChain.setStopAt("2014-02-14T14:03:40.000001Z")
      .setCreatedAt("2014-02-14T14:03:15.000001Z")
      .setUpdatedAt("2014-02-14T14:03:45.000001Z");
    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T13:53:50.000001Z"));

    assertTrue(result.isPresent());
    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butLastSegmentNotDubbedSoChainNotComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    insert(newChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null, now()));
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(12))
      .setOffset(5L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Chain fromChain = chainFactory.newChain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAt("2014-02-14T12:03:40.000001Z");
    fromChain.setStopAt("2014-02-14T14:03:40.000001Z")
      .setCreatedAt("2014-02-14T14:03:15.000001Z")
      .setUpdatedAt("2014-02-14T14:03:45.000001Z");
    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));
    assertTrue(result.isEmpty());
    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    Chain fromChain = insert(newChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null, now(), newChainBinding("Library", 1)));
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(12))
      .setBeginAt("2014-02-14T14:03:35.000001Z")
      .setEndAt("2014-02-14T14:03:55.000001Z")
      .setOffset(5L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));
    assertTrue(result.isEmpty());
    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Complete, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    Chain chain = newChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-02-14T12:03:40.000001Z"), Instant.parse("2014-02-14T14:03:40.000001Z"), null, now());
    chain.setCreatedAt("2014-02-14T14:03:15.000001Z").setUpdatedAt("2014-02-14T14:03:45.000001Z");
    insert(chain);
    insert(segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(12))
      .setBeginAt("2014-02-14T14:03:15.000001Z")
      .setEndAt("2014-02-14T14:03:25.000001Z")
      .setOffset(5L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, chain, Instant.parse("2014-02-14T14:03:50.000001Z"), Instant.parse("2014-02-14T14:15:50.000001Z"));
    assertTrue(result.isPresent());
    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsAlreadyHasNextSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    insert(newSegment(5, 1, 4, Instant.parse("2017-02-14T12:03:08.000001Z")));

    Chain fromChain = chainFactory.newChain(BigInteger.valueOf(1));
    fromChain.setStartAt("2015-02-14T12:03:40.000001Z");
    fromChain.setStopAt(null);
    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertFalse(result.isPresent());
  }

  @Test
  public void buildNextSegmentOrComplete_chainEndingInCraftedSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    Chain fromChain = insert(newChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null, now()));
    insert(newSegment(6, 12, 5, SegmentState.Crafted, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T14:03:38.000001Z"), "A major", 64, 0.52, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));

    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertTrue(result.isPresent());
    assertEquals(BigInteger.valueOf(12L), result.get().getChainId());
    assertEquals(Long.valueOf(6), result.get().getOffset());
    assertEquals("2014-08-12T14:03:38.000001Z", result.get().getBeginAt().toString());
  }

  @Test
  public void buildNextSegmentOrComplete_newEmptyChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    insert(newChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now()));

    Chain fromChain = chainFactory.newChain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAt("2014-08-12T12:17:02.527142Z");
    fromChain.setStopAt(null);
    Optional<Segment> result = testDAO.buildNextSegmentOrComplete(access, fromChain, Instant.parse("2014-08-12T14:03:38.000001Z"), Instant.parse("2014-08-12T13:53:38.000001Z"));

    assertTrue(result.isPresent());
    assertEquals(BigInteger.valueOf(12L), result.get().getChainId());
    assertEquals(Long.valueOf(0), result.get().getOffset());
    assertEquals("2014-08-12T12:17:02.527142Z", result.get().getBeginAt().toString());
  }

  @Test
  public void destroy() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test
  public void destroy_SucceedsEvenWithChildren() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    insert(newLibrary(1, 1, "nerds", now()));

    try {
      testDAO.destroy(access, BigInteger.valueOf(1L));

    } catch (Exception e) {
      Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
      assertNotNull(result);
      throw e;
    }
  }

  @Test
  public void erase_inDraftState() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.erase(access, BigInteger.valueOf(3L));

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
  }

  @Test
  public void erase_inCompleteState() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Complete, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.erase(access, BigInteger.valueOf(3L));

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
  }

  /**
   [#150279533] Engineer expects when Chain is set to erase state, its `embed_key` is set to null immediately, in order to prevent public consumption of a chain in erase state, and enable a new chain to be created immediately with that `embed_key`
   */
  @Test
  public void erase_removesEmbedKey() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Complete, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), "play_me", now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.erase(access, BigInteger.valueOf(3L));

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
    assertNull(result.getEmbedKey());
  }

  @Test
  public void erase_inFailedState() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Failed, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.erase(access, BigInteger.valueOf(3L));

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(ChainState.Erase, result.getState());
  }

  @Test
  public void erase_failsInFabricateState() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("transition to Erase not in allowed (Fabricate,Failed,Complete)");

    testDAO.erase(access, BigInteger.valueOf(3L));
  }

  @Test
  public void erase_failsInReadyState() throws Exception {
    insert(newChain(3, 1, "bucket", ChainType.Production, ChainState.Ready, Instant.parse("2015-05-10T12:17:02.527142Z"), Instant.parse("2015-06-09T12:17:01.047563Z"), null, now()));
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("transition to Erase not in allowed (Draft,Ready,Fabricate)");

    testDAO.erase(access, BigInteger.valueOf(3L));
  }

}
