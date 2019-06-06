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
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.model.instrument.InstrumentType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different libraries to readMany vs. create vs. update or delete chain libraries
public class ChainInstrumentIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ChainInstrumentDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "fish" has chain "school" and chain "bucket"
    IntegrationTestEntity.insertAccount(1, "fish");
    IntegrationTestEntity.insertChain(1, 1, "school", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.52714Z"), Instant.parse("2014-09-11T12:17:01.0475Z"), null);
    IntegrationTestEntity.insertChain(2, 1, "bucket", ChainType.Production, ChainState.Fabricate, Instant.parse("2015-05-10T12:17:02.52714Z"), Instant.parse("2015-06-09T12:17:01.0475Z"), null);

    // Account "blocks" has chain "red"
    IntegrationTestEntity.insertAccount(2, "blocks");
    IntegrationTestEntity.insertChain(3, 2, "red", ChainType.Production, ChainState.Complete, Instant.parse("2014-08-12T12:17:02.52714Z"), Instant.parse("2014-09-11T12:17:01.0475Z"), null);

    // Stub users
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");

    // Library "palm tree" has instrument "fonds" and instrument "nuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "fonds", InstrumentType.Harmonic, 0.342);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "nuts", InstrumentType.Melodic, 0.342);

    // Library "boat" has instrument "helm" and instrument "sail"
    IntegrationTestEntity.insertLibrary(2, 2, "boat");
    IntegrationTestEntity.insertInstrument(3, 2, 3, "helm", InstrumentType.Percussive, 0.342);
    IntegrationTestEntity.insertInstrument(4, 2, 3, "sail", InstrumentType.Vocal, 0.342);

    // Chain "school" has instrument "helm"
    IntegrationTestEntity.insertChainInstrument(1, 3);

    // Chain "bucket" has instruments "fonds" and "nuts"
    IntegrationTestEntity.insertChainInstrument(2, 1);
    IntegrationTestEntity.insertChainInstrument(2, 2);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainInstrumentDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L))
      .setInstrumentId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L))
      .setInstrumentId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(3L))
      .setInstrumentId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailIfUserNotInInstrumentAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L))
      .setInstrumentId(BigInteger.valueOf(3L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setInstrumentId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutInstrumentId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    ChainInstrument result = testDAO.readOne(access, BigInteger.valueOf(1003000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(3L), result.getInstrumentId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1003000L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<ChainInstrument> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<ChainInstrument> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1003000L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1003000L));
  }

  @Test(expected = CoreException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "5"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1003000L));
  }
}
