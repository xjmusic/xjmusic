// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.library.Library;
import io.outright.xj.core.model.library.LibraryWrapper;
import io.outright.xj.core.tables.records.LibraryRecord;

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
    Library inputData = new Library();
    inputData.setName("manuts");
    inputData.setAccountId(BigInteger.valueOf(1));
    LibraryWrapper inputDataWrapper = new LibraryWrapper();
    inputDataWrapper.setLibrary(inputData);

    JSONObject actualResult = testDAO.create(inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("accountId"));
    assertEquals("manuts", actualResult.get("name"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    Library inputData = new Library();
    inputData.setName("manuts");
    LibraryWrapper inputDataWrapper = new LibraryWrapper();
    inputDataWrapper.setLibrary(inputData);

    testDAO.create(inputDataWrapper);
  }

  @Test
  public void readOneAble() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONObject actualResult = testDAO.readOneAble(access, ULong.valueOf(2));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(2), actualResult.get("id"));
    assertEquals(ULong.valueOf(1), actualResult.get("accountId"));
    assertEquals("coconuts", actualResult.get("name"));
  }

  @Test
  public void readOneAble_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    JSONObject actualResult = testDAO.readOneAble(access, ULong.valueOf(1));

    assertNull(actualResult);
  }

  @Test
  public void readAllAble() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllAble(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("leaves", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("coconuts", actualResult2.get("name"));
  }

  @Test
  public void readAllAble_SeesNothingOutsideOfAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllAble(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutAccountID() throws Exception {
    Library inputData = new Library();
    inputData.setName("cannons");
    LibraryWrapper inputDataWrapper = new LibraryWrapper();
    inputDataWrapper.setLibrary(inputData);

    testDAO.update(ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Library inputData = new Library();
    inputData.setAccountId(BigInteger.valueOf(3));
    LibraryWrapper inputDataWrapper = new LibraryWrapper();
    inputDataWrapper.setLibrary(inputData);

    testDAO.update(ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAccount() throws Exception {
    Library inputData = new Library();
    inputData.setName("cannons");
    inputData.setAccountId(BigInteger.valueOf(3));
    LibraryWrapper inputDataWrapper = new LibraryWrapper();
    inputDataWrapper.setLibrary(inputData);

    testDAO.update(ULong.valueOf(3), inputDataWrapper);

    LibraryRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("cannons", updatedRecord.getName());
    assertEquals(ULong.valueOf(2), updatedRecord.getAccountId());
  }

  @Test
  public void update_Name() throws Exception {
    Library inputData = new Library();
    inputData.setName("cannons");
    inputData.setAccountId(BigInteger.valueOf(2));
    LibraryWrapper inputDataWrapper = new LibraryWrapper();
    inputDataWrapper.setLibrary(inputData);

    testDAO.update(ULong.valueOf(3), inputDataWrapper);

    LibraryRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("cannons", updatedRecord.getName());
    assertEquals(ULong.valueOf(2), updatedRecord.getAccountId());
  }

  @Test
  public void update_NameAndAccount() throws Exception {
    Library inputData = new Library();
    inputData.setName("trunk");
    inputData.setAccountId(BigInteger.valueOf(1));
    LibraryWrapper inputDataWrapper = new LibraryWrapper();
    inputDataWrapper.setLibrary(inputData);

    testDAO.update(ULong.valueOf(3), inputDataWrapper);

    LibraryRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("trunk", updatedRecord.getName());
    assertEquals(ULong.valueOf(1), updatedRecord.getAccountId());
  }

  @Test
  public void delete() throws Exception {
    testDAO.delete(ULong.valueOf(1));

    LibraryRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfLibraryHasChildRecords() throws Exception {
    IntegrationTestEntity.insertUser(101, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertIdea(301, 101, 2, Idea.MAIN, "brilliant", 0.342, "C#", 0.286);

    testDAO.delete(ULong.valueOf(2));

    LibraryRecord stillExistingRecord = IntegrationTestService.getDb()
      .selectFrom(LIBRARY)
      .where(LIBRARY.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(stillExistingRecord);
  }

}
