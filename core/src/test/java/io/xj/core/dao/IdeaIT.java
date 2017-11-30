// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.idea.Idea;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.IdeaRecord;
import io.xj.core.transport.CSV;
import io.xj.core.transport.JSON;
import io.xj.core.util.testing.Testing;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

import static io.xj.core.tables.Idea.IDEA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete ideas
public class IdeaIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
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
    IntegrationTestEntity.insertIdea(1, 2, 1, IdeaType.Main, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdeaMeme(12, 1, "leafy");
    IntegrationTestEntity.insertIdeaMeme(14, 1, "smooth");
    IntegrationTestEntity.insertIdea(2, 2, 1, IdeaType.Rhythm, "nuts", 0.342, "C#", 0.286);

    // Library "boat" has idea "helm" and idea "sail"
    IntegrationTestEntity.insertLibrary(2, 1, "boat");
    IntegrationTestEntity.insertIdea(3, 3, 2, IdeaType.Macro, "helm", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(4, 2, 2, IdeaType.Support, "sail", 0.342, "C#", 0.286);

    // Instantiate the test subject
    testDAO = injector.getInstance(IdeaDAO.class);
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
    Idea inputData = new Idea()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(0.42, result.get("density"));
    assertEquals("G minor 7", result.get("key"));
    assertEquals(ULong.valueOf(2), result.get("libraryId"));
    assertEquals("cannons", result.get("name"));
    assertEquals(129.4, result.get("tempo"));
    assertEquals(IdeaType.Main, result.get("type"));
    assertEquals(ULong.valueOf(2), result.get("userId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Idea inputData = new Idea()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutUserID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Idea inputData = new Idea()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setLibraryId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Idea result = new Idea().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOneRecordTypeInLink_Macro() throws Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1,1,0, LinkState.Crafting,Timestamp.valueOf("2014-08-12 12:17:02.527142"),Timestamp.valueOf("2014-08-12 12:17:32.527142"),"C",64, 0.6, 121, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(1,1,3, IdeaType.Macro,0,0);
    IntegrationTestEntity.insertChoice(2,1,1, IdeaType.Main,0,0);

    Idea result = new Idea().setFromRecord(testDAO.readOneRecordTypeInLink(Access.internal(), ULong.valueOf(1), IdeaType.Macro));

    assertNotNull(result);
    assertEquals(ULong.valueOf(3), result.getId());
    assertEquals(ULong.valueOf(2), result.getLibraryId());
    assertEquals("helm", result.getName());
  }

  @Test
  public void readOneRecordTypeInLink_Main() throws Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1,1,0, LinkState.Crafting,Timestamp.valueOf("2014-08-12 12:17:02.527142"),Timestamp.valueOf("2014-08-12 12:17:32.527142"),"C",64, 0.6, 121, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(1,1,3, IdeaType.Macro,0,0);
    IntegrationTestEntity.insertChoice(2,1,1, IdeaType.Main,0,0);

    Idea result = new Idea().setFromRecord(testDAO.readOneRecordTypeInLink(Access.internal(), ULong.valueOf(1), IdeaType.Main));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getId());
    assertEquals(ULong.valueOf(1), result.getLibraryId());
    assertEquals("fonds", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    IdeaRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  // future test: readAllInAccount vs readAllInLibrary, positive and negative cases

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
    assertEquals("fonds", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("nuts", result2.get("name"));
  }

  @Test
  public void readAllBoundToChain() throws  Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertChainIdea(1, 1, 1);

    Result<? extends Record> result = testDAO.readAllBoundToChain(Access.internal(), ULong.valueOf(1), IdeaType.Main);

    assertEquals(1, result.size());
    assertEquals("fonds", result.get(0).get("name"));
    List<String> actualMemes = CSV.split(String.valueOf(result.get(0).get("memes")));
    assertEquals(2, actualMemes.size());
    String[] expectMemes = {"smooth", "leafy"};
    Testing.assertIn(expectMemes, actualMemes.get(0));
    Testing.assertIn(expectMemes, actualMemes.get(1));
  }

  @Test
  public void readAllBoundToChainLibrary() throws  Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertChainLibrary(1, 1, 1);

    Result<? extends Record> result = testDAO.readAllBoundToChainLibrary(Access.internal(), ULong.valueOf(1), IdeaType.Main);

    assertEquals(1, result.size());
    assertEquals("fonds", result.get(0).get("name"));
    List<String> actualMemes = CSV.split(String.valueOf(result.get(0).get("memes")));
    assertEquals(2, actualMemes.size());
    String[] expectMemes = {"smooth", "leafy"};
    Testing.assertIn(expectMemes, actualMemes.get(0));
    Testing.assertIn(expectMemes, actualMemes.get(1));
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

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Idea inputData = new Idea()
      .setName("cannons");

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Idea inputData = new Idea()
      .setLibraryId(BigInteger.valueOf(3));

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Idea inputData = new Idea()
      .setName("cannons")
      .setLibraryId(BigInteger.valueOf(3));

    try {
      testDAO.update(access, ULong.valueOf(3), inputData);

    } catch (Exception e) {
      IdeaRecord result = IntegrationTestService.getDb()
        .selectFrom(IDEA)
        .where(IDEA.ID.eq(ULong.valueOf(3)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(ULong.valueOf(2), result.getLibraryId());
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
    Idea inputData = new Idea()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2));

    testDAO.update(access, ULong.valueOf(3), inputData);

    IdeaRecord result = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(ULong.valueOf(2), result.getLibraryId());
  }

  // future test: DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(2));

    IdeaRecord result = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfIdeaHasChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
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
