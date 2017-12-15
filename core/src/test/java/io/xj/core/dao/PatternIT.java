// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;

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
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete patterns
public class PatternIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private PatternDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "palm tree" has pattern "fonds" and pattern "nuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Main, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPatternMeme(12, 1, "leafy");
    IntegrationTestEntity.insertPatternMeme(14, 1, "smooth");
    IntegrationTestEntity.insertPattern(2, 2, 1, PatternType.Rhythm, "nuts", 0.342, "C#", 0.286);

    // Library "boat" has pattern "helm" and pattern "sail"
    IntegrationTestEntity.insertLibrary(2, 1, "boat");
    IntegrationTestEntity.insertPattern(3, 3, 2, PatternType.Macro, "helm", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(4, 2, 2, PatternType.Detail, "sail", 0.342, "C#", 0.286);

    // Instantiate the test subject
    testDAO = injector.getInstance(PatternDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Pattern inputData = new Pattern()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2));

    Pattern result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(PatternType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2), result.getUserId());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Pattern inputData = new Pattern()
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
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Pattern inputData = new Pattern()
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
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Pattern result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOneTypeInLink_Macro() throws Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Crafting, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-08-12 12:17:32.527142"), "C", 64, 0.6, 121, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(1, 1, 3, PatternType.Macro, 0, 0);
    IntegrationTestEntity.insertChoice(2, 1, 1, PatternType.Main, 0, 0);

    Pattern result = testDAO.readOneTypeInLink(Access.internal(), BigInteger.valueOf(1), PatternType.Macro);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(3), result.getId());
    assertEquals(BigInteger.valueOf(2), result.getLibraryId());
    assertEquals("helm", result.getName());
  }

  @Test
  public void readOneTypeInLink_Main() throws Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Crafting, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-08-12 12:17:32.527142"), "C", 64, 0.6, 121, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(1, 1, 3, PatternType.Macro, 0, 0);
    IntegrationTestEntity.insertChoice(2, 1, 1, PatternType.Main, 0, 0);

    Pattern result = testDAO.readOneTypeInLink(Access.internal(), BigInteger.valueOf(1), PatternType.Main);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLibraryId());
    assertEquals("fonds", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Pattern result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  // future test: readAllInAccount vs readAllInLibrary, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAllInLibrary(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("fonds", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("nuts", result2.get("name"));
  }

  @Test
  public void readAllBoundToChain() throws Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertChainPattern(1, 1, 1);

    Collection<Pattern> result = testDAO.readAllBoundToChain(Access.internal(), BigInteger.valueOf(1), PatternType.Main);

    assertEquals(1, result.size());
    Pattern result0 = result.iterator().next();
    assertEquals("fonds", result0.getName());
  }

  @Test
  public void readAllBoundToChainLibrary() throws Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertChainLibrary(1, 1, 1);

    Collection<Pattern> result = testDAO.readAllBoundToChainLibrary(Access.internal(), BigInteger.valueOf(1), PatternType.Main);

    assertEquals(1, result.size());
    Pattern result0 = result.iterator().next();
    assertEquals("fonds", result0.getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAllInLibrary(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Pattern inputData = new Pattern()
      .setName("cannons");

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Pattern inputData = new Pattern()
      .setLibraryId(BigInteger.valueOf(3));

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Pattern inputData = new Pattern()
      .setName("cannons")
      .setLibraryId(BigInteger.valueOf(3));

    try {
      testDAO.update(access, BigInteger.valueOf(3), inputData);

    } catch (Exception e) {
      Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(BigInteger.valueOf(2), result.getLibraryId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Pattern inputData = new Pattern()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2));

    testDAO.update(access, BigInteger.valueOf(3), inputData);

    Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(2), result.getLibraryId());
  }

  // future test: DAO cannot update Pattern to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.delete(access, BigInteger.valueOf(2));

    Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfPatternHasChilds() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertPhase(1, 2, 0, 14, "testPhase", 0.524, "F#", 125.49);

    try {
      testDAO.delete(access, BigInteger.valueOf(2));

    } catch (Exception e) {
      Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
      assertNotNull(result);
      throw e;
    }
  }

}
