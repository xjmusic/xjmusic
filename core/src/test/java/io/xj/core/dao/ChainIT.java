// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.CancelException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.LinkState;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.transport.JSON;

import org.jooq.Result;
import org.jooq.types.ULong;

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

import static io.xj.core.Tables.CHAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class ChainIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private ChainDAO testDAO;
  @Mock AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // inject mocks
    createInjector();

    // link waveform config
    System.setProperty("link.file.bucket", "xj-link-test");

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertChain(2, 1, "bucket", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

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

    System.clearProperty("link.file.bucket");
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1L), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(ChainState.Draft, result.get("state"));
    assertEquals(ChainType.Production, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.get("stopAt"));
  }

  @Test
  public void create_PreviewType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setType("Preview")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1L), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(ChainState.Draft, result.get("state"));
    assertEquals(ChainType.Preview, result.get("type"));
    // TODO: test time from startAt to stopAt relative to [#190] specs
//    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
//    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.get("stopAt"));
  }

  @Test
  // [#126] Chains are always readMany in DRAFT state
  public void create_alwaysCreatedInDraftState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Fabricating")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1L), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(ChainType.Production, result.get("type"));
    assertEquals(ChainState.Draft, result.get("state"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.get("stopAt"));
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Draft")
      .setStartAt("2009-08-12 12:17:02.527142");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1L), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(ChainState.Draft, result.get("state"));
    assertEquals(ChainType.Production, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertFalse(result.has("stopAt"));
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1L), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(ChainState.Draft, result.get("state"));
    assertEquals(ChainType.Production, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertFalse(result.has("stopAt"));
  }

  @Test
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
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
      "roles", "admin"
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
      "roles", "user",
      "accounts", "326"
    ));

    ChainRecord result = testDAO.readOne(access, ULong.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Chain result = new Chain().setFromRecord(testDAO.readOne(access, ULong.valueOf(2L)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2L), result.getId());
    assertEquals(ULong.valueOf(1L), result.getAccountId());
    assertEquals("bucket", result.getName());
    assertEquals(ChainState.Fabricating, result.getState());
    assertEquals(ChainType.Production, result.getType());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    ChainRecord result = testDAO.readOne(access, ULong.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1L)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("school", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("bucket", result2.get("name"));
  }

  @Test
  public void readAll_excludesChainsInEraseState() throws Exception {
    IntegrationTestEntity.insertChain(17,1,"sham",ChainType.Production,ChainState.Erase,Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1L)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("school", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("bucket", result2.get("name"));
  }

  @Test
  public void readAllInState() throws Exception {
    JSONArray result = JSON.arrayOf(testDAO.readAllInState(Access.internal(), ChainState.Fabricating, 1));

    assertNotNull(result);
    assertEquals(1, result.length());
    JSONObject result2 = (JSONObject) result.get(0);
    assertEquals("bucket", result2.get("name"));
  }

  @Test
  public void readAllRecordsInStateFabricating() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);

    Result<ChainRecord> actualResults = testDAO.readAllInStateFabricating(access, Timestamp.valueOf("2015-05-20 12:00:00"));

    assertNotNull(actualResults);
    assertEquals(2, actualResults.size());
    ChainRecord result1 = actualResults.get(0);
    assertEquals(ULong.valueOf(2L), result1.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result1.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result1.getStopAt());
    ChainRecord result2 = actualResults.get(1);
    assertEquals(ULong.valueOf(4L), result2.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result2.getStartAt());
    assertNull(result2.getStopAt());
  }

  @Test
  public void readAllIdBoundsInStateFabricating_ReturnsChainBeforeBoundary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllInStateFabricating(access, Timestamp.valueOf("2016-05-20 12:00:00"));

    assertNotNull(actualResults);
    assertEquals(2, actualResults.size());
  }

  @Test
  public void readAllIdBoundsInStateFabricating_DoesNotReturnChainAfterBoundary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2015-06-10 12:17:02.527142"), Timestamp.valueOf("2015-06-12 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllInStateFabricating(access, Timestamp.valueOf("2015-05-20 12:00:00"));

    assertNotNull(actualResults);

    assertEquals(1, actualResults.size());
    ChainRecord result1 = actualResults.get(0);
    assertEquals(ULong.valueOf(2L), result1.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result1.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result1.getStopAt());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, ULong.valueOf(2L), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1L), result.getAccountId());
    assertEquals("Complete", result.getState());
    assertEquals("Production", result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test
  public void update_cannotChangeType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Preview")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, ULong.valueOf(2L), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1L), result.getAccountId());
    assertEquals("Complete", result.getState());
    assertEquals("Production", result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test
  public void update_failsToChangeStartAt_whenChainsHasLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("bucket")
      .setType("Production")
      .setState("Fabricating")
      .setStartAt("2015-05-10 12:17:03.527142")
      .setStopAt("2015-06-09 12:17:01.047563");
    IntegrationTestEntity.insertLink(6, 2, 5, LinkState.Crafted, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120, "chain-1-link-97898asdf7892.wav");

    failure.expect(BusinessException.class);
    failure.expectMessage("cannot change chain startAt time after it has links");

    testDAO.update(access, ULong.valueOf(2L), inputData);
  }

  @Test
  public void update_canChangeName_whenChainsHasLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setState("Fabricating")
      .setType("Production")
      .setStartAt("2015-05-10 12:17:02.527142")
      .setStopAt("2015-06-09 12:17:01.047563");
    IntegrationTestEntity.insertLink(6, 2, 5, LinkState.Crafted, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120, "chain-1-link-97898asdf7892.wav");

    testDAO.update(access, ULong.valueOf(2L), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1L), result.getAccountId());
    assertEquals("Fabricating", result.getState());
    assertEquals("Production", result.get("type"));
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setName("manuts")
      .setType("Production")
      .setState("Complete")
      .setStartAt("2009-08-12 12:17:02.687327");

    testDAO.update(access, ULong.valueOf(2L), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1L), result.getAccountId());
    assertEquals("Complete", result.getState());
    assertEquals("Production", result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertNull(result.getStopAt());
  }

  @Test
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setName("manuts")
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, ULong.valueOf(2L), inputData);
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1L))
      .setState("Draft")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, ULong.valueOf(2L), inputData);
  }

  @Test
  public void update_CannotChangeAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(75L))
      .setName("manuts")
      .setState("Complete")
      .setType("Production")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    testDAO.update(access, ULong.valueOf(2L), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(1L), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));

    testDAO.updateState(access, ULong.valueOf(2L), ChainState.Complete);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("Complete", result.getState());
  }

  @Test
  public void updateState_WithoutAccountAccess_Fails() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "54"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("must have either top-level or account access");

    testDAO.updateState(access, ULong.valueOf(2L), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user,engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, ULong.valueOf(2L), ChainState.Complete);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("Complete", result.getState());
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutEngineerRole_ForProductionChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("engineer role is required");

    testDAO.updateState(access, ULong.valueOf(2L), ChainState.Complete);
  }

  @Test
  public void updateState_WithAccountAccess_FailsWithoutArtistOrEngineerRole_ForPreviewChain() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Preview, ChainState.Fabricating, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("artist or engineer role is required");

    testDAO.updateState(access, ULong.valueOf(3L), ChainState.Complete);
  }

  @Test
  public void updateState_outOfDraft_WithoutEntitiesBound_Fails() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);
    Access access = new Access(ImmutableMap.of(
      "roles", "user,artist,engineer",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain must be bound to at least one Library, Idea, or Instrument");

    testDAO.updateState(access, ULong.valueOf(3L), ChainState.Ready);
  }

  @Test
  public void updateState_outOfDraft_BoundToLibrary() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);
    IntegrationTestEntity.insertLibrary(3, 1, "pajamas");
    IntegrationTestEntity.insertChainLibrary(1, 3, 3);

    Access access = new Access(ImmutableMap.of(
      "roles", "user,artist,engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, ULong.valueOf(3L), ChainState.Ready);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("Ready", result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToIdea() throws Exception {
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);
    IntegrationTestEntity.insertLibrary(3, 1, "pajamas");
    IntegrationTestEntity.insertIdea(3, 3, 3, IdeaType.Main, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertChainIdea(1, 3, 3);

    Access access = new Access(ImmutableMap.of(
      "roles", "user,artist,engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, ULong.valueOf(3L), ChainState.Ready);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("Ready", result.getState());
  }

  @Test
  public void updateState_outOfDraft_BoundToInstrument() throws Exception {
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);
    IntegrationTestEntity.insertLibrary(3, 1, "pajamas");
    IntegrationTestEntity.insertInstrument(3, 3, 3, "fonds", InstrumentType.Harmonic, 0.342);
    IntegrationTestEntity.insertChainInstrument(1, 3, 3);

    Access access = new Access(ImmutableMap.of(
      "roles", "user,artist,engineer",
      "accounts", "1"
    ));

    testDAO.updateState(access, ULong.valueOf(3L), ChainState.Ready);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3L)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("Ready", result.getState());
  }


  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, LinkState.Crafting, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 12:04:10.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12L));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 11:53:40.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12L), result.get("chainId"));
    assertEquals(ULong.valueOf(6L), result.get("offset"));
    assertEquals(Timestamp.valueOf("2014-02-14 12:04:10.000001"), result.get("beginAt"));
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, LinkState.Crafting, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12L));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 13:53:50.000001"));

    assertNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12L)))
      .fetchOne();
    assertEquals("Fabricating", finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butLastLinkNotDubbedSoChainNotComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, LinkState.Dubbing, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12L));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12L)))
      .fetchOne();
    assertEquals("Fabricating", finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, LinkState.Dubbed, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12L));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12L)))
      .fetchOne();
    assertEquals("Complete", finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, LinkState.Dubbed, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12L));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNotNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12L)))
      .fetchOne();
    assertEquals("Fabricating", finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksAlreadyHasNextLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertLink_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(1L));
    fromChain.setStartAt(Timestamp.valueOf("2015-02-14 12:03:40.000001"));
    fromChain.setStopAt(null);
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNull(result);
  }

  @Test
  public void buildNextLinkOrComplete_chainEndingInCraftedLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(6, 12, 5, LinkState.Crafted, Timestamp.valueOf("2014-08-12 14:03:08.000001"), Timestamp.valueOf("2014-08-12 14:03:38.000001"), "A major", 64, 0.52, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12L));
    fromChain.setStartAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAt(Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12L), result.get("chainId"));
    assertEquals(ULong.valueOf(6L), result.get("offset"));
    assertEquals(Timestamp.valueOf("2014-08-12 14:03:38.000001"), result.get("beginAt"));
  }

  @Test
  public void buildNextLinkOrComplete_newEmptyChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12L));
    fromChain.setStartAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAt(null);
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12L), result.get("chainId"));
    assertEquals(0, result.get("offset"));
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.get("beginAt"));
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1L));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(1L)))
      .fetchOne();
    assertNull(result);
  }

  @Test
  public void delete_SucceedsEvenWithChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertLibrary(1, 1, "nerds");
    IntegrationTestEntity.insertChainConfig(101, 1, ChainConfigType.OutputSampleBits, "3");

    try {
      testDAO.delete(access, ULong.valueOf(1L));

    } catch (Exception e) {
      ChainRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(1L)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }

  @Test
  public void erase_inDraftState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Draft, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.erase(access, ULong.valueOf(3L));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3L)))
      .fetchOne();
    assertEquals("Erase", result.getState());
  }

  @Test
  public void erase_inCompleteState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Complete, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.erase(access, ULong.valueOf(3L));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3L)))
      .fetchOne();
    assertEquals("Erase", result.getState());
  }

  @Test
  public void erase_inFailedState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Failed, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.erase(access, ULong.valueOf(3L));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3L)))
      .fetchOne();
    assertEquals("Erase", result.getState());
  }

  @Test
  public void erase_failsInFabricatingState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Fabricating, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Erase not in allowed (Fabricating,Failed,Complete)");

    testDAO.erase(access, ULong.valueOf(3L));
  }

  @Test
  public void erase_failsInReadyState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Erase not in allowed (Draft,Ready,Fabricating)");

    testDAO.erase(access, ULong.valueOf(3L));
  }

}
