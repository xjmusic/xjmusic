// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different libraries to readMany vs. create vs. update or delete chain configs
public class ChainConfigIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChainConfigDAO testDAO;
  @Rule
  public ExpectedException failure = ExpectedException.none();

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
    IntegrationTestEntity.insertChainConfig(1, ChainConfigType.OutputSampleBits, "1");

    // Chain "bucket" has sequences and instruments
    IntegrationTestEntity.insertChainConfig(2, ChainConfigType.OutputFrameRate, "1,4,35");
    IntegrationTestEntity.insertChainConfig(2, ChainConfigType.OutputChannels, "2,83,4");

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainConfigDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L))
      .setType("OutputFrameRate")
      .setValue("3,4,74");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(3L))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInLibraryAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L))
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setType("OutputSampleBits")
      .setValue("3");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutLibraryId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainConfig inputData = new ChainConfig()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    ChainConfig result = testDAO.readOne(access, BigInteger.valueOf(1000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(ChainConfigType.OutputSampleBits, result.getType());
    assertEquals("1", result.getValue());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1000L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<ChainConfig> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<ChainConfig> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1000L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Engineer",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));
  }
}
