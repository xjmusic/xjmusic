// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.chain.ChainWrapper;
import io.outright.xj.core.tables.records.ChainRecord;

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
