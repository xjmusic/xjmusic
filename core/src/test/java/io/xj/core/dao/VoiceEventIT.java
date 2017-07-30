// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.role.Role;
import io.xj.core.model.voice_event.VoiceEvent;
import io.xj.core.tables.records.VoiceEventRecord;
import io.xj.core.transport.JSON;

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

import static io.xj.core.Tables.VOICE_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to readMany vs. create vs. update or delete voice events
public class VoiceEventIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private VoiceEventDAO testDAO;

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
    IntegrationTestEntity.insertIdea(1, 2, 1, IdeaType.Main, "leaves", 0.342, "C#", 110.286);

    // Idea "leaves" has voices "Intro" and "Outro"
    IntegrationTestEntity.insertPhase(1, 1, 0, 4, "Intro", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, 1, 4, "Outro", 0.583, "E major", 140.0);

    // Voice "Caterpillars" has voices "Drums" and "Bass"
    IntegrationTestEntity.insertVoice(1, 2, InstrumentType.Percussive, "Drums");
    IntegrationTestEntity.insertVoice(2, 2, InstrumentType.Harmonic, "Bass");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertVoiceEvent(1, 1, 0, 1, "BOOM", "C", 0.8, 1.0);
    IntegrationTestEntity.insertVoiceEvent(2, 1, 1, 1, "SMACK", "G", 0.1, 0.8);
    IntegrationTestEntity.insertVoiceEvent(3, 1, 2.5, 1, "BOOM", "C", 0.8, 0.6);
    IntegrationTestEntity.insertVoiceEvent(4, 1, 3, 1, "SMACK", "G", 0.1, 0.9);

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
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEvent inputData = new VoiceEvent()
      .setDuration(1.4)
      .setInflection("BOOM")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setVoiceId(BigInteger.valueOf(2));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(1.4, result.get("duration"));
    assertEquals(0.42, result.get("position"));
    assertEquals("C", result.get("note"));
    assertEquals("BOOM", result.get("inflection"));
    assertEquals(0.92, result.get("tonality"));
    assertEquals(0.72, result.get("velocity"));
    assertEquals(ULong.valueOf(2), result.get("voiceId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutVoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEvent inputData = new VoiceEvent()
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
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEvent inputData = new VoiceEvent()
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
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    VoiceEvent result = new VoiceEvent().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getVoiceId());
    assertEquals(Double.valueOf(1.0), result.getDuration());
    assertEquals("SMACK", result.getInflection());
    assertEquals("G", result.getNote());
    assertEquals(Double.valueOf(1.0), result.getPosition());
    assertEquals(Double.valueOf(0.1), result.getTonality());
    assertEquals(Double.valueOf(0.8), result.getVelocity());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    VoiceEventRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

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
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutVoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEvent inputData = new VoiceEvent()
      .setDuration(1.0)
      .setInflection("BOOM")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutNote() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEvent inputData = new VoiceEvent()
      .setDuration(1.0)
      .setInflection("BOOM")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setVoiceId(BigInteger.valueOf(2));

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentVoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEvent inputData = new VoiceEvent()
      .setDuration(1.0)
      .setInflection("SMACK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setVoiceId(BigInteger.valueOf(287));

    try {
      testDAO.update(access, ULong.valueOf(3), inputData);

    } catch (Exception e) {
      VoiceEventRecord result = IntegrationTestService.getDb()
        .selectFrom(VOICE_EVENT)
        .where(VOICE_EVENT.ID.eq(ULong.valueOf(3)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("BOOM", result.getInflection());
      assertEquals(ULong.valueOf(1), result.getVoiceId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    VoiceEvent inputData = new VoiceEvent()
      .setDuration(1.2)
      .setInflection("POPPYCOCK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setVoiceId(BigInteger.valueOf(1));

    testDAO.update(access, ULong.valueOf(1), inputData);

    VoiceEventRecord result = IntegrationTestService.getDb()
      .selectFrom(VOICE_EVENT)
      .where(VOICE_EVENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getInflection());
    assertEquals((Double) 1.2, result.getDuration());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(0.92, result.get("tonality"));
    assertEquals(0.72, result.get("velocity"));
    assertEquals(ULong.valueOf(1), result.getVoiceId());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    VoiceEventRecord result = IntegrationTestService.getDb()
      .selectFrom(VOICE_EVENT)
      .where(VOICE_EVENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

}
