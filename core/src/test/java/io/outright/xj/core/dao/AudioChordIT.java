// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.audio_chord.AudioChord;
import io.outright.xj.core.model.audio_chord.AudioChordWrapper;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.AudioChordRecord;

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

import static io.outright.xj.core.Tables.AUDIO_CHORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete audio chords
public class AudioChordIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private AudioChordDAO testDAO;

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

    // Idea "leaves" has instruments "808" and "909"
    IntegrationTestEntity.insertInstrument(1, 1, 2, "Harmonic Performance", Instrument.PERCUSSIVE, 0.9);

    // Instrument "808" has Audio "Chords Cm to D"
    IntegrationTestEntity.insertAudio(1, 1, "Chords Cm to D", "https://static.xj.outright.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);

    // Audio "Drums" has events "C minor" and "D major" 2x each
    //
    IntegrationTestEntity.insertAudioChord(1, 1, 4, "D major");
    IntegrationTestEntity.insertAudioChord(2, 1, 0, "C minor");

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioChordDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  // TODO cannot create or update a audio to an offset that already exists for that audio chord

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioChordWrapper inputDataWrapper = new AudioChordWrapper()
      .setAudioChord(new AudioChord()
        .setPosition(0.42)
        .setName("G minor 7")
        .setAudioId(BigInteger.valueOf(1))
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(0.42, actualResult.get("position"));
    assertEquals("G minor 7", actualResult.get("name"));
    assertEquals(ULong.valueOf(1), actualResult.get("audioId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioChordWrapper inputDataWrapper = new AudioChordWrapper()
      .setAudioChord(new AudioChord()
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
    AudioChordWrapper inputDataWrapper = new AudioChordWrapper()
      .setAudioChord(new AudioChord()
        .setPosition(0.42)
        .setAudioId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(1));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("id"));
    assertEquals(ULong.valueOf(1), actualResult.get("audioId"));
    assertEquals("D major", actualResult.get("name"));
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
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("C minor", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("D major", actualResult2.get("name"));
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
  public void update_FailsWithoutAudioID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioChordWrapper inputDataWrapper = new AudioChordWrapper()
      .setAudioChord(new AudioChord()
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
    AudioChordWrapper inputDataWrapper = new AudioChordWrapper()
      .setAudioChord(new AudioChord()
        .setPosition(0.42)
        .setAudioId(BigInteger.valueOf(2))
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioChordWrapper inputDataWrapper = new AudioChordWrapper()
      .setAudioChord(new AudioChord()
        .setPosition(0.42)
        .setAudioId(BigInteger.valueOf(57))
        .setName("cannons")
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);

    AudioChordRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(AUDIO_CHORD)
      .where(AUDIO_CHORD.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("cannons", updatedRecord.getName());
    assertEquals(ULong.valueOf(2), updatedRecord.getAudioId());
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioChordWrapper inputDataWrapper = new AudioChordWrapper()
      .setAudioChord(new AudioChord()
        .setAudioId(BigInteger.valueOf(1))
        .setName("POPPYCOCK")
        .setPosition(0.42)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    AudioChordRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(AUDIO_CHORD)
      .where(AUDIO_CHORD.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("POPPYCOCK", updatedRecord.getName());
    assertEquals((Double) 0.42, updatedRecord.getPosition());
    assertEquals(ULong.valueOf(1), updatedRecord.getAudioId());
  }

  // TODO: [core] test DAO cannot update audio chord to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    AudioChordRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(AUDIO_CHORD)
      .where(AUDIO_CHORD.ID.eq(ULong.valueOf(1)))
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
