// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.external.AuthType;
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
    IntegrationTestEntity.insertUserRole(2, Role.USER);
    IntegrationTestEntity.insertUserRole(2, Role.ADMIN);
    IntegrationTestEntity.insertAccountUser(1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, AuthType.GOOGLE, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, Role.USER);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(4, Role.USER);

    // Library "palm tree" has idea "leaves" and idea "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "leaves", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(2, 2, 1, Idea.MAIN, "coconuts", 0.342, "C#", 0.286);

    // Library "boat" has idea "helm" and idea "sail"
    IntegrationTestEntity.insertLibrary(2, 1, "boat");
    IntegrationTestEntity.insertIdea(3, 3, 2, Idea.MAIN, "leaves", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(4, 3, 2, Idea.MAIN, "coconuts", 0.342, "C#", 0.286);

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

    JSONObject actualResult = testDAO.create(access,inputDataWrapper);

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

    testDAO.create(access,inputDataWrapper);
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
    assertEquals(ULong.valueOf(1), actualResult.get("libraryId"));
    assertEquals("coconuts", actualResult.get("name"));
  }

  @Test
  public void readOneAble_FailsWhenUserIsNotInLibrary() throws Exception {
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
  public void readAllAble_SeesNothingOutsideOfLibrary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllAble(access, ULong.valueOf(1));

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

    testDAO.update(access,ULong.valueOf(3), inputDataWrapper);

    IdeaRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("cannons", updatedRecord.getName());
    assertEquals(ULong.valueOf(2), updatedRecord.getLibraryId());
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

    testDAO.update(access,ULong.valueOf(3), inputDataWrapper);

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
    testDAO.delete(ULong.valueOf(1));

    IdeaRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfIdeaHasChildRecords() throws Exception {
    IntegrationTestEntity.insertPhase(1, 2, 0, 14, "testPhase", 0.524, "F#", 125.49);

    testDAO.delete(ULong.valueOf(2));

    IdeaRecord stillExistingRecord = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(stillExistingRecord);
  }

}
