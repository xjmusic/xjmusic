// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.AudioEventRecord;
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
import java.util.List;

import static io.xj.core.Tables.AUDIO_EVENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to readMany vs. create vs. update or delete audio events
//
public class AudioEventIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private AudioEventDAO testDAO;

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

    // Idea "leaves" has instruments "808" and "909"
    IntegrationTestEntity.insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has Audio "Beat"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Beat", "https://static.xj.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);

    // Audio "Drums" has events "KICK" and "SNARE" 2x each
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(2, 1, 3, 1, "SNARE", "Ab", 0.1, 0.8);
    IntegrationTestEntity.insertAudioEvent(3, 1, 0, 1, "KICK", "C", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(4, 1, 1, 1, "SNARE", "G", 0.1, 0.8);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioEventDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  // TODO cannot create or update a audio to an offset that already exists for that idea

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.4)
      .setInflection("KICK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setAudioId(BigInteger.valueOf(1));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(1.4, result.get("duration"));
    assertEquals(0.42, result.get("position"));
    assertEquals("C", result.get("note"));
    assertEquals("KICK", result.get("inflection"));
    assertEquals(0.92, result.get("tonality"));
    assertEquals(0.72, result.get("velocity"));
    assertEquals(ULong.valueOf(1), result.get("audioId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
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
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    AudioEvent result = new AudioEvent().setFromRecord(testDAO.readOne(access, ULong.valueOf(4)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(4), result.getId());
    assertEquals(ULong.valueOf(1), result.getAudioId());
    assertEquals(Double.valueOf(1.0), result.getDuration());
    assertEquals("SNARE", result.getInflection());
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

    AudioEventRecord result = testDAO.readOne(access, ULong.valueOf(1));

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
    assertEquals("KICK", result1.get("inflection"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("SNARE", result2.get("inflection"));
    JSONObject result3 = (JSONObject) result.get(2);
    assertEquals("KICK", result3.get("inflection"));
    JSONObject result4 = (JSONObject) result.get(3);
    assertEquals("SNARE", result4.get("inflection"));
  }

  @Test
  public void readAllFirstEventsForInstrument() throws Exception {
    List<AudioEvent> result = testDAO.readAllFirstEventsForInstrument(Access.internal(), ULong.valueOf(1));

    assertNotNull(result);
    assertEquals(1, result.size());
    AudioEvent result1 = result.get(0);
    assertEquals("KICK", result1.getInflection());
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
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
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
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(2));

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("SNARE")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(287));

    try {
      testDAO.update(access, ULong.valueOf(3), inputData);

    } catch (Exception e) {
      AudioEventRecord result = IntegrationTestService.getDb()
        .selectFrom(AUDIO_EVENT)
        .where(AUDIO_EVENT.ID.eq(ULong.valueOf(3)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("KICK", result.getInflection());
      assertEquals(ULong.valueOf(1), result.getAudioId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.2)
      .setInflection("POPPYCOCK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setAudioId(BigInteger.valueOf(1));

    testDAO.update(access, ULong.valueOf(1), inputData);

    AudioEventRecord result = IntegrationTestService.getDb()
      .selectFrom(AUDIO_EVENT)
      .where(AUDIO_EVENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getInflection());
    assertEquals((Double) 1.2, result.getDuration());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(0.92, result.get("tonality"));
    assertEquals(0.72, result.get("velocity"));
    assertEquals(ULong.valueOf(1), result.getAudioId());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    AudioEventRecord result = IntegrationTestService.getDb()
      .selectFrom(AUDIO_EVENT)
      .where(AUDIO_EVENT.ID.eq(ULong.valueOf(1)))
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
