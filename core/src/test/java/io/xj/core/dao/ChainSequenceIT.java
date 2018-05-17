// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_sequence.ChainSequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
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
public class ChainSequenceIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChainSequenceDAO testDAO;

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

    // Stub users
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");

    // Library "palm tree" has sequence "fonds" and sequence "nuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Main, SequenceState.Published, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertSequence(2, 2, 1, SequenceType.Rhythm, SequenceState.Published, "nuts", 0.342, "C#", 0.286);

    // Library "boat" has sequence "helm" and sequence "sail"
    IntegrationTestEntity.insertLibrary(2, 2, "boat");
    IntegrationTestEntity.insertSequence(3, 3, 2, SequenceType.Macro, SequenceState.Published, "helm", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertSequence(4, 2, 2, SequenceType.Detail, SequenceState.Published, "sail", 0.342, "C#", 0.286);

    // Chain "school" has sequence "helm"
    IntegrationTestEntity.insertChainSequence(1, 1, 3);

    // Chain "bucket" has sequences "fonds" and "nuts"
    IntegrationTestEntity.insertChainSequence(2, 2, 1);
    IntegrationTestEntity.insertChainSequence(3, 2, 2);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainSequenceDAO.class);
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
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(2L));

    JSONObject result = JSON.objectFrom(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(1, result.get("chainId"));
    assertEquals(2, result.get("sequenceId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(3L))
      .setSequenceId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInSequenceAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setSequenceId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutSequenceId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainSequence inputData = new ChainSequence()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    ChainSequence result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(3L), result.getSequenceId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    ChainSequence result = testDAO.readOne(access, BigInteger.valueOf(1L));

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
    assertEquals(1, result1.get("sequenceId"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals(2, result2.get("sequenceId"));
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
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    ChainSequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
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
