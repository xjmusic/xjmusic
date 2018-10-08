// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete pattern entities

// future test readAllSequences() which supports [#154234716] library ingest requires enumerating all possible sub-sequences of entities for any pattern

public class PatternChordIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private PatternChordDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Main, SequenceState.Published, "leaves", 0.342, "C#", 110.286);

    // Sequence "leaves" has patterns "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPatternSequencePattern(1, 1, PatternType.Main, PatternState.Published, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPatternSequencePattern(2, 1, PatternType.Main, PatternState.Published, 1, 16, "Caterpillars", 0.583, "E major", 140.0);

    // Pattern "Caterpillars" has entities "C minor" and "D major"
    IntegrationTestEntity.insertPatternChord(1, 2, 0, "C minor");
    IntegrationTestEntity.insertPatternChord(2, 2, 4, "D major");

    // Instantiate the test subject
    testDAO = injector.getInstance(PatternChordDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setName("G minor 7")
      .setPatternId(BigInteger.valueOf(2L));

    JSONObject result = JSON.objectFrom(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(4.0, result.get("position"));
    assertEquals("G minor 7", result.get("name"));
    assertEquals(2, result.get("patternId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setPatternId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    PatternChord result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(2L), result.getPatternId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    PatternChord result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(2L))));

    assertNotNull(result);
    assertEquals(2L, (long) result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("C minor", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("D major", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(0L, (long) result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setPatternId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setPatternId(BigInteger.valueOf(57L))
      .setName("D minor");

    try {
      testDAO.update(access, BigInteger.valueOf(2L), inputData);

    } catch (Exception e) {
      PatternChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals("D major", result.getName());
      assertEquals(BigInteger.valueOf(2L), result.getPatternId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPatternId(BigInteger.valueOf(1L))
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    PatternChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals(Double.valueOf(4.0), result.getPosition());
    assertEquals(BigInteger.valueOf(1L), result.getPatternId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    PatternChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

}
