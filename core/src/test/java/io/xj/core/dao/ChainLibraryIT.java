// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableList;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different libraries to readMany vs. create vs. update or delete chain libraries
public class ChainLibraryIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChainLibraryDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.52714"), Timestamp.valueOf("2014-09-11 12:17:01.0475"), null);
    IntegrationTestEntity.insertChain(2, 1, "bucket", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2015-05-10 12:17:02.52714"), Timestamp.valueOf("2015-06-09 12:17:01.0475"), null);

    // Account "blocks" has chain "red"
    IntegrationTestEntity.insertAccount(2, "blocks");
    IntegrationTestEntity.insertChain(3, 2, "red", ChainType.Production, ChainState.Complete, Timestamp.valueOf("2014-08-12 12:17:02.52714"), Timestamp.valueOf("2014-09-11 12:17:01.0475"), null);

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
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L))
      .setLibraryId(BigInteger.valueOf(2L));

    JSONObject result = JSON.objectFrom(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(1, result.get("chainId"));
    assertEquals(2, result.get("libraryId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L))
      .setLibraryId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(3L))
      .setLibraryId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInLibraryAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L))
      .setLibraryId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setLibraryId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainLibrary inputData = new ChainLibrary()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    ChainLibrary result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    ChainLibrary result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(2L))));

    assertNotNull(result);
    assertEquals(2L, (long) result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals(1, result1.get("libraryId"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals(2, result2.get("libraryId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(0L, (long) result.length());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    ChainLibrary result = testDAO.readOne(Access.internal(),BigInteger.valueOf(1L));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }
}
