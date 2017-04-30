// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.model.voice.VoiceWrapper;
import io.outright.xj.core.tables.records.VoiceRecord;

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

import static io.outright.xj.core.tables.Voice.VOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete ideas
public class VoiceIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private VoiceDAO testDAO;

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

    // Phase "Ants" has Voices "Head" and "Body"
    IntegrationTestEntity.insertVoice(1, 1, Voice.PERCUSSIVE, "This is a percussive voice");
    IntegrationTestEntity.insertVoice(2, 1, Voice.MELODIC, "This is melodious");
    IntegrationTestEntity.insertVoice(3, 1, Voice.HARMONIC, "This is harmonious");
    IntegrationTestEntity.insertVoice(4, 1, Voice.VOCAL, "This is a vocal voice");

    // Instantiate the test subject
    testDAO = injector.getInstance(VoiceDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  // TODO cannot create or update a voice to an offset that already exists for that idea

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceWrapper inputDataWrapper = new VoiceWrapper()
      .setVoice(new Voice()
        .setPhaseId(BigInteger.valueOf(2))
        .setType(Voice.HARMONIC)
        .setDescription("This is harmonious")
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(Voice.HARMONIC, result.get("type"));
    assertEquals("This is harmonious", result.get("description"));
    assertEquals(ULong.valueOf(2), result.get("phaseId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutPhaseID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceWrapper inputDataWrapper = new VoiceWrapper()
      .setVoice(new Voice()
        .setType(Voice.HARMONIC)
        .setDescription("This is harmonious")
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutType() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceWrapper inputDataWrapper = new VoiceWrapper()
      .setVoice(new Voice()
        .setPhaseId(BigInteger.valueOf(2))
        .setDescription("This is harmonious")
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
    assertEquals(ULong.valueOf(1), result.get("phaseId"));
    assertEquals(Voice.MELODIC, result.get("type"));
    assertEquals("This is melodious", result.get("description"));
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

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(4, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("This is a percussive voice", actualResult1.get("description"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("This is melodious", actualResult2.get("description"));
    JSONObject actualResult3 = (JSONObject) actualResultList.get(2);
    assertEquals("This is harmonious", actualResult3.get("description"));
    JSONObject actualResult4 = (JSONObject) actualResultList.get(3);
    assertEquals("This is a vocal voice", actualResult4.get("description"));
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
    VoiceWrapper inputDataWrapper = new VoiceWrapper()
      .setVoice(new Voice()
        .setType(Voice.HARMONIC)
        .setDescription("This is harmonious")
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutType() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceWrapper inputDataWrapper = new VoiceWrapper()
      .setVoice(new Voice()
        .setPhaseId(BigInteger.valueOf(2))
        .setDescription("This is harmonious")
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentPhase() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceWrapper inputDataWrapper = new VoiceWrapper()
      .setVoice(new Voice()
        .setPhaseId(BigInteger.valueOf(7))
        .setType(Voice.MELODIC)
        .setDescription("This is melodious")
      );

    try {
      testDAO.update(access, ULong.valueOf(3), inputDataWrapper);

    } catch (Exception e) {
      VoiceRecord result = IntegrationTestService.getDb()
        .selectFrom(VOICE)
        .where(VOICE.ID.eq(ULong.valueOf(3)))
        .fetchOne();
      assertNotNull(result);
      assertEquals(Voice.HARMONIC, result.getType());
      assertEquals("This is harmonious", result.getDescription());
      assertEquals(ULong.valueOf(1), result.getPhaseId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceWrapper inputDataWrapper = new VoiceWrapper()
      .setVoice(new Voice()
        .setPhaseId(BigInteger.valueOf(1))
        .setType(Voice.MELODIC)
        .setDescription("This is melodious; Yoza!")
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    VoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(VOICE)
      .where(VOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("This is melodious; Yoza!", result.getDescription());
    assertEquals(Voice.MELODIC, result.getType());
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

    VoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(VOICE)
      .where(VOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfIdeaHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2",
      "roles", "artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertVoiceEvent(1, 1, 0.42, 0.41, "HEAVY", "C", 0.7, 0.98);

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      VoiceRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(VOICE)
        .where(VOICE.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }

  // TODO [core] test VoiceDAO cannot delete record unless user has account access

}
