// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.model.voice.VoiceWrapper;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.tables.records.VoiceRecord;

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
    IntegrationTestEntity.insertUserRole(2, Role.ADMIN);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, Role.USER);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Library "palm tree" has idea "leaves" and idea "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "leaves", 0.342, "C#", 110.286);

    // Idea "leaves" has phases "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, 1, 16, "Caterpillars", 0.583, "E major", 140.0);

    // Phase "Ants" has Voices "Head" and "Body"
    IntegrationTestEntity.insertVoice(1, 1, Voice.PERCUSSIVE, "This is a percussive voice");
    IntegrationTestEntity.insertVoice(2, 1, Voice.MELODIC, "This is melodious");
    IntegrationTestEntity.insertVoice(3, 1, Voice.HARMONIC, "This is a harmonic voice");
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

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(Voice.HARMONIC, actualResult.get("type"));
    assertEquals("This is harmonious", actualResult.get("description"));
    assertEquals(ULong.valueOf(2), actualResult.get("phaseId"));
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

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(2), actualResult.get("id"));
    assertEquals(ULong.valueOf(1), actualResult.get("phaseId"));
    assertEquals(Voice.MELODIC, actualResult.get("type"));
    assertEquals("This is melodious", actualResult.get("description"));
  }

  @Test
  public void readOneAble_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(actualResult);
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
    assertEquals("This is a harmonic voice", actualResult3.get("description"));
    JSONObject actualResult4 = (JSONObject) actualResultList.get(3);
    assertEquals("This is a vocal voice", actualResult4.get("description"));
  }

  @Test
  public void readAllAble_SeesNothingOutsideOfLibrary() throws Exception {
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
        .setType(Voice.HARMONIC)
        .setDescription("This is harmonious")
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
        .setDescription("This is melodic; Yoza!")
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    VoiceRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(VOICE)
      .where(VOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("This is melodic; Yoza!", updatedRecord.getDescription());
    assertEquals(Voice.MELODIC, updatedRecord.getType());
    assertEquals(ULong.valueOf(1), updatedRecord.getPhaseId());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    VoiceRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(VOICE)
      .where(VOICE.ID.eq(ULong.valueOf(1)))
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
