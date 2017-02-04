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
import io.outright.xj.core.model.voice_event.VoiceEvent;
import io.outright.xj.core.model.voice_event.VoiceEventWrapper;
import io.outright.xj.core.tables.records.VoiceEventRecord;

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

import static io.outright.xj.core.Tables.VOICE_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete ideas
public class VoiceEventIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private VoiceEventDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, Role.ADMIN);

    // Library "palm tree" has idea "leaves" and idea "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "leaves", 0.342, "C#", 110.286);

    // Idea "leaves" has voices "Intro" and "Outro"
    IntegrationTestEntity.insertPhase(1, 1, 0, 4, "Intro", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, 1, 4, "Outro", 0.583, "E major", 140.0);

    // Voice "Caterpillars" has voices "Drums" and "Bass"
    IntegrationTestEntity.insertVoice(1, 2, Voice.PERCUSSIVE, "Drums");
    IntegrationTestEntity.insertVoice(2, 2, Voice.HARMONIC, "Bass");

    // Voice "Drums" has events "KICK" and "SNARE" 2x each
    IntegrationTestEntity.insertVoiceEvent(1, 1, 0, 1, "KICK", "C", 0.8, 1.0);
    IntegrationTestEntity.insertVoiceEvent(2, 1, 1, 1, "SNARE", "G", 0.1, 0.8);
    IntegrationTestEntity.insertVoiceEvent(3, 1, 2.5, 1, "KICK", "C", 0.8, 0.6);
    IntegrationTestEntity.insertVoiceEvent(4, 1, 3, 1, "SNARE", "G", 0.1, 0.9);

    // Instantiate the test subject
    testDAO = injector.getInstance(VoiceEventDAO.class);
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
    VoiceEventWrapper inputDataWrapper = new VoiceEventWrapper()
      .setVoiceEvent(new VoiceEvent()
        .setDuration(1.4)
        .setInflection("KICK")
        .setNote("C")
        .setPosition(0.42)
        .setTonality(0.92)
        .setVelocity(0.72)
        .setVoiceId(BigInteger.valueOf(2))
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(1.4, actualResult.get("duration"));
    assertEquals(0.42, actualResult.get("position"));
    assertEquals("C", actualResult.get("note"));
    assertEquals("KICK", actualResult.get("inflection"));
    assertEquals(0.92, actualResult.get("tonality"));
    assertEquals(0.72, actualResult.get("velocity"));
    assertEquals(ULong.valueOf(2), actualResult.get("voiceId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutVoiceID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEventWrapper inputDataWrapper = new VoiceEventWrapper()
      .setVoiceEvent(new VoiceEvent()
        .setDuration(1.0)
        .setInflection("KICK")
        .setNote("C")
        .setPosition(0.0)
        .setTonality(1.0)
        .setVelocity(1.0)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutNote() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEventWrapper inputDataWrapper = new VoiceEventWrapper()
      .setVoiceEvent(new VoiceEvent()
        .setDuration(1.0)
        .setInflection("KICK")
        .setPosition(0.0)
        .setTonality(1.0)
        .setVelocity(1.0)
        .setVoiceId(BigInteger.valueOf(2))
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
    assertEquals(ULong.valueOf(1), actualResult.get("voiceId"));
    assertEquals(1.0, actualResult.get("duration"));
    assertEquals("SNARE", actualResult.get("inflection"));
    assertEquals("G", actualResult.get("note"));
    assertEquals(1.0, actualResult.get("position"));
    assertEquals(0.1, actualResult.get("tonality"));
    assertEquals(0.8, actualResult.get("velocity"));
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
    assertEquals("KICK", actualResult1.get("inflection"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("SNARE", actualResult2.get("inflection"));
    JSONObject actualResult3 = (JSONObject) actualResultList.get(2);
    assertEquals("KICK", actualResult3.get("inflection"));
    JSONObject actualResult4 = (JSONObject) actualResultList.get(3);
    assertEquals("SNARE", actualResult4.get("inflection"));
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
  public void update_FailsWithoutVoiceID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEventWrapper inputDataWrapper = new VoiceEventWrapper()
      .setVoiceEvent(new VoiceEvent()
        .setDuration(1.0)
        .setInflection("KICK")
        .setNote("C")
        .setPosition(0.0)
        .setTonality(1.0)
        .setVelocity(1.0)
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutNote() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEventWrapper inputDataWrapper = new VoiceEventWrapper()
      .setVoiceEvent(new VoiceEvent()
        .setDuration(1.0)
        .setInflection("KICK")
        .setPosition(0.0)
        .setTonality(1.0)
        .setVelocity(1.0)
        .setVoiceId(BigInteger.valueOf(2))
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentVoice() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEventWrapper inputDataWrapper = new VoiceEventWrapper()
      .setVoiceEvent(new VoiceEvent()
        .setDuration(1.0)
        .setInflection("SNARE")
        .setNote("C")
        .setPosition(0.0)
        .setTonality(1.0)
        .setVelocity(1.0)
        .setVoiceId(BigInteger.valueOf(287))
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);

    VoiceEventRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(VOICE_EVENT)
      .where(VOICE_EVENT.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("KICK", updatedRecord.getInflection());
    assertEquals(ULong.valueOf(2), updatedRecord.getVoiceId());
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEventWrapper inputDataWrapper = new VoiceEventWrapper()
      .setVoiceEvent(new VoiceEvent()
        .setDuration(1.2)
        .setInflection("POPPYCOCK")
        .setNote("C")
        .setPosition(0.42)
        .setTonality(0.92)
        .setVelocity(0.72)
        .setVoiceId(BigInteger.valueOf(1))
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    VoiceEventRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(VOICE_EVENT)
      .where(VOICE_EVENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("POPPYCOCK", updatedRecord.getInflection());
    assertEquals((Double) 1.2, updatedRecord.getDuration());
    assertEquals((Double) 0.42, updatedRecord.getPosition());
    assertEquals(0.92, updatedRecord.get("tonality"));
    assertEquals(0.72, updatedRecord.get("velocity"));
    assertEquals(ULong.valueOf(1), updatedRecord.getVoiceId());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    VoiceEventRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(VOICE_EVENT)
      .where(VOICE_EVENT.ID.eq(ULong.valueOf(1)))
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
