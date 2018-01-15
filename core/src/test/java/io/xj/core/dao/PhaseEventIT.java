// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.phase_event.PhaseEvent;
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

// future test: permissions of different users to readMany vs. create vs. update or delete voice events
public class PhaseEventIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private PhaseEventDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Library "palm tree" has pattern "leaves" and pattern "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Main, "leaves", 0.342, "C#", 110.286);

    // Pattern "leaves" has voices "Intro" and "Outro"
    IntegrationTestEntity.insertPhase(1, 1, PhaseType.Main, 0, 4, "Intro", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, PhaseType.Main, 1, 4, "Outro", 0.583, "E major", 140.0);

    // Voice "Caterpillars" has voices "Drums" and "Bass"
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "Drums");
    IntegrationTestEntity.insertVoice(2, 1, InstrumentType.Harmonic, "Bass");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertPhaseEvent(1, 1, 1, 0, 1, "BOOM", "C", 0.8, 1.0);
    IntegrationTestEntity.insertPhaseEvent(2, 1, 1, 1, 1, "SMACK", "G", 0.1, 0.8);
    IntegrationTestEntity.insertPhaseEvent(3, 1, 1, 2.5, 1, "BOOM", "C", 0.8, 0.6);
    IntegrationTestEntity.insertPhaseEvent(4, 1, 1, 3, 1, "SMACK", "G", 0.1, 0.9);

    // Instantiate the test subject
    testDAO = injector.getInstance(PhaseEventDAO.class);
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
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.4)
      .setInflection("BOOM")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setPhaseId(BigInteger.valueOf(1))
      .setVoiceId(BigInteger.valueOf(2));

    JSONObject result = JSON.objectFrom(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(1.4, result.get("duration"));
    assertEquals(0.42, result.get("position"));
    assertEquals("C", result.get("note"));
    assertEquals("BOOM", result.get("inflection"));
    assertEquals(0.92, result.get("tonality"));
    assertEquals(0.72, result.get("velocity"));
    assertEquals(1, result.get("phaseId"));
    assertEquals(2, result.get("voiceId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutVoiceID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.0)
      .setInflection("BOOM")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutNote() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.0)
      .setInflection("BOOM")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setVoiceId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    PhaseEvent result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getVoiceId());
    assertEquals(Double.valueOf(1.0), result.getDuration());
    assertEquals("SMACK", result.getInflection());
    assertEquals("G", result.getNote());
    assertEquals(Double.valueOf(1.0), result.getPosition());
    assertEquals(Double.valueOf(0.1), result.getTonality());
    assertEquals(Double.valueOf(0.8), result.getVelocity());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    PhaseEvent result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(4, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("BOOM", result1.get("inflection"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("SMACK", result2.get("inflection"));
    JSONObject result3 = (JSONObject) result.get(2);
    assertEquals("BOOM", result3.get("inflection"));
    JSONObject result4 = (JSONObject) result.get(3);
    assertEquals("SMACK", result4.get("inflection"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutVoiceID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.0)
      .setInflection("BOOM")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setPhaseId(BigInteger.valueOf(1))
      .setVelocity(1.0);

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutPhaseID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.0)
      .setInflection("BOOM")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVoiceId(BigInteger.valueOf(1))
      .setVelocity(1.0);

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutNote() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.0)
      .setInflection("BOOM")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setVoiceId(BigInteger.valueOf(2));

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentVoice() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.0)
      .setInflection("SMACK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setPhaseId(BigInteger.valueOf(1))
      .setVoiceId(BigInteger.valueOf(287));

    try {
      testDAO.update(access, BigInteger.valueOf(3), inputData);

    } catch (Exception e) {
      PhaseEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
      assertNotNull(result);
      assertEquals("BOOM", result.getInflection());
      assertEquals(BigInteger.valueOf(1), result.getVoiceId());
      throw e;
    }
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentPhase() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.0)
      .setInflection("SMACK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setPhaseId(BigInteger.valueOf(287))
      .setVoiceId(BigInteger.valueOf(1));

    try {
      testDAO.update(access, BigInteger.valueOf(3), inputData);

    } catch (Exception e) {
      PhaseEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3));
      assertNotNull(result);
      assertEquals("BOOM", result.getInflection());
      assertEquals(BigInteger.valueOf(1), result.getPhaseId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PhaseEvent inputData = new PhaseEvent()
      .setDuration(1.2)
      .setInflection("POPPYCOCK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setPhaseId(BigInteger.valueOf(1))
      .setVoiceId(BigInteger.valueOf(1));

    testDAO.update(access, BigInteger.valueOf(1), inputData);

    PhaseEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getInflection());
    assertEquals((Double) 1.2, result.getDuration());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(0.92, result.getTonality(), 0.01);
    assertEquals(0.72, result.getVelocity(), 0.01);
    assertEquals(BigInteger.valueOf(1), result.getVoiceId());
    assertEquals(BigInteger.valueOf(1), result.getPhaseId());
  }

  // future test: DAO cannot update Pattern to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    PhaseEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

}
