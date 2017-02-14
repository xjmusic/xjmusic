// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.idea.IdeaWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.IdeaRecord;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.jooq.types.ULong;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static io.outright.xj.core.tables.Idea.IDEA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete ideas
public class IdeaIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private IdeaDAO testDAO;

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

    // Library "palm tree" has idea "fonds" and idea "nuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(2, 2, 1, Idea.RHYTHM, "nuts", 0.342, "C#", 0.286);

    // Library "boat" has idea "helm" and idea "sail"
    IntegrationTestEntity.insertLibrary(2, 1, "boat");
    IntegrationTestEntity.insertIdea(3, 3, 2, Idea.MACRO, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(4, 2, 2, Idea.SUPPORT, "jams", 0.342, "C#", 0.286);

    // Instantiate the test subject
    testDAO = injector.getInstance(IdeaDAO.class);
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
    IdeaWrapper inputDataWrapper = new IdeaWrapper()
      .setIdea(new Idea()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setLibraryId(BigInteger.valueOf(2))
        .setName("cannons")
        .setTempo(129.4)
        .setType(Idea.MAIN)
        .setUserId(BigInteger.valueOf(2))
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(0.42, actualResult.get("density"));
    assertEquals("G minor 7", actualResult.get("key"));
    assertEquals(ULong.valueOf(2), actualResult.get("libraryId"));
    assertEquals("cannons", actualResult.get("name"));
    assertEquals(129.4, actualResult.get("tempo"));
    assertEquals(Idea.MAIN, actualResult.get("type"));
    assertEquals(ULong.valueOf(2), actualResult.get("userId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    IdeaWrapper inputDataWrapper = new IdeaWrapper()
      .setIdea(new Idea()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setName("cannons")
        .setTempo(129.4)
        .setType(Idea.MAIN)
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
    IdeaWrapper inputDataWrapper = new IdeaWrapper()
      .setIdea(new Idea()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setName("cannons")
        .setTempo(129.4)
        .setType(Idea.MAIN)
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
    assertEquals("nuts", actualResult.get("name"));
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
    assertEquals("fonds", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("nuts", actualResult2.get("name"));
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
    Idea inputData = new Idea();
    inputData.setName("cannons");
    IdeaWrapper inputDataWrapper = new IdeaWrapper();
    inputDataWrapper.setIdea(inputData);

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Idea inputData = new Idea();
    inputData.setLibraryId(BigInteger.valueOf(3));
    IdeaWrapper inputDataWrapper = new IdeaWrapper();
    inputDataWrapper.setIdea(inputData);

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Idea inputData = new Idea();
    inputData.setName("cannons");
    inputData.setLibraryId(BigInteger.valueOf(3));
    IdeaWrapper inputDataWrapper = new IdeaWrapper();
    inputDataWrapper.setIdea(inputData);

    try {
      testDAO.update(access, ULong.valueOf(3), inputDataWrapper);

    } catch (Exception e) {
      IdeaRecord updatedRecord = IntegrationTestService.getDb()
        .selectFrom(IDEA)
        .where(IDEA.ID.eq(ULong.valueOf(3)))
        .fetchOne();
      assertNotNull(updatedRecord);
      assertEquals("fonds", updatedRecord.getName());
      assertEquals(ULong.valueOf(2), updatedRecord.getLibraryId());
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
    IdeaWrapper inputDataWrapper = new IdeaWrapper()
      .setIdea(new Idea()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setLibraryId(BigInteger.valueOf(2))
        .setName("cannons")
        .setTempo(129.4)
        .setType(Idea.MAIN)
        .setUserId(BigInteger.valueOf(2))
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);

    IdeaRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("cannons", updatedRecord.getName());
    assertEquals(ULong.valueOf(2), updatedRecord.getLibraryId());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    IdeaRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfIdeaHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertPhase(1, 2, 0, 14, "testPhase", 0.524, "F#", 125.49);

    try {
      testDAO.delete(access, ULong.valueOf(2));

    } catch (Exception e) {
      IdeaRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(IDEA)
        .where(IDEA.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }

  }

}
