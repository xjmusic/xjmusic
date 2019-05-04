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
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or delete pattern entities

// future test readAllSequences() which supports [#154234716] library ingest requires enumerating all possible sub-sequences of entities for any pattern

public class PatternChordIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private PatternChordDAO testDAO;

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

    // Pattern "Caterpillars" has entities "C minor" and "D major"
    IntegrationTestEntity.insertPatternChord(2, 0, "C minor");
    IntegrationTestEntity.insertPatternChord(2, 4, "D major");

    // Instantiate the test subject
    testDAO = injector.getInstance(PatternChordDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setName("G minor 7")
      .setPatternId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
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
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setPatternId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    PatternChord result = testDAO.readOne(access, BigInteger.valueOf(2001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getPatternId());
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

    testDAO.readOne(access, BigInteger.valueOf(2001L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<PatternChord> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<PatternChord> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(2002L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setPatternId(BigInteger.valueOf(2001L));

    testDAO.update(access, BigInteger.valueOf(2001L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPosition(4.0)
      .setPatternId(BigInteger.valueOf(57L))
      .setName("D minor");

    try {
      testDAO.update(access, BigInteger.valueOf(2001L), inputData);

    } catch (Exception e) {
      PatternChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2001L));
      assertNotNull(result);
      assertEquals("D major", result.getName());
      assertEquals(BigInteger.valueOf(2L), result.getPatternId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    PatternChord inputData = new PatternChord()
      .setPatternId(BigInteger.valueOf(1L))
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, BigInteger.valueOf(2000L), inputData);

    PatternChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals(Double.valueOf(4.0), result.getPosition());
    assertEquals(BigInteger.valueOf(1L), result.getPatternId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(2000L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(2000L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    testDAO.destroy(access, BigInteger.valueOf(2000L));
  }

}
