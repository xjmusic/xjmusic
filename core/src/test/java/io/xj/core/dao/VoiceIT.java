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
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.voice.Voice;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or delete sequences
public class VoiceIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private VoiceDAO testDAO;

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
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Main, SequenceState.Published, "leaves", 0.342, "C#", 110.286);

    // Sequence "leaves" has patterns "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertSequencePattern(110, 1, 1, 0);
    IntegrationTestEntity.insertPattern(2, 1, PatternType.Main, PatternState.Published, 16, "Caterpillars", 0.583, "E major", 140.0);
    IntegrationTestEntity.insertSequencePattern(211, 1, 2, 1);

    // Pattern "Ants" has Voices "Head" and "Body"
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertVoice(2, 1, InstrumentType.Melodic, "This is melodious");
    IntegrationTestEntity.insertVoice(3, 1, InstrumentType.Harmonic, "This is harmonious");
    IntegrationTestEntity.insertVoice(4, 1, InstrumentType.Vocal, "This is a vocal voice");

    // Instantiate the test subject
    testDAO = injector.getInstance(VoiceDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setSequenceId(BigInteger.valueOf(1L))
      .setType("Harmonic")
      .setDescription("This is harmonious");

    Voice result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(InstrumentType.Harmonic, result.getType());
    assertEquals("This is harmonious", result.getDescription());
    assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutSequenceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setType("Harmonic")
      .setDescription("This is harmonious");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setSequenceId(BigInteger.valueOf(2L))
      .setDescription("This is harmonious");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Voice result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
    assertEquals(InstrumentType.Melodic, result.getType());
    assertEquals("This is melodious", result.getDescription());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<Voice> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<Voice> resultIt = result.iterator();
    assertEquals("This is a percussive voice", resultIt.next().getDescription());
    assertEquals("This is melodious", resultIt.next().getDescription());
    assertEquals("This is harmonious", resultIt.next().getDescription());
    assertEquals("This is a vocal voice", resultIt.next().getDescription());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<Voice> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setType("Harmonic")
      .setDescription("This is harmonious");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setSequenceId(BigInteger.valueOf(2L))
      .setDescription("This is harmonious");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setSequenceId(BigInteger.valueOf(7L))
      .setType("Melodic")
      .setDescription("This is melodious");

    try {
      testDAO.update(access, BigInteger.valueOf(3L), inputData);

    } catch (Exception e) {
      Voice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
      assertNotNull(result);
      assertEquals(InstrumentType.Harmonic, result.getType());
      assertEquals("This is harmonious", result.getDescription());
      assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setSequenceId(BigInteger.valueOf(1L))
      .setType("Melodic")
      .setDescription("This is melodious; Yoza!");

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    Voice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals("This is melodious; Yoza!", result.getDescription());
    assertEquals(InstrumentType.Melodic, result.getType());
    assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

  @Test
  public void delete_SuccessEvenIfSequenceHasChildren() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertPatternEvent(1, 1, 0.42, 0.41, "HEAVY", "C", 0.7, 0.98);
    IntegrationTestEntity.insertPatternEvent(1, 1, 2.42, 0.41, "HEAVY", "C", 0.7, 0.98);
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);

    testDAO.destroy(access, BigInteger.valueOf(1L));

    // Assert total annihilation
    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(1L));
    IntegrationTestEntity.assertNotExist(injector.getInstance(PatternEventDAO.class), BigInteger.valueOf(21L));
    IntegrationTestEntity.assertNotExist(injector.getInstance(PatternEventDAO.class), BigInteger.valueOf(21L));
  }

  // future test: VoiceDAO cannot delete record unless user has account access

}
