// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.CancelException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ChainIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  @Mock AmazonProvider amazonProvider;
  private Injector injector;
  private ChainDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // segment waveform config
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertChain(2, 1, "bucket", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);

    // Account "boat" has no chains
    IntegrationTestEntity.insertAccount(2, "boat");

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainDAO.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
    injector = null;

    System.clearProperty("segment.file.bucket");
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void create_withEmbedKey() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setEmbedKey("my $% favorite THINGS")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals("my_favorite_things", result.getEmbedKey());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void create_withEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), "my_favorite_things");
    failure.expect(BusinessException.class);
    failure.expectMessage("Found Existing Chain with this embed_key");

    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setEmbedKey("my_favorite_things")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_PreviewType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setType("Preview")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Preview, result.getType());
    // future test: time from startAt to stopAt relative to [#190] specs
//    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.getStartAt());
//    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.getStopAt());
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_alwaysCreatedInDraftState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2009-08-12 12:17:02.527142");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.getStartAt());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("");

    Chain result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("manuts", result.getName());
    assertEquals(ChainState.Draft, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.getStartAt());
    assertNull(result.getStopAt());
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setName("manuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("bullshit state")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("'bullshit state' is not a valid state");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Chain result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
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
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.getStopAt());
  }

  /**
   [#150279540] Unauthenticated or specifically-authenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.
   */
  @Test
  public void readOne_byEmbedKey_unauthenticatedOk() throws Exception {
    IntegrationTestEntity.insertChain(102, 1, "cats test", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), "cats");
    Access access = Access.unauthenticated();

    Chain result = testDAO.readOne(access, "cats");

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(102L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals("cats test", result.getName());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.getStopAt());
    assertEquals("cats", result.getEmbedKey());
  }

  @Test
  public void readOne_toJSONObject() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONObject result = JSON.objectFrom(testDAO.readOne(access, BigInteger.valueOf(2L)));

    assertNotNull(result);
    assertEquals(2, result.get("id"));
    assertEquals(1, result.get("accountId"));
    assertEquals("bucket", result.get("name"));
    assertEquals("2015-05-10 12:17:02.527142Z", result.get("startAt"));
    assertEquals("2015-06-09 12:17:01.047563Z", result.get("stopAt"));
    assertEquals("Fabricate", result.get("state"));
    assertEquals("Production", result.get("type"));
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Chain result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(2L, (long) result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("school", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("bucket", result2.get("name"));
  }

  @Test
  public void readAll_excludesChainsInEraseState() throws Exception {
    IntegrationTestEntity.insertChain(17, 1, "sham", ChainType.Production, ChainState.Erase, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(2L, (long) result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("school", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("bucket", result2.get("name"));
  }

  @Test
  public void readAllInState() throws Exception {
    Collection<Chain> result = testDAO.readAllInState(Access.internal(), ChainState.Fabricate);

    assertNotNull(result);
    assertEquals(1L, (long) result.size());
    Chain result0 = result.iterator().next();

    assertEquals("bucket", result0.getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(0L, (long) result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test
  public void update_addEmbedKey() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setEmbedKey("twenty %$** four HOURS")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals("twenty_four_hours", result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test
  public void update_removeEmbedKey() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), "twenty_four_hours");
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setEmbedKey("")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertNull(result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test
  public void update_addEmbedKey_failsIfEmbedKeyAlreadyExists() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), "twenty_four_hours");
    failure.expect(BusinessException.class);
    failure.expectMessage("Found Existing Chain with this embed_key");

    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setEmbedKey("twenty_four_hours")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_okayWithEmbedKey() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertChain(274, 1, "school", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), "jabberwocky");
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(274L))
      .setName("manuts")
      .setType("Production")
      .setState("Ready")
      .setEmbedKey("jabberwocky")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, BigInteger.valueOf(274L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(274L));
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals("jabberwocky", result.getEmbedKey());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Ready, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }


  @Test
  public void update_cannotChangeType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Preview")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("bucket")
      .setType("Production")
      .setState("Fabricate")
      .setStartAt("2015-05-10 12:17:03.527142")
      .setStopAt("2015-06-09 12:17:01.047563");
    IntegrationTestEntity.insertSegment(6, 2, 5, SegmentState.Crafted, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120.0, "chain-1-segment-97898asdf7892.wav");

    failure.expect(BusinessException.class);
    failure.expectMessage("cannot change chain startAt time after it has segments");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_canChangeName_whenChainsHasSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Fabricate")
      .setType("Production")
      .setStartAt("2015-05-10 12:17:02.527142")
      .setStopAt("2015-06-09 12:17:01.047563");
    IntegrationTestEntity.insertSegment(6, 2, 5, SegmentState.Crafted, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120.0, "chain-1-segment-97898asdf7892.wav");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAccountId());
    assertEquals(ChainState.Complete, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertNull(result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setName("manuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_CannotChangeAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(75L))
      .setName("manuts")
      .setState("Complete")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

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

    failure.expect(BusinessException.class);
    failure.expectMessage("must have either top-level or account access");

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

    failure.expect(BusinessException.class);
    failure.expectMessage("Engineer role is required");

    testDAO.updateState(access, BigInteger.valueOf(2L), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Preview, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("Artist or Engineer role is required");

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Complete);
  }

  @Test
  public void updateState_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null, null);
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist,Engineer",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Sequence, or Instrument");

    testDAO.updateState(access, BigInteger.valueOf(3L), ChainState.Ready);
  }

  @Test
  public void updateState_outOfDraft_BoundToLibrary() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLibrary(3, 1, "pajamas");
    IntegrationTestEntity.insertChainLibrary(1, 3, 3);

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
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLibrary(3, 1, "pajamas");
    IntegrationTestEntity.insertSequence(3, 3, 3, SequenceType.Main, SequenceState.Published, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertChainSequence(1, 3, 3);

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
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLibrary(3, 1, "pajamas");
    IntegrationTestEntity.insertSequence(3, 3, 3, SequenceType.Main, SequenceState.Published, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertSequence(4, 3, 3, SequenceType.Macro, SequenceState.Published, "trees A to B", 0.7, "D#", 0.4);
    IntegrationTestEntity.insertSequence(5, 3, 3, SequenceType.Macro, SequenceState.Published, "trees B to A", 0.6, "F", 0.6);
    IntegrationTestEntity.insertSequence(6, 3, 3, SequenceType.Rhythm, SequenceState.Published, "beets", 0.5, "C", 1.5);
    IntegrationTestEntity.insertChainSequence(1, 3, 3);
    IntegrationTestEntity.insertChainSequence(2, 3, 4);
    IntegrationTestEntity.insertChainSequence(3, 3, 5);
    IntegrationTestEntity.insertChainSequence(4, 3, 6);

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
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLibrary(3, 1, "pajamas");
    IntegrationTestEntity.insertInstrument(3, 3, 3, "fonds", InstrumentType.Harmonic, 0.342);
    IntegrationTestEntity.insertChainInstrument(1, 3, 3);

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
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"), null);
    IntegrationTestEntity.insertSegment(6, 12, 5, SegmentState.Crafting, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 12:04:10.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAtTimestamp(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAtTimestamp(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 11:53:40.000001"));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12L), result.getChainId());
    assertEquals(BigInteger.valueOf(6L), result.getOffset());
    assertEquals(Timestamp.valueOf("2014-02-14 12:04:10.000001"), result.getBeginAt());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"), null);
    IntegrationTestEntity.insertSegment(6, 12, 5, SegmentState.Crafting, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStopAtTimestamp(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAtTimestamp(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 13:53:50.000001"));

    assertNull(result);
    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butLastSegmentNotDubbedSoChainNotComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"), null);
    IntegrationTestEntity.insertSegment(6, 12, 5, SegmentState.Dubbing, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAtTimestamp(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAtTimestamp(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));
    assertNull(result);

    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"), null);
    IntegrationTestEntity.insertSegment(6, 12, 5, SegmentState.Dubbed, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAtTimestamp(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAtTimestamp(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));
    assertNull(result);

    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Complete, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsReadyForNextSegment_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"), null);
    IntegrationTestEntity.insertSegment(6, 12, 5, SegmentState.Dubbed, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAtTimestamp(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));
    assertNotNull(result);

    Chain resultFinal = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));
    assertNotNull(resultFinal);
    assertEquals(ChainState.Fabricate, resultFinal.getState());
  }

  @Test
  public void buildNextSegmentOrComplete_chainWithSegmentsAlreadyHasNextSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertSegment_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(1L));
    fromChain.setStartAtTimestamp(Timestamp.valueOf("2015-02-14 12:03:40.000001"));
    fromChain.setStopAt(null);
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNull(result);
  }

  @Test
  public void buildNextSegmentOrComplete_chainEndingInCraftedSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertSegment(6, 12, 5, SegmentState.Crafted, Timestamp.valueOf("2014-08-12 14:03:08.000001"), Timestamp.valueOf("2014-08-12 14:03:38.000001"), "A major", 64, 0.52, 120.0, "chain-1-segment-97898asdf7892.wav");

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAtTimestamp(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAtTimestamp(Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12L), result.getChainId());
    assertEquals(BigInteger.valueOf(6L), result.getOffset());
    assertEquals(Timestamp.valueOf("2014-08-12 14:03:38.000001"), result.getBeginAt());
  }

  @Test
  public void buildNextSegmentOrComplete_newEmptyChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    Chain fromChain = new Chain();
    fromChain.setId(BigInteger.valueOf(12L));
    fromChain.setStartAtTimestamp(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAt(null);
    Segment result = testDAO.buildNextSegmentOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12L), result.getChainId());
    assertEquals(BigInteger.valueOf(0L), result.getOffset());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getBeginAt());
  }

  @Test
  public void destroy() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    Chain result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

  @Test
  public void destroy_SucceedsEvenWithChildren() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertLibrary(1, 1, "nerds");
    IntegrationTestEntity.insertChainConfig(101, 1, ChainConfigType.OutputSampleBits, "3");

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
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);
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
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Complete, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);
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
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Complete, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), "play_me");
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
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Failed, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);
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
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Erase not in allowed (Fabricate,Failed,Complete)");

    testDAO.erase(access, BigInteger.valueOf(3L));
  }

  @Test
  public void erase_failsInReadyState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"), null);
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Erase not in allowed (Draft,Ready,Fabricate)");

    testDAO.erase(access, BigInteger.valueOf(3L));
  }

}
