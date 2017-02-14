// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.instrument.InstrumentWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.InstrumentRecord;

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

import static io.outright.xj.core.tables.Instrument.INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete instruments
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

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2",
      "roles", "user",
      "accounts", "1"
    ));
    InstrumentWrapper inputDataWrapper = new InstrumentWrapper()
      .setInstrument(new Instrument()
        .setDensity(0.42)
        .setLibraryId(BigInteger.valueOf(1))
        .setDescription("bimmies")
        .setType(Instrument.PERCUSSIVE)
        .setUserId(BigInteger.valueOf(2))
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(0.42, actualResult.get("density"));
    assertEquals(ULong.valueOf(1), actualResult.get("libraryId"));
    assertEquals("bimmies", actualResult.get("description"));
    assertEquals(Instrument.PERCUSSIVE, actualResult.get("type"));
    assertEquals(ULong.valueOf(2), actualResult.get("userId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    InstrumentWrapper inputDataWrapper = new InstrumentWrapper()
      .setInstrument(new Instrument()
        .setDensity(0.42)
        .setDescription("bimmies")
        .setType(Instrument.PERCUSSIVE)
        .setUserId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutUserID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    InstrumentWrapper inputDataWrapper = new InstrumentWrapper()
      .setInstrument(new Instrument()
        .setDensity(0.42)
        .setDescription("bimmies")
        .setType(Instrument.PERCUSSIVE)
        .setLibraryId(BigInteger.valueOf(2))
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
    assertEquals(ULong.valueOf(1), actualResult.get("libraryId"));
    assertEquals("buns", actualResult.get("description"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
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
      "roles", "admin",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("jams", actualResult1.get("description"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("buns", actualResult2.get("description"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument();
    inputData.setDescription("bimmies");
    InstrumentWrapper inputDataWrapper = new InstrumentWrapper();
    inputDataWrapper.setInstrument(inputData);

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument();
    inputData.setLibraryId(BigInteger.valueOf(3));
    InstrumentWrapper inputDataWrapper = new InstrumentWrapper();
    inputDataWrapper.setInstrument(inputData);

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument();
    inputData.setDescription("bimmies");
    inputData.setLibraryId(BigInteger.valueOf(387));
    InstrumentWrapper inputDataWrapper = new InstrumentWrapper();
    inputDataWrapper.setInstrument(inputData);

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      InstrumentRecord updatedRecord = IntegrationTestService.getDb()
        .selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(updatedRecord);
      assertEquals("buns", updatedRecord.getDescription());
      assertEquals(ULong.valueOf(1), updatedRecord.getLibraryId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2",
      "roles", "user",
      "accounts", "1"
    ));
    InstrumentWrapper inputDataWrapper = new InstrumentWrapper()
      .setInstrument(new Instrument()
        .setDensity(0.42)
        .setLibraryId(BigInteger.valueOf(1))
        .setDescription("bimmies")
        .setType(Instrument.PERCUSSIVE)
        .setUserId(BigInteger.valueOf(2))
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    InstrumentRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("bimmies", updatedRecord.getDescription());
    assertEquals(ULong.valueOf(1), updatedRecord.getLibraryId());
  }

  // TODO: [core] test DAO cannot update Instrument to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    InstrumentRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(INSTRUMENT)
      .where(INSTRUMENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfInstrumentHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "hams");

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      InstrumentRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }

  }

}
