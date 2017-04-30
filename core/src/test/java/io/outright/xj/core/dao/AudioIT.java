// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.audio.Audio;
import io.outright.xj.core.model.audio.AudioWrapper;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.AudioRecord;

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

import static io.outright.xj.core.tables.Audio.AUDIO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

// TODO [core] test permissions of different users to read vs. create vs. update or delete audios
public class AudioIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private AudioDAO testDAO;

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
    IntegrationTestEntity.insertInstrument(1, 1, 2, "808 Drums", Instrument.PERCUSSIVE, 0.9);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "909 Drums", Instrument.PERCUSSIVE, 0.8);

    // Instrument "808" has Audios "Kick" and "Snare"
    IntegrationTestEntity.insertAudio(1, 1, "Kick", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudio(2, 1, "Snare", "instrument/percussion/808/snare.wav", 0.0023, 1.05, 131.0, 702);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  // TODO cannot create or update a audio to an offset that already exists for that idea

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioWrapper inputDataWrapper = new AudioWrapper()
      .setAudio(new Audio()
        .setInstrumentId(BigInteger.valueOf(2))
        .setName("maracas")
        .setStart(0.009)
        .setLength(0.21)
        .setPitch(1567.0)
        .setTempo(80.5)
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.get("instrumentId"));
    assertEquals("maracas", result.get("name"));
    assertNotNull(result.get("waveformKey"));
    assertTrue(result.get("waveformKey").toString().length() > 30);
    assertEquals(0.009, result.get("start"));
    assertEquals(0.21, result.get("length"));
    assertEquals(80.5, result.get("tempo"));
    assertEquals(1567.0, result.get("pitch"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioWrapper inputDataWrapper = new AudioWrapper()
      .setAudio(new Audio()
        .setName("maracas")
        .setWaveformKey("instrument/percussion/808/maracas.wav")
        .setStart(0.009)
        .setLength(0.21)
        .setPitch(1567.0)
        .setTempo(80.5)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void create_SucceedsWithoutWaveformKey() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioWrapper inputDataWrapper = new AudioWrapper()
      .setAudio(new Audio()
        .setInstrumentId(BigInteger.valueOf(2))
        .setName("maracas")
        .setStart(0.009)
        .setLength(0.21)
        .setPitch(1567.0)
        .setTempo(80.5)
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
    assertEquals(ULong.valueOf(1), result.get("instrumentId"));
    assertEquals("Snare", result.get("name"));
    assertEquals("instrument/percussion/808/snare.wav", result.get("waveformKey"));
    assertEquals(0.0023, result.get("start"));
    assertEquals(1.05, result.get("length"));
    assertEquals(131.0, result.get("tempo"));
    assertEquals(702.0, result.get("pitch"));
  }

  @Test
  public void uploadOne() throws Exception {
    System.setProperty("aws.file.upload.url","https://manuts.com");
    System.setProperty("aws.file.upload.key","totally_awesome");
    System.setProperty("aws.file.upload.secret","much_secret_12345");

    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject result = testDAO.uploadOne(access, ULong.valueOf(2));

    assertNotNull(result);
    assertEquals("https://manuts.com", result.get("uploadUrl"));
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
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("Kick", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("Snare", actualResult2.get("name"));
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
  public void update_FailsWithoutInstrumentID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioWrapper inputDataWrapper = new AudioWrapper()
      .setAudio(new Audio()
        .setName("maracas")
        .setWaveformKey("instrument/percussion/808/maracas.wav")
        .setStart(0.009)
        .setLength(0.21)
        .setPitch(1567.0)
        .setTempo(80.5)
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  // TODO: ensure that it is not possible to change the waveform key EVER!
  @Test(expected = BusinessException.class)
  public void update_FailsWithoutWaveformKey() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioWrapper inputDataWrapper = new AudioWrapper()
      .setAudio(new Audio()
        .setInstrumentId(BigInteger.valueOf(2))
        .setName("maracas")
        .setStart(0.009)
        .setLength(0.21)
        .setPitch(1567.0)
        .setTempo(80.5)
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentInstrument() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioWrapper inputDataWrapper = new AudioWrapper()
      .setAudio(new Audio()
        .setInstrumentId(BigInteger.valueOf(7))
        .setName("maracas")
        .setWaveformKey("instrument/percussion/808/maracas.wav")
        .setStart(0.009)
        .setLength(0.21)
        .setPitch(1567.0)
        .setTempo(80.5)
      );

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      AudioRecord result = IntegrationTestService.getDb()
        .selectFrom(AUDIO)
        .where(AUDIO.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("Snare", result.getName());
      assertEquals(ULong.valueOf(1), result.getInstrumentId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    AudioWrapper inputDataWrapper = new AudioWrapper()
      .setAudio(new Audio()
        .setInstrumentId(BigInteger.valueOf(2))
        .setName("maracas")
        .setStart(0.009)
        .setLength(0.21)
        .setPitch(1567.0)
        .setTempo(80.5)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    AudioRecord result = IntegrationTestService.getDb()
      .selectFrom(AUDIO)
      .where(AUDIO.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getInstrumentId());
    assertEquals("maracas", result.getName());
    assertEquals("instrument/percussion/808/kick1.wav", result.getWaveformKey());
    assertEquals(Double.valueOf(0.009), result.getStart());
    assertEquals(Double.valueOf(0.21), result.getLength());
    assertEquals(Double.valueOf(80.5), result.getTempo());
    assertEquals(Double.valueOf(1567.0), result.getPitch());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    AudioRecord result = IntegrationTestService.getDb()
      .selectFrom(AUDIO)
      .where(AUDIO.ID.eq(ULong.valueOf(1)))
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
    IntegrationTestEntity.insertAudioEvent(1, 1, 0.42, 0.41, "HEAVY", "C", 0.7, 0.98);

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      AudioRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(AUDIO)
        .where(AUDIO.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }

  // TODO [core] test AudioDAO cannot delete record unless user has account access

  // TODO test AudioDAO cannot write to WaveformKey value on create or update- ONLY updated by generating an upload policy

}
