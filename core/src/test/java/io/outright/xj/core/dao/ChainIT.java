// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.chain.ChainWrapper;
import io.outright.xj.core.model.link.Link;
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
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Timestamp;

import static io.outright.xj.core.tables.Chain.CHAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ChainIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private ChainDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertChain(2, 1, "bucket", Chain.PRODUCTION, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

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
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("accountId"));
    assertEquals("manuts", actualResult.get("name"));
    assertEquals(Chain.DRAFT, actualResult.get("state"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), actualResult.get("startAt"));
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.047563"), actualResult.get("stopAt"));
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
        .setState(Chain.DRAFT)
        .setStartAt("2009-08-12 12:17:02.527142")
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("accountId"));
    assertEquals("manuts", actualResult.get("name"));
    assertEquals(Chain.DRAFT, actualResult.get("state"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), actualResult.get("startAt"));
    assertFalse(actualResult.has("stopAt"));
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
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("")
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("accountId"));
    assertEquals("manuts", actualResult.get("name"));
    assertEquals(Chain.DRAFT, actualResult.get("state"));
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.527142"), actualResult.get("startAt"));
    assertFalse(actualResult.has("stopAt"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithInvalidState() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("manuts")
        .setState("bullshit state")
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(2), actualResult.get("id"));
    assertEquals(ULong.valueOf(1), actualResult.get("accountId"));
    assertEquals("bucket", actualResult.get("name"));
    assertEquals(Chain.PRODUCTION, actualResult.get("state"));
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), actualResult.get("startAt"));
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), actualResult.get("stopAt"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(actualResult);
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
  public void readAllRecordsInProduction() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Timestamp.valueOf("2015-05-10 12:17:02.527142"), null);

    Result<ChainRecord> actualResults = testDAO.readAllRecordsInProduction(access, Timestamp.valueOf("2015-05-20 12:00:00"));

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
  public void readAllIdBoundsInProduction_ReturnsChainBeforeBoundary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Timestamp.valueOf("2015-05-10 12:17:02.527142"), Timestamp.valueOf("2015-06-09 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllRecordsInProduction(access, Timestamp.valueOf("2016-05-20 12:00:00"));

    assertNotNull(actualResults);
    assertEquals(2, actualResults.size());
  }

  @Test
  public void readAllIdBoundsInProduction_DoesNotReturnChainAfterBoundary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(4, 2, "smash", Chain.PRODUCTION, Timestamp.valueOf("2015-06-10 12:17:02.527142"), Timestamp.valueOf("2015-06-12 12:17:01.047563"));

    Result<ChainRecord> actualResults = testDAO.readAllRecordsInProduction(access, Timestamp.valueOf("2015-05-20 12:00:00"));

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
        .setState(Chain.COMPLETE)
        .setStartAt("2009-08-12 12:17:02.687327")
        .setStopAt("2009-09-11 12:17:01.989941")
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    ChainRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("manuts", updatedRecord.getName());
    assertEquals(ULong.valueOf(1), updatedRecord.getAccountId());
    assertEquals(Chain.COMPLETE, updatedRecord.getState());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), updatedRecord.getStartAt());
    assertEquals(Timestamp.valueOf("2009-09-11 12:17:01.989941"), updatedRecord.getStopAt());
  }

  @Test(expected = BusinessException.class)
  public void update_failsToChangeStartAt_whenChainsHasLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setName("bucket")
        .setState(Chain.PRODUCTION)
        .setStartAt("2015-05-10 12:17:03.527142")
        .setStopAt("2015-06-09 12:17:01.047563")
      );
    IntegrationTestEntity.insertLink(6, 2, 5, Link.CRAFTED, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120);

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      assertTrue(e.getMessage().contains("cannot change chain startAt time after it has links"));
      throw e;
    }
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
        .setState(Chain.PRODUCTION)
        .setStartAt("2015-05-10 12:17:02.527142")
        .setStopAt("2015-06-09 12:17:01.047563")
      );
    IntegrationTestEntity.insertLink(6, 2, 5, Link.CRAFTED, Timestamp.valueOf("2015-05-10 12:18:02.527142"), Timestamp.valueOf("2015-05-10 12:18:32.527142"), "A major", 64, 0.52, 120);

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    ChainRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("manuts", updatedRecord.getName());
    assertEquals(ULong.valueOf(1), updatedRecord.getAccountId());
    assertEquals(Chain.PRODUCTION, updatedRecord.getState());
    assertEquals(Timestamp.valueOf("2015-05-10 12:17:02.527142"), updatedRecord.getStartAt());
    assertEquals(Timestamp.valueOf("2015-06-09 12:17:01.047563"), updatedRecord.getStopAt());
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
        .setState(Chain.COMPLETE)
        .setStartAt("2009-08-12 12:17:02.687327")
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    ChainRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("manuts", updatedRecord.getName());
    assertEquals(ULong.valueOf(1), updatedRecord.getAccountId());
    assertEquals(Chain.COMPLETE, updatedRecord.getState());
    assertEquals(Timestamp.valueOf("2009-08-12 12:17:02.687327"), updatedRecord.getStartAt());
    assertEquals(null, updatedRecord.getStopAt());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutAccountID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(1))
        .setState(Chain.DRAFT)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChainWrapper inputDataWrapper = new ChainWrapper()
      .setChain(new Chain()
        .setAccountId(BigInteger.valueOf(75))
        .setName("manuts")
        .setState(Chain.DRAFT)
        .setStartAt("2009-08-12 12:17:02.527142")
        .setStopAt("2009-09-11 12:17:01.047563")
      );

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      ChainRecord updatedRecord = IntegrationTestService.getDb()
        .selectFrom(CHAIN)
        .where(CHAIN.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(updatedRecord);
      assertEquals("bucket", updatedRecord.getName());
      assertEquals(ULong.valueOf(1), updatedRecord.getAccountId());
      throw e;
    }
  }

  @Test
  public void updateState() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));

    testDAO.updateState(access, ULong.valueOf(2), Chain.COMPLETE);

    ChainRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals(Chain.COMPLETE, updatedRecord.getState());
  }

  @Test(expected = BusinessException.class)
  public void updateState_FailsOnInvalidState() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));

    testDAO.updateState(access, ULong.valueOf(2), "bullshit state");
  }

  @Test(expected = BusinessException.class)
  public void updateState_FailsWithoutTopLevelAccess() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "54"
    ));

    testDAO.updateState(access, ULong.valueOf(2), Chain.COMPLETE);
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTING, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 12:04:10.000001"), "E minor", 64, 0.41, 120);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject actualResult = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 11:53:40.000001"));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(12), actualResult.get("chainId"));
    assertEquals(ULong.valueOf(6), actualResult.get("offset"));
    assertEquals(Timestamp.valueOf("2014-02-14 12:04:10.000001"), actualResult.get("beginAt"));
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butNotSoLongEnoughToBeComplete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTING, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject actualResult = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 13:53:50.000001"));

    assertNull(actualResult);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12)))
      .fetchOne();
    assertEquals(Chain.PRODUCTION, finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_butLastLinkNotDubbedSoChainNotComplete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBING, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject actualResult = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNull(actualResult);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12)))
      .fetchOne();
    assertEquals(Chain.PRODUCTION, finalChainRecord.getState());
  }

  @Test
  public void buildNextLinkOrComplete_chainWithLinksReadyForNextLink_butChainIsAlreadyFull_andGetsUpdatedToComplete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Timestamp.valueOf("2014-02-14 12:03:40.000001"), Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.DUBBED, Timestamp.valueOf("2014-02-14 14:03:15.000001"), Timestamp.valueOf("2014-02-14 14:03:45.000001"), "E minor", 64, 0.41, 120);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-02-14 12:03:40.000001"));
    fromChain.setStopAt(Timestamp.valueOf("2014-02-14 14:03:40.000001"));
    JSONObject actualResult = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-02-14 14:03:50.000001"), Timestamp.valueOf("2014-02-14 14:15:50.000001"));

    assertNull(actualResult);
    ChainRecord finalChainRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(12)))
      .fetchOne();
    assertEquals(Chain.COMPLETE, finalChainRecord.getState());
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
    JSONObject actualResult = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNull(actualResult);
  }

  @Test
  public void buildNextLinkOrComplete_chainEndingInCraftedLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.PRODUCTION, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(6, 12, 5, Link.CRAFTED, Timestamp.valueOf("2014-08-12 14:03:08.000001"), Timestamp.valueOf("2014-08-12 14:03:38.000001"), "A major", 64, 0.52, 120);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAt(Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    JSONObject actualResult = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(12), actualResult.get("chainId"));
    assertEquals(ULong.valueOf(6), actualResult.get("offset"));
    assertEquals(Timestamp.valueOf("2014-08-12 14:03:38.000001"), actualResult.get("beginAt"));
  }

  @Test
  public void buildNextLinkOrComplete_newEmptyChain() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(12, 1, "Test Print #2", Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);

    ChainRecord fromChain = new ChainRecord();
    fromChain.setId(ULong.valueOf(12));
    fromChain.setStartAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    fromChain.setStopAt(null);
    JSONObject actualResult = testDAO.buildNextLinkOrComplete(access, fromChain, Timestamp.valueOf("2014-08-12 14:03:38.000001"), Timestamp.valueOf("2014-08-12 13:53:38.000001"));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(12), actualResult.get("chainId"));
    assertEquals(0, actualResult.get("offset"));
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), actualResult.get("beginAt"));
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ChainRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .where(CHAIN.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfChainHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertLibrary(1, 1, "nerds");
    IntegrationTestEntity.insertChainLibrary(101, 1, 1);

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
}
