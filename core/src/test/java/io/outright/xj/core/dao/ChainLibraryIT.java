// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.chain_library.ChainLibrary;
import io.outright.xj.core.model.chain_library.ChainLibraryWrapper;
import io.outright.xj.core.tables.records.ChainLibraryRecord;

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

import static io.outright.xj.core.tables.ChainLibrary.CHAIN_LIBRARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different libraries to read vs. create vs. update or delete chain libraries
public class ChainLibraryIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private ChainLibraryDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.52714"), Timestamp.valueOf("2014-09-11 12:17:01.0475"));
    IntegrationTestEntity.insertChain(2, 1, "bucket", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2015-05-10 12:17:02.52714"), Timestamp.valueOf("2015-06-09 12:17:01.0475"));

    // Account "blocks" has chain "red"
    IntegrationTestEntity.insertAccount(2, "blocks");
    IntegrationTestEntity.insertChain(3, 2, "red", Chain.PRODUCTION, Chain.COMPLETE, Timestamp.valueOf("2014-08-12 12:17:02.52714"), Timestamp.valueOf("2014-09-11 12:17:01.0475"));

    // Libraries in account "fish"
    IntegrationTestEntity.insertLibrary(1, 1, "buns");
    IntegrationTestEntity.insertLibrary(2, 1, "jams");

    // Libraries in account "blocks"
    IntegrationTestEntity.insertLibrary(3, 2, "pajamas");

    // Chain "school" has library "buns"
    IntegrationTestEntity.insertChainLibrary(1, 1, 1);

    // Chain "bucket" has libraries "buns" and "jams"
    IntegrationTestEntity.insertChainLibrary(2, 2, 1);
    IntegrationTestEntity.insertChainLibrary(3, 2, 2);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainLibraryDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainLibraryWrapper inputDataWrapper = new ChainLibraryWrapper()
      .setChainLibrary(new ChainLibrary()
        .setChainId(BigInteger.valueOf(1))
        .setLibraryId(BigInteger.valueOf(2))
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.get("chainId"));
    assertEquals(BigInteger.valueOf(2), result.get("libraryId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainLibraryWrapper inputDataWrapper = new ChainLibraryWrapper()
      .setChainLibrary(new ChainLibrary()
        .setChainId(BigInteger.valueOf(1))
        .setLibraryId(BigInteger.valueOf(1))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainLibraryWrapper inputDataWrapper = new ChainLibraryWrapper()
      .setChainLibrary(new ChainLibrary()
        .setChainId(BigInteger.valueOf(3))
        .setLibraryId(BigInteger.valueOf(1))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInLibraryAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainLibraryWrapper inputDataWrapper = new ChainLibraryWrapper()
      .setChainLibrary(new ChainLibrary()
        .setChainId(BigInteger.valueOf(1))
        .setLibraryId(BigInteger.valueOf(3))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChainID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainLibraryWrapper inputDataWrapper = new ChainLibraryWrapper()
      .setChainLibrary(new ChainLibrary()
        .setLibraryId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryId() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainLibraryWrapper inputDataWrapper = new ChainLibraryWrapper()
      .setChainLibrary(new ChainLibrary()
        .setChainId(BigInteger.valueOf(1))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get("chainId"));
    assertEquals(ULong.valueOf(1), result.get("libraryId"));
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(2));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals(1, actualResult1.get("libraryId"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals(2, actualResult2.get("libraryId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts","1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ChainLibraryRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(CHAIN_LIBRARY)
      .where(CHAIN_LIBRARY.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "library",
      "accounts","5"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }
}
