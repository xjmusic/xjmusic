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
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.link.Link;
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
  @Mock private AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // inject mocks
    createInjector();

    // link waveform config
    System.setProperty("link.file.bucket", "xj-link-test");

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertChain(2, 1, "bucket", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

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
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setState(Chain.DRAFT)
      .setType(Chain.PRODUCTION)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(Chain.DRAFT, result.get("state"));
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.get("stopAt"));
  }

  @Test
  public void create_PreviewType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setState(Chain.DRAFT)
      .setType(Chain.PREVIEW)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(Chain.DRAFT, result.get("state"));
    assertEquals(Chain.PREVIEW, result.get("type"));
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
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setType(Chain.PRODUCTION)
      .setState(Chain.FABRICATING)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Chain.DRAFT, result.get("state"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), result.get("stopAt"));
  }

  @Test
  public void create_WithoutStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setType(Chain.PRODUCTION)
      .setState(Chain.DRAFT)
      .setStartAt("2009-08-12 12:17:02.527142");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(Chain.DRAFT, result.get("state"));
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertFalse(result.has("stopAt"));
  }

  @Test
  public void create_WithEmptyStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setState(Chain.DRAFT)
      .setType(Chain.PRODUCTION)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
    assertEquals(Chain.DRAFT, result.get("state"));
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), result.get("startAt"));
    assertFalse(result.has("stopAt"));
  }

  @Test()
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setName("manuts")
      .setState(Chain.DRAFT)
      .setType(Chain.PRODUCTION)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputData);
  }

  @Test()
  public void create_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setType(Chain.PRODUCTION)
      .setState("bullshit state")
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("'bullshitstate' is not a valid state (draft,ready,fabricating,complete,failed,erase)");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    ChainRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Chain result = new Chain().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getAccountId());
    assertEquals("bucket", result.getName());
    assertEquals(Chain.FABRICATING, result.getState());
    assertEquals(Chain.PRODUCTION, result.getType());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void readOneJSONObject_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    ChainRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("school", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("bucket", result2.get("name"));
  }

  @Test
  public void readAll_excludesChainsInEraseState() throws Exception {
    IntegrationTestEntity.insertChain(17,1,"sham",Chain.PRODUCTION,Chain.ERASE,Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("school", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("bucket", result2.get("name"));
  }

  @Test
  public void readAllInState() throws Exception {
    JSONArray result = JSON.arrayOf(testDAO.readAllInState(Access.internal(), Chain.FABRICATING, 1));

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
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);

    Result<ChainRecord> actualResults = testDAO.readAllInStateFabricating(access, Timestamp.valueOf("2015-05-20 12:00:00"));

    assertNotNull(actualResults);
    assertEquals(2, actualResults.size());
    ChainRecord result1 = actualResults.get(0);
    assertEquals(ULong.valueOf(2), result1.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result1.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result1.getStopAt());
    ChainRecord result2 = actualResults.get(1);
    assertEquals(ULong.valueOf(4), result2.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result2.getStartAt());
    assertNull(result2.getStopAt());
  }

  @Test
  public void readAllIdBoundsInStateFabricating_ReturnsChainBeforeBoundary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllInStateFabricating(access, Timestamp.valueOf("2016-05-20 12:00:00"));

    assertNotNull(actualResults);
    assertEquals(2, actualResults.size());
  }

  @Test
  public void readAllIdBoundsInStateFabricating_DoesNotReturnChainAfterBoundary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-06-10 12:17:02.527142"), Timestamp.valueOf("2015-06-12 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllInStateFabricating(access, Timestamp.valueOf("2015-05-20 12:00:00"));

    assertNotNull(actualResults);

    assertEquals(1, actualResults.size());
    ChainRecord result1 = actualResults.get(0);
    assertEquals(ULong.valueOf(2), result1.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result1.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result1.getStopAt());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setType(Chain.PRODUCTION)
      .setState(Chain.COMPLETE)
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, ULong.valueOf(2), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1), result.getAccountId());
    assertEquals(Chain.COMPLETE, result.getState());
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test
  public void update_cannotChangeType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setType(Chain.PREVIEW)
      .setState(Chain.COMPLETE)
      .setStartAt("2009-08-12 12:17:02.687327")
      .setStopAt("2009-09-11 12:17:01.989941");

    testDAO.update(access, ULong.valueOf(2), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1), result.getAccountId());
    assertEquals(Chain.COMPLETE, result.getState());
    assertEquals(Chain.PRODUCTION, result.getType());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), result.getStopAt());
  }

  @Test()
  public void update_failsToChangeStartAt_whenChainsHasLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("bucket")
      .setType(Chain.PRODUCTION)
      .setState(Chain.FABRICATING)
      .setStartAt("2015-05-10 12:17:03.527142")
      .setStopAt("2015-06-09 12:17:01.047563");
    IntegrationTestEntity.insertLink(6, 2, 5, Link.CRAFTED, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120, "chain-1-link-97898asdf7892.wav");

    failure.expect(BusinessException.class);
    failure.expectMessage("cannot change chain startAt time after it has links");

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test
  public void update_canChangeName_whenChainsHasLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setState(Chain.FABRICATING)
      .setType(Chain.PRODUCTION)
      .setStartAt("2015-05-10 12:17:02.527142")
      .setStopAt("2015-06-09 12:17:01.047563");
    IntegrationTestEntity.insertLink(6, 2, 5, Link.CRAFTED, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120, "chain-1-link-97898asdf7892.wav");

    testDAO.update(access, ULong.valueOf(2), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1), result.getAccountId());
    assertEquals(Chain.FABRICATING, result.getState());
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.getStopAt());
  }

  @Test
  public void update_RemoveStopAt() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setName("manuts")
      .setType(Chain.PRODUCTION)
      .setState(Chain.COMPLETE)
      .setStartAt("2009-08-12 12:17:02.687327");

    testDAO.update(access, ULong.valueOf(2), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("manuts", result.getName());
    assertEquals(ULong.valueOf(1), result.getAccountId());
    assertEquals(Chain.COMPLETE, result.getState());
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), result.getStartAt());
    assertEquals(null, result.getStopAt());
  }

  @Test()
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setName("manuts")
      .setState(Chain.DRAFT)
      .setType(Chain.PRODUCTION)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test()
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(1))
      .setState(Chain.DRAFT)
      .setType(Chain.PRODUCTION)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test()
  public void update_CannotChangeAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Chain inputData = new Chain()
      .setAccountId(BigInteger.valueOf(75))
      .setName("manuts")
      .setState(Chain.COMPLETE)
      .setType(Chain.PRODUCTION)
      .setStartAt("2009-08-12 12:17:02.527142")
      .setStopAt("2009-09-11 12:17:01.047563");

    testDAO.update(access, ULong.valueOf(2), inputData);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getAccountId());
  }

  @Test
  public void updateState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));

    testDAO.updateState(access, ULong.valueOf(2), Chain.COMPLETE);

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(Chain.COMPLETE, result.getState());
  }

  @Test()
  public void updateState_FailsOnInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("'bullshit state' is not a valid state (draft,ready,fabricating,complete,failed,erase)");

    testDAO.updateState(access, ULong.valueOf(2), "bullshit state");
  }

  @Test()
  public void updateState_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "54"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("if not top level access, must provide account id");

    testDAO.updateState(access, ULong.valueOf(2), Chain.COMPLETE);
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 12:04:10.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 11:53:40.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.get("chainId"));
    assertEquals(ULong.valueOf(6), result.get("offset"));
    assertEquals(Timestamp.valueOf("2014-02-14 12:04:10.000001"), result.get("beginAt"));
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTING, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 13:53:50.000001"));

    assertNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12)))
      .fetchOne();
    assertEquals(Chain.FABRICATING, finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butLastLinkNotDubbedSoChainNotComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBING, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12)))
      .fetchOne();
    assertEquals(Chain.FABRICATING, finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBED, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12)))
      .fetchOne();
    assertEquals(Chain.COMPLETE, finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butCantKnowBecauseBoundsProvidedAreNull() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBED, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNotNull(result);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12)))
      .fetchOne();
    assertEquals(Chain.FABRICATING, finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksAlreadyHasNextLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertLink_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(1));
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
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTED, Timestamp.valueOf("2014-08-12 14:03:08.000001"), Timestamp.valueOf("2014-08-12 14:03:38.000001"), "A major", 64, 0.52, 120, "chain-1-link-97898asdf7892.wav");

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAt(Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.get("chainId"));
    assertEquals(ULong.valueOf(6), result.get("offset"));
    assertEquals(Timestamp.valueOf("2014-08-12 14:03:38.000001"), result.get("beginAt"));
  }

  @Test
  public void buildNextLinkOrComplete_newEmptyChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAt(null);
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.get("chainId"));
    assertEquals(0, result.get("offset"));
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.get("beginAt"));
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test()
  public void delete_SucceedsEvenWithChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertLibrary(1, 1, "nerds");
    IntegrationTestEntity.insertChainConfig(101, 1, ChainConfigType.OutputSampleBits, "3");

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      ChainRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }

  @Test
  public void erase_inDraftState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.DRAFT, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.erase(access, ULong.valueOf(3));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertEquals(Chain.ERASE, result.getState());
  }

  @Test
  public void erase_inCompleteState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.COMPLETE, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.erase(access, ULong.valueOf(3));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertEquals(Chain.ERASE, result.getState());
  }

  @Test
  public void erase_inFailedState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.FAILED, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.erase(access, ULong.valueOf(3));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertEquals(Chain.ERASE, result.getState());
  }

  @Test
  public void erase_failsInFabricatingState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to erase not in allowed (fabricating,failed,complete)");

    testDAO.erase(access, ULong.valueOf(3));
  }

  @Test
  public void erase_failsInReadyState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to erase not in allowed (draft,ready,fabricating)");

    testDAO.erase(access, ULong.valueOf(3));
  }

}
