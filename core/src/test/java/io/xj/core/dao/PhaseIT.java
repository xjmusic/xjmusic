// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete phases
public class PhaseIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private PhaseDAO testDAO;

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

    // Library "palm tree" has pattern "leaves" and pattern "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Main, "leaves", 0.342, "C#", 110.286);
    IntegrationTestEntity.insertPattern(2, 2, 1, PatternType.Macro, "coconuts", 8.02, "D", 130.2);

    // Pattern "leaves" has phases "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, 1, 16, "Caterpillars", 0.583, "E major", 140.0);

    // Instantiate the test subject
    testDAO = injector.getInstance(PhaseDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(16);

    Phase result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.01);
    assertEquals(BigInteger.valueOf(16), result.getOffset());
    assertEquals(Integer.valueOf(16), result.getTotal());
  }

  @Test
  public void create_TotalNotRequiredForMacroPatternPhase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    Phase result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertNull(result.getTotal());
  }

  @Test
  public void create_TotalIsRequiredForNonMacroTypePatternPhase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_TotalMustBeGreaterThanZeroForNonMacroTypePatternPhase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(0);

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) must be greater than zero");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_NullOptionalFieldsAllowed() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(null)
      .setKey(null)
      .setPatternId(BigInteger.valueOf(2))
      .setName(null)
      .setTempo(null)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

    Phase result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertNull(result.getDensity());
    assertNull(result.getKey());
    assertNull(result.getName());
    assertNull(result.getTempo());
    assertEquals(BigInteger.valueOf(0), result.getOffset());
    assertEquals(Integer.valueOf(16), result.getTotal());
  }

  @Test
  public void create_FailsWithoutPatternID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutOffset() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setPatternId(BigInteger.valueOf(2))
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    testDAO.create(access, inputData);
  }

  /**
   [#237] shouldn't be able to create phase with same offset in pattern
   */
  @Test
  public void create_FailsIfPatternAlreadyHasPhaseWithThisOffset() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Phase inputData = new Phase()
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.42)
      .setPatternId(BigInteger.valueOf(1))
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Found phase with same offset in pattern");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Phase result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getPatternId());
    assertEquals("Caterpillars", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    Phase result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readOneForPattern() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Phase result = testDAO.readOneForPattern(access, BigInteger.valueOf(1), BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getPatternId());
    assertEquals("Caterpillars", result.getName());
  }

  @Test
  public void readOneForPattern_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "143"
    ));

    Phase result = testDAO.readOneForPattern(access, BigInteger.valueOf(1), BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("Ants", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("Caterpillars", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  // future test: DAO cannot update Pattern to a User or Library not owned by current session

  @Test
  public void update() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setPatternId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(7))
      .setTotal(32)
      .setName(null)
      .setDensity(null)
      .setKey("")
      .setTempo((double) 0);

    testDAO.update(access, BigInteger.valueOf(1), inputData);

    Phase result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertNull(result.getName());
    assertNull(result.getDensity());
    assertNull(result.getTempo());
    assertNull(result.getKey());
    assertEquals(BigInteger.valueOf(7), result.getOffset());
    assertEquals(Integer.valueOf(32), result.getTotal());
    assertEquals(BigInteger.valueOf(1), result.getPatternId());
  }

  @Test
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    testDAO.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_TotalNotRequiredForMacroPatternPhase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    testDAO.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_TotalIsRequiredForNonMacroTypePatternPhase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) is required");

    testDAO.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_TotalMustBeGreaterThanZeroForNonMacroTypePatternPhase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(0);

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) must be greater than zero");

    testDAO.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_FailsWithoutOffset() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    testDAO.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(57))
      .setName("Smash!")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern does not exist");

    try {
      testDAO.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      Phase result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals("Caterpillars", result.getName());
      assertEquals(BigInteger.valueOf(1), result.getPatternId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.delete(access, BigInteger.valueOf(1));

    Phase result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.delete(access, BigInteger.valueOf(1));
  }

  @Test
  public void delete_FailsIfPatternHasChilds() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertPhaseMeme(1, 1, "mashup");

    failure.expect(BusinessException.class);
    failure.expectMessage("Found Meme in Phase");

    try {
      testDAO.delete(access, BigInteger.valueOf(1));

    } catch (Exception e) {
      Phase result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
      assertNotNull(result);
      throw e;
    }
  }

  // future test: PhaseDAO cannot delete record unless user has account access

}
