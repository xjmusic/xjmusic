// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.library.Library;
import io.outright.xj.core.tables.records.LibraryRecord;
import io.outright.xj.core.transport.JSON;

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

import static io.outright.xj.core.tables.Library.LIBRARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LibraryIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private LibraryDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "palm tree" has library "leaves" and library "coconuts"
    IntegrationTestEntity.insertAccount(1, "palm tree");
    IntegrationTestEntity.insertLibrary(1, 1, "leaves");
    IntegrationTestEntity.insertLibrary(2, 1, "coconuts");

    // Account "boat" has library "helm" and library "sail"
    IntegrationTestEntity.insertAccount(2, "boat");
    IntegrationTestEntity.insertLibrary(3, 2, "helm");
    IntegrationTestEntity.insertLibrary(4, 2, "sail");

    // Instantiate the test subject
    testDAO = injector.getInstance(LibraryDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setName("manuts")
      .setAccountId(BigInteger.valueOf(1));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals("manuts", result.get("name"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setName("manuts");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Library result = new Library().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getAccountId());
    assertEquals("coconuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    LibraryRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("leaves", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("coconuts", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutAccountID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setName("cannons");

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setAccountId(BigInteger.valueOf(3));

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(1));

    testDAO.update(access, ULong.valueOf(3), inputData);

    LibraryRecord result = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(ULong.valueOf(1), result.getAccountId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(3978));

    try {
      testDAO.update(access, ULong.valueOf(3), inputData);

    } catch (Exception e) {
      LibraryRecord result = IntegrationTestService.getDb()
        .selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(ULong.valueOf(3)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(ULong.valueOf(2), result.getAccountId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setName("cannons")
      .setAccountId(BigInteger.valueOf(2));

    testDAO.update(access, ULong.valueOf(3), inputData);

    LibraryRecord result = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(ULong.valueOf(2), result.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Library inputData = new Library()
      .setName("trunk")
      .setAccountId(BigInteger.valueOf(1));

    testDAO.update(access, ULong.valueOf(3), inputData);

    LibraryRecord result = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("trunk", result.getName());
    assertEquals(ULong.valueOf(1), result.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    LibraryRecord result = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfLibraryHasChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertUser(101, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertIdea(301, 101, 2, Idea.MAIN, "brilliant", 0.342, "C#", 0.286);

    try {
      testDAO.delete(access, ULong.valueOf(2));
    } catch (Exception e) {
      LibraryRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }
}
