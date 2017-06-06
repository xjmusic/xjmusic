// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.tables.records.ChainInstrumentRecord;
import io.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Timestamp;

import static io.xj.core.tables.ChainInstrument.CHAIN_INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different libraries to readMany vs. create vs. update or delete chain libraries
public class ChainInstrumentIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private ChainInstrumentDAO testDAO;

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

    // Stub users
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");

    // Library "palm tree" has instrument "fonds" and instrument "nuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "fonds", Instrument.HARMONIC, 0.342);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "nuts", Instrument.MELODIC, 0.342);

    // Library "boat" has instrument "helm" and instrument "sail"
    IntegrationTestEntity.insertLibrary(2, 2, "boat");
    IntegrationTestEntity.insertInstrument(3, 2, 3, "helm", Instrument.PERCUSSIVE, 0.342);
    IntegrationTestEntity.insertInstrument(4, 2, 3, "sail", Instrument.VOCAL, 0.342);

    // Chain "school" has instrument "helm"
    IntegrationTestEntity.insertChainInstrument(1, 1, 3);

    // Chain "bucket" has instruments "fonds" and "nuts"
    IntegrationTestEntity.insertChainInstrument(2, 2, 1);
    IntegrationTestEntity.insertChainInstrument(3, 2, 2);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChainInstrumentDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1))
      .setInstrumentId(BigInteger.valueOf(2));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("chainId"));
    assertEquals(ULong.valueOf(2), result.get("instrumentId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1))
      .setInstrumentId(BigInteger.valueOf(3));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInChainAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(3))
      .setInstrumentId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfUserNotInInstrumentAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1))
      .setInstrumentId(BigInteger.valueOf(3));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setInstrumentId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutInstrumentId() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    ChainInstrument inputData = new ChainInstrument()
      .setChainId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    ChainInstrument result = new ChainInstrument().setFromRecord(testDAO.readOne(access, ULong.valueOf(1)));

    assertNotNull(result);
    Assert.assertEquals(ULong.valueOf(1), result.getId());
    assertEquals(ULong.valueOf(1), result.getChainId());
    assertEquals(ULong.valueOf(3), result.getInstrumentId());
  }

  @Test
  public void readOne_FailsWhenChainIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    ChainInstrumentRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals(ULong.valueOf(1), result1.get("instrumentId"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals(ULong.valueOf(2), result2.get("instrumentId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ChainInstrumentRecord result = IntegrationTestService.getDb()
      .selectFrom(CHAIN_INSTRUMENT)
      .where(CHAIN_INSTRUMENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "instrument",
      "accounts", "5"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }
}
