// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.InstrumentRecord;
import io.outright.xj.core.transport.CSV;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.core.util.testing.Testing;

import org.jooq.Record;
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
import java.util.List;

import static io.outright.xj.core.tables.Instrument.INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to readMany vs. create vs. update or delete instruments
public class InstrumentIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private InstrumentDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "sandwich" has instrument "jams" and instrument "buns"
    IntegrationTestEntity.insertLibrary(1, 1, "sandwich");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "jams", Instrument.PERCUSSIVE, 0.6);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "buns", Instrument.HARMONIC, 0.4);
    IntegrationTestEntity.insertInstrumentMeme(1,1,"smooth");

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setLibraryId(BigInteger.valueOf(1))
      .setDescription("bimmies")
      .setType(Instrument.PERCUSSIVE)
      .setUserId(BigInteger.valueOf(2));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(0.42, result.get("density"));
    assertEquals(ULong.valueOf(1), result.get("libraryId"));
    assertEquals("bimmies", result.get("description"));
    assertEquals(Instrument.PERCUSSIVE, result.get("type"));
    assertEquals(ULong.valueOf(2), result.get("userId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setDescription("bimmies")
      .setType(Instrument.PERCUSSIVE)
      .setUserId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutUserID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setDescription("bimmies")
      .setType(Instrument.PERCUSSIVE)
      .setLibraryId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Instrument result = new Instrument().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getLibraryId());
    assertEquals("buns", result.getDescription());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    InstrumentRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  // TODO: test readAllInAccount vs readAllInLibrary, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAllInLibrary(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("jams", result1.get("description"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("buns", result2.get("description"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAllInLibrary(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllBoundToChain() throws  Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    IntegrationTestEntity.insertChainInstrument(1, 1, 1);

    Result<? extends Record> result = testDAO.readAllBoundToChain(Access.internal(), ULong.valueOf(1), Instrument.PERCUSSIVE);

    assertEquals(1, result.size());
    assertEquals("jams", result.get(0).get("description"));
    List<String> actualMemes = CSV.split(String.valueOf(result.get(0).get("memes")));
    assertEquals(1, actualMemes.size());
    assertEquals("smooth", actualMemes.get(0));
  }

  @Test
  public void readAllBoundToChainLibrary() throws  Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    IntegrationTestEntity.insertChainLibrary(1, 1, 1);

    Result<? extends Record> result = testDAO.readAllBoundToChainLibrary(Access.internal(), ULong.valueOf(1), Instrument.PERCUSSIVE);

    assertEquals(1, result.size());
    assertEquals("jams", result.get(0).get("description"));
    List<String> actualMemes = CSV.split(String.valueOf(result.get(0).get("memes")));
    assertEquals(1, actualMemes.size());
    assertEquals("smooth", actualMemes.get(0));
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDescription("bimmies");

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setLibraryId(BigInteger.valueOf(3));

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDescription("bimmies")
      .setLibraryId(BigInteger.valueOf(387));

    try {
      testDAO.update(access, ULong.valueOf(2), inputData);

    } catch (Exception e) {
      InstrumentRecord result = IntegrationTestService.getDb()
        .selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("buns", result.getDescription());
      assertEquals(ULong.valueOf(1), result.getLibraryId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setLibraryId(BigInteger.valueOf(1))
      .setDescription("bimmies")
      .setType(Instrument.PERCUSSIVE)
      .setUserId(BigInteger.valueOf(2));

    testDAO.update(access, ULong.valueOf(2), inputData);

    InstrumentRecord result = IntegrationTestService.getDb()
      .selectFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("bimmies", result.getDescription());
    assertEquals(ULong.valueOf(1), result.getLibraryId());
  }

  // TODO: [core] test DAO cannot update Instrument to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertInstrument(86, 1, 2, "jub", Instrument.HARMONIC, 0.4);

    testDAO.delete(access, ULong.valueOf(86));

    InstrumentRecord result = IntegrationTestService.getDb()
      .selectFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(ULong.valueOf(86)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfInstrumentHasChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertInstrument(86, 1, 2, "hamsicle", Instrument.HARMONIC, 0.4);
    IntegrationTestEntity.insertInstrumentMeme(5, 86, "frozen");
    IntegrationTestEntity.insertInstrumentMeme(6, 86, "ham");

    try {
      testDAO.delete(access, ULong.valueOf(86));

    } catch (Exception e) {
      InstrumentRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(ULong.valueOf(86)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }

  }

}
