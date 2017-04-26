// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.phase_chord.PhaseChord;
import io.outright.xj.core.model.phase_chord.PhaseChordWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.PhaseChordRecord;

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

import static io.outright.xj.core.Tables.PHASE_CHORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete phase chords
public class PhaseChordIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private PhaseChordDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Library "palm tree" has idea "leaves" and idea "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "leaves", 0.342, "C#", 110.286);

    // Idea "leaves" has phases "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, 1, 16, "Caterpillars", 0.583, "E major", 140.0);

    // Phase "Caterpillars" has chords "C minor" and "D major"
    IntegrationTestEntity.insertPhaseChord(1, 2, 0, "C minor");
    IntegrationTestEntity.insertPhaseChord(2, 2, 4, "D major");

    // Instantiate the test subject
    testDAO = injector.getInstance(PhaseChordDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  // TODO cannot create or update a phase to an offset that already exists for that idea

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseChordWrapper inputDataWrapper = new PhaseChordWrapper()
      .setPhaseChord(new PhaseChord()
        .setPosition(0.42)
        .setName("G minor 7")
        .setPhaseId(BigInteger.valueOf(2))
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(0.42, result.get("position"));
    assertEquals("G minor 7", result.get("name"));
    assertEquals(ULong.valueOf(2), result.get("phaseId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutPhaseID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseChordWrapper inputDataWrapper = new PhaseChordWrapper()
      .setPhaseChord(new PhaseChord()
        .setPosition(0.42)
        .setName("G minor 7")
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseChordWrapper inputDataWrapper = new PhaseChordWrapper()
      .setPhaseChord(new PhaseChord()
        .setPosition(0.42)
        .setPhaseId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.get("id"));
    assertEquals(ULong.valueOf(2), result.get("phaseId"));
    assertEquals("D major", result.get("name"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(2));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("C minor", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("D major", actualResult2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutPhaseID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseChordWrapper inputDataWrapper = new PhaseChordWrapper()
      .setPhaseChord(new PhaseChord()
        .setPosition(0.42)
        .setName("G minor 7")
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseChordWrapper inputDataWrapper = new PhaseChordWrapper()
      .setPhaseChord(new PhaseChord()
        .setPosition(0.42)
        .setPhaseId(BigInteger.valueOf(2))
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentPhase() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseChordWrapper inputDataWrapper = new PhaseChordWrapper()
      .setPhaseChord(new PhaseChord()
        .setPosition(0.42)
        .setPhaseId(BigInteger.valueOf(57))
        .setName("D minor")
      );

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      PhaseChordRecord result = IntegrationTestService.getDb()
        .selectFrom(PHASE_CHORD)
        .where(PHASE_CHORD.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("D major", result.getName());
      assertEquals(ULong.valueOf(2), result.getPhaseId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseChordWrapper inputDataWrapper = new PhaseChordWrapper()
      .setPhaseChord(new PhaseChord()
        .setPhaseId(BigInteger.valueOf(1))
        .setName("POPPYCOCK")
        .setPosition(0.42)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    PhaseChordRecord result = IntegrationTestService.getDb()
      .selectFrom(PHASE_CHORD)
      .where(PHASE_CHORD.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(ULong.valueOf(1), result.getPhaseId());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    PhaseChordRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(PHASE_CHORD)
      .where(PHASE_CHORD.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

}
