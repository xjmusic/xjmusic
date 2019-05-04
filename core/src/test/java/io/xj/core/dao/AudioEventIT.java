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
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.user_role.UserRoleType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or delete audio events
//
public class AudioEventIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AudioEventDAO testDAO;

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
    IntegrationTestEntity.insertInstrument(1, 1, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "909 Drums", InstrumentType.Percussive, 0.8);

    // Instrument "808" has Audio "Beat"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);

    // Audio "Drums" has events "KICK" and "SNARE" 2x each
    IntegrationTestEntity.insertAudioEvent(1, 2.5, 1.0, "KICK", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(1, 3.0, 1.0, "SNARE", "Ab", 0.1, 0.8);
    IntegrationTestEntity.insertAudioEvent(1, 0, 1.0, "KICK", "C", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(1, 1.0, 1.0, "SNARE", "G", 0.1, 0.8);

    // Instantiate the test subject
    testDAO = injector.getInstance(AudioEventDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.4)
      .setInflection("KICK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setAudioId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
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

  @Test(expected = CoreException.class)
  public void create_FailsWithoutNote() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AudioEvent result = testDAO.readOne(access, BigInteger.valueOf(1003L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1003L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
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
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1003L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<AudioEvent> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<AudioEvent> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }


  @Test
  public void readAllOfInstrument() throws Exception {
    IntegrationTestEntity.insertAudio(51, 1, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);
    IntegrationTestEntity.insertAudioEvent(51, 12.5, 1.0, "JAM", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(51, 14.0, 1.0, "PUMP", "Ab", 0.1, 0.8);
    IntegrationTestEntity.insertAudioEvent(51, 18, 1.0, "JAM", "C", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(51, 20.0, 1.0, "DUNK", "G", 0.1, 0.8);
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<AudioEvent> result = testDAO.readAllOfInstrument(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(8L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("JAM", it.next().getInflection());
    assertEquals("PUMP", it.next().getInflection());
    assertEquals("JAM", it.next().getInflection());
    assertEquals("DUNK", it.next().getInflection());
  }

  @Test
  public void readAllOfInstrument_SeesNothingOutsideOfLibrary() throws Exception {
    IntegrationTestEntity.insertAccount(6, "bananas");
    IntegrationTestEntity.insertLibrary(61, 6, "palm tree");
    IntegrationTestEntity.insertInstrument(61, 61, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrument(62, 61, 2, "909 Drums", InstrumentType.Percussive, 0.8);
    IntegrationTestEntity.insertAudio(61, 61, "Published", "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0);
    IntegrationTestEntity.insertAudioEvent(61, 2.5, 1.0, "ASS", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(61, 3.0, 1.0, "ASS", "Ab", 0.1, 0.8);
    IntegrationTestEntity.insertAudioEvent(61, 0, 1.0, "ASS", "C", 0.8, 1.0);
    IntegrationTestEntity.insertAudioEvent(61, 1.0, 1.0, "ASS", "G", 0.1, 0.8);
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<AudioEvent> result = testDAO.readAllOfInstrument(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<AudioEvent> it = result.iterator();
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
    assertEquals("KICK", it.next().getInflection());
    assertEquals("SNARE", it.next().getInflection());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutAudioID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.update(access, BigInteger.valueOf(1002L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutNote() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("KICK")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(1001L));

    testDAO.update(access, BigInteger.valueOf(1001L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentAudio() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.0)
      .setInflection("SNARE")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setAudioId(BigInteger.valueOf(287L));

    try {
      testDAO.update(access, BigInteger.valueOf(1002L), inputData);

    } catch (Exception e) {
      AudioEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1002L));
      assertNotNull(result);
      assertEquals("KICK", result.getInflection());
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
    AudioEvent inputData = new AudioEvent()
      .setDuration(1.2)
      .setInflection("POPPYCOCK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setAudioId(BigInteger.valueOf(1L));

    testDAO.update(access, BigInteger.valueOf(1000L), inputData);

    AudioEvent result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getInflection());
    assertEquals((Double) 1.2, result.getDuration());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(0.92, result.getTonality(), 0.01);
    assertEquals(0.72, result.getVelocity(), 0.01);
    assertEquals(BigInteger.valueOf(1L), result.getAudioId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

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
