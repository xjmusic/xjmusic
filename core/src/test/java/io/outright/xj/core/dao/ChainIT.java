// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.chain.ChainWrapper;
import io.outright.xj.core.model.chain_config.ChainConfig;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.ChainRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.Tables.ARRANGEMENT;
import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.PICK;
import static io.outright.xj.core.Tables.POINT;
import static io.outright.xj.core.Tables.MORPH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChainIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private ChainDAO testDAO;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertChain(2, 1, "bucket", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

    // Account "boat" has no chains
    IntegrationTestEntity.insertAccount(2, "boat");

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setType(Chain.PRODUCTION)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setType(Chain.PREVIEW)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

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
  // [#126] Chains are always created in DRAFT state
  public void create_alwaysCreatedInDraftState() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setType(Chain.PRODUCTION)
        .setState(Chain.FABRICATING)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setType(Chain.PRODUCTION)
        .setState(Chain.DRAFT)
        .setStartAt("2009-08-12 12:17:02.527142")
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setType(Chain.PRODUCTION)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("")
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setType(Chain.PRODUCTION)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    testDAO.create(access, inputDataWrapper);
  }

  @Test()
  public void create_FailsWithInvalidState() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setType(Chain.PRODUCTION)
        .setState("bullshit state")
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("'bullshitstate' is not a valid state (draft,ready,fabricating,complete)");

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals("bucket", result.get("name"));
    assertEquals(Chain.FABRICATING, result.get("state"));
    assertEquals(Chain.PRODUCTION, result.get("type"));
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), result.get("startAt"));
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), result.get("stopAt"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("school", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("bucket", actualResult2.get("name"));
  }

  @Test
  public void readAllRecordsInStateFabricating() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);

    Result<ChainRecord> actualResults = testDAO.readAllRecordsInStateFabricating(access, Timestamp.valueOf("2015-05-20 12:00:00"));

    assertNotNull(actualResults);
    assertEquals(2, actualResults.size());
    ChainRecord actualResult1 = actualResults.get(0);
    assertEquals(ULong.valueOf(2), actualResult1.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), actualResult1.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), actualResult1.getStopAt());
    ChainRecord actualResult2 = actualResults.get(1);
    assertEquals(ULong.valueOf(4), actualResult2.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), actualResult2.getStartAt());
    assertNull(actualResult2.getStopAt());
  }

  @Test
  public void readAllIdBoundsInStateFabricating_ReturnsChainBeforeBoundary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllRecordsInStateFabricating(access, Timestamp.valueOf("2016-05-20 12:00:00"));

    assertNotNull(actualResults);
    assertEquals(2, actualResults.size());
  }

  @Test
  public void readAllIdBoundsInStateFabricating_DoesNotReturnChainAfterBoundary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-06-10 12:17:02.527142"), Timestamp.valueOf("2015-06-12 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllRecordsInStateFabricating(access, Timestamp.valueOf("2015-05-20 12:00:00"));

    assertNotNull(actualResults);

    assertEquals(1, actualResults.size());
    ChainRecord actualResult1 = actualResults.get(0);
    assertEquals(ULong.valueOf(2), actualResult1.getId());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), actualResult1.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), actualResult1.getStopAt());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setType(Chain.PRODUCTION)
        .setState(Chain.COMPLETE)
        .setStartAt("2009-08-12 12:17:02.687327")
        .setStopAt("2009-09-11 12:17:01.989941")
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setType(Chain.PREVIEW)
        .setState(Chain.COMPLETE)
        .setStartAt("2009-08-12 12:17:02.687327")
        .setStopAt("2009-09-11 12:17:01.989941")
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("bucket")
        .setType(Chain.PRODUCTION)
        .setState(Chain.FABRICATING)
        .setStartAt("2015-05-10 12:17:03.527142")
        .setStopAt("2015-06-09 12:17:01.047563")
      );
    IntegrationTestEntity.insertLink(6, 2, 5, Link.CRAFTED, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120);

    failure.expect(BusinessException.class);
    failure.expectMessage("cannot change chain startAt time after it has links");

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test
  public void update_canChangeName_whenChainsHasLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setState(Chain.FABRICATING)
        .setType(Chain.PRODUCTION)
        .setStartAt("2015-05-10 12:17:02.527142")
        .setStopAt("2015-06-09 12:17:01.047563")
      );
    IntegrationTestEntity.insertLink(6, 2, 5, Link.CRAFTED, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120);

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setType(Chain.PRODUCTION)
        .setState(Chain.COMPLETE)
        .setStartAt("2009-08-12 12:17:02.687327")
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setType(Chain.PRODUCTION)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("Account ID is required");

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test()
  public void update_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setState(Chain.DRAFT)
        .setType(Chain.PRODUCTION)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test()
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(75))
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setType(Chain.PRODUCTION)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("transition to draft not allowed");

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      ChainRecord result = IntegrationTestService.getDb()
        .selectFrom(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("bucket", result.getName());
      assertEquals(ULong.valueOf(1), result.getAccountId());
      throw e;
    }
  }

  @Test
  public void updateState() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("'bullshit state' is not a valid state (draft,ready,fabricating,complete)");

    testDAO.updateState(access, ULong.valueOf(2), "bullshit state");
  }

  @Test()
  public void updateState_FailsWithoutTopLevelAccess() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "54"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("if not top level access, must provide account id");

    testDAO.updateState(access, ULong.valueOf(2), Chain.COMPLETE);
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 12:04:10.000001"), "E minor", 64, 0.41, 120);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTING, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBING, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBED, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBED, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertLink(6, 1, 5, Link.PLANNED, Timestamp.valueOf("2017-02-14 12:03:08.000001"), null, "A major", 64, 0.52, 120);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(1));
    fromChain.setStartAt(Timestamp.valueOf("2015-02-14 12:03:40.000001"));
    fromChain.setStopAt(null);
    JSONObject result = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNull(result);
  }

  @Test
  public void buildNextLinkOrComplete_chainEndingInCraftedLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTED, Timestamp.valueOf("2014-08-12 14:03:08.000001"), Timestamp.valueOf("2014-08-12 14:03:38.000001"), "A major", 64, 0.52, 120);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
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
    AccessControl access = new AccessControl(ImmutableMap.of(
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
  public void delete_FailsIfChainHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertLibrary(1, 1, "nerds");
    IntegrationTestEntity.insertChainConfig(101, 1, ChainConfig.OUTPUT_SAMPLE_BITS, "3");

    failure.expect(BusinessException.class);
    failure.expectMessage("Found ChainConfig");

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
  public void destroy_allChildEntities() throws Exception {
    // User "bill"
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 2, "test sounds");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MACRO, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, Voice.PERCUSSIVE, "This is a percussive voice");
    IntegrationTestEntity.insertVoiceEvent(1, 8, 0, 1, "KICK", "C", 0.8, 1.0);

    // Library has Instrument with Audio
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", Instrument.PERCUSSIVE, 0.6);
    IntegrationTestEntity.insertAudio(1, 9, "Kick", "https://static.xj.outright.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(3, 1, "Test Print #1", Chain.PRODUCTION, Chain.COMPLETE, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(1, 3, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120);

    // Choice
    IntegrationTestEntity.insertChoice(1, 1, 1, Choice.MACRO, 2, -5);

    // Arrangement
    IntegrationTestEntity.insertArrangement(1, 1, 8, 9);

    // Morph is in arrangement
    IntegrationTestEntity.insertMorph(1, 1, 0.75, "C", 0.5);

    // Pick is in Morph
    IntegrationTestEntity.insertPick(1, 1, 1, 1, 0.125, 1.23, 0.94, 440);

    // Point is in Morph
    IntegrationTestEntity.insertPoint(1, 1, 1, 0.125, "C", 1.5);

    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.destroy(access, ULong.valueOf(3));

    // Assert destroyed Chain
    assertNull(IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3)))
      .fetchOne());

    // Assert destroyed Link
    assertNull(IntegrationTestService.getDb()
      .selectFrom(LINK)
      .where(LINK.ID.eq(ULong.valueOf(1)))
      .fetchOne());

    // Assert destroyed Arrangement
    assertNull(IntegrationTestService.getDb()
      .selectFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(ULong.valueOf(1)))
      .fetchOne());

    // Assert destroyed Choice
    assertNull(IntegrationTestService.getDb()
      .selectFrom(CHOICE)
      .where(CHOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne());

    // Assert destroyed Morph
    assertNull(IntegrationTestService.getDb()
      .selectFrom(MORPH)
      .where(MORPH.ID.eq(ULong.valueOf(1)))
      .fetchOne());

    // Assert destroyed Pick
    assertNull(IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.ID.eq(ULong.valueOf(1)))
      .fetchOne());

    // Assert destroyed Point
    assertNull(IntegrationTestService.getDb()
      .selectFrom(POINT)
      .where(POINT.ID.eq(ULong.valueOf(1)))
      .fetchOne());

  }

  @Test
  public void destroy_inDraftState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.DRAFT, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.destroy(access, ULong.valueOf(3));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNull(result);
  }

  @Test
  public void destroy_inCompleteState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.COMPLETE, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.destroy(access, ULong.valueOf(3));

    ChainRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNull(result);
  }

  @Test
  public void destroy_failsInFabricatingState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain must be in a draft or complete state");

    testDAO.destroy(access, ULong.valueOf(3));
  }

  @Test
  public void destroy_failsInReadyState() throws Exception {
    IntegrationTestEntity.insertChain(3, 1, "bucket", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain must be in a draft or complete state");

    testDAO.destroy(access, ULong.valueOf(3));
  }

}
