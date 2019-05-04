// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.user_role.UserRoleType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or delete audio entities

// future test readAllSequences() which supports [#154234716] library ingest requires enumerating all possible sub-sequences of entities for any audio

public class AudioChordIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AudioChordDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");

    // Sequence "leaves" has instruments "808" and "909"
    IntegrationTestEntity.insertInstrument(1, 1, 2, "Harmonic Performance", InstrumentType.Percussive, 0.9);

    // Instrument "808" has Audio "Chords Cm to D"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Chords Cm to D", "instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440.0);

    // Audio "Drums" has events "C minor" and "D major" 2x each
    IntegrationTestEntity.insertAudioChord(1, 4, "D major");
    IntegrationTestEntity.insertAudioChord(1, 0, "C minor");

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
      .setPosition(4.0)
      .setName("G minor 7")
      .setAudioId(BigInteger.valueOf(1L));

    AudioChord result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(4.0, result.getPosition(), 0.01);
    assertEquals("G minor 7", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setAudioId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AudioChord result = testDAO.readOne(access, BigInteger.valueOf(1000L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1000L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<AudioChord> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<AudioChord> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setAudioId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioChord inputData = new AudioChord()
      .setPosition(4.0)
      .setAudioId(BigInteger.valueOf(57L))
      .setName("cannons");

    try {
      testDAO.update(access, BigInteger.valueOf(1001L), inputData);

    } catch (Exception e) {
      AudioChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001L));
      assertNotNull(result);
      assertEquals("C minor", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getAudioId());
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
      .setAudioId(BigInteger.valueOf(1L))
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, BigInteger.valueOf(1000L), inputData);

    AudioChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals(Double.valueOf(4.0), result.getPosition());
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
  }

  // future test: DAO cannot update audio chord to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1000L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1000L));
  }

}
