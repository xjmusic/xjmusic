// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.instrument.InstrumentType;
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

// future test: permissions of different users to readMany vs. create vs. update or delete audio chords

// future test readAllSequences() which supports [#154234716] library evaluation requires enumerating all possible sub-sequences of chords for any audio

public class AudioChordIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private AudioChordDAO testDAO;

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

    // Pattern "leaves" has instruments "808" and "909"
    IntegrationTestEntity.insertInstrument(1, 1, 2, "Harmonic Performance", InstrumentType.Percussive, 0.9);

    // Instrument "808" has Audio "Chords Cm to D"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Chords Cm to D", "https://static.xj.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);

    // Audio "Drums" has events "C minor" and "D major" 2x each
    IntegrationTestEntity.insertAudioChord(1, 1, 4, "D major");
    IntegrationTestEntity.insertAudioChord(2, 1, 0, "C minor");

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioChordDAO.class);
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
    AudioChord inputData = new AudioChord()
      .setPosition(4)
      .setName("G minor 7")
      .setAudioId(BigInteger.valueOf(1));

    AudioChord result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(4, result.getPosition(), 0.01);
    assertEquals("G minor 7", result.getName());
    assertEquals(BigInteger.valueOf(1), result.getAudioId());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4)
      .setAudioId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AudioChord result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getAudioId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    AudioChord result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(2, result.length());
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

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4)
      .setAudioId(BigInteger.valueOf(2));

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4)
      .setAudioId(BigInteger.valueOf(57))
      .setName("cannons");

    try {
      testDAO.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      AudioChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals("C minor", result.getName());
      assertEquals(BigInteger.valueOf(1), result.getAudioId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setAudioId(BigInteger.valueOf(1))
      .setName("POPPYCOCK")
      .setPosition(4);

    testDAO.update(access, BigInteger.valueOf(1), inputData);

    AudioChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals((Integer) 4, result.getPosition());
    assertEquals(BigInteger.valueOf(1), result.getAudioId());
  }

  // future test: DAO cannot update audio chord to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    AudioChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

}
