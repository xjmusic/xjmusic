// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
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

// future test: permissions of different libraries to readMany vs. create vs. update or delete chain configs
public class ChainConfigIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChainConfigDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

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
    IntegrationTestEntity.insertChainConfig(1, 1, ChainConfigType.OutputSampleBits, "1");

    // Chain "bucket" has patterns and instruments
    IntegrationTestEntity.insertChainConfig(2, 2, ChainConfigType.OutputFrameRate, "1,4,35");
    IntegrationTestEntity.insertChainConfig(3, 2, ChainConfigType.OutputChannels, "2,83,4");

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainConfigDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1))
      .setType("OutputFrameRate")
      .setValue("3,4,74");

    JSONObject result = JSON.objectFrom(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(1, result.get("chainId"));
    assertEquals("OutputFrameRate", result.get("type"));
    assertEquals("3,4,74", result.get("value"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(3))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInLibraryAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryId() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    ChainConfig result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getChainId());
    assertEquals(ChainConfigType.OutputSampleBits, result.getType());
    assertEquals("1", result.getValue());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    ChainConfig result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(2))));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("OutputFrameRate", result1.get("type"));
    assertEquals("1,4,35", result1.get("value"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("OutputChannels", result2.get("type"));
    assertEquals("2,83,4", result2.get("value"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    ChainConfig result = testDAO.readOne(Access.internal(),BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));
  }
}
