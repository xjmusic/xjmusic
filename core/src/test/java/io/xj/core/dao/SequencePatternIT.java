//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.work.WorkManager;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or destroy patterns
@RunWith(MockitoJUnitRunner.class)
public class SequencePatternIT {
  @Spy
  private final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private SequencePatternDAO testDAO;

  /**
   assert result collection

   @param count  to assert
   @param result to make assertion about
   */
  private static void assertCollection(int count, Collection<SequencePattern> result) {
    assertNotNull(result);
    assertEquals(count, result.size());
  }

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Main, SequenceState.Published, "leaves", 0.342, "C#", 110.286);
    IntegrationTestEntity.insertSequence(2, 2, 1, SequenceType.Macro, SequenceState.Published, "coconuts", 8.02, "D", 130.2);

    // Sequence "leaves" has patterns "Ants" and "Jibbawhammers"
    IntegrationTestEntity.insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertSequencePattern(110, 1, 1, 0);
    IntegrationTestEntity.insertPattern(2, 1, PatternType.Main, PatternState.Published, 16, "Jibbawhammers", 0.583, "E major", 140.0);
    IntegrationTestEntity.insertSequencePattern(211, 1, 2, 1);

    // Instantiate the test subject
    testDAO = injector.getInstance(SequencePatternDAO.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(16L));

    SequencePattern result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getSequenceId());
    assertEquals(BigInteger.valueOf(1L), result.getPatternId());
    assertEquals(BigInteger.valueOf(16L), result.getOffset());
  }

  @Test
  public void create_FailsWithoutSequenceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(16L));

    failure.expect(CoreException.class);
    failure.expectMessage("Sequence ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setOffset(BigInteger.valueOf(16L));

    failure.expect(CoreException.class);
    failure.expectMessage("Pattern ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L));

    failure.expect(CoreException.class);
    failure.expectMessage("Offset is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    SequencePattern result = testDAO.readOne(access, BigInteger.valueOf(211));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
    assertEquals(BigInteger.valueOf(2L), result.getPatternId());
    assertEquals(BigInteger.valueOf(1L), result.getOffset());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(110));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<SequencePattern> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

  /**
   [#165803886] Segment memes expected to be taken directly from sequence_pattern binding
   */
  @Test
  public void readAllAtSequenceOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<SequencePattern> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(1L), BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(1L, result.size());
  }

  /**
   [#165803886] Segment memes expected to be taken directly from sequence_pattern binding
   */
  @Test
  public void readAllAtSequenceOffset_samePatternBoundMultipleTimes() throws Exception {
    Access access = Access.internal();
    IntegrationTestEntity.insertSequence(5, 2, 1, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertPattern(15, 5, PatternType.Main, PatternState.Published, 16, "Intro A", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertPatternChord(15, 0, "Gb minor");
    IntegrationTestEntity.insertPatternChord(15, 8, "G minor");
    IntegrationTestEntity.insertPattern(16, 5, PatternType.Main, PatternState.Published, 16, "Intro B", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternChord(16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(16, 8, "G major");
    IntegrationTestEntity.insertSequencePatternAndMeme(5, 15, 0, "Zero");
    IntegrationTestEntity.insertSequencePatternAndMeme(5, 15, 1, "One");
    IntegrationTestEntity.insertSequencePatternAndMeme(5, 15, 2, "Two");
    IntegrationTestEntity.insertSequencePatternAndMeme(5, 15, 3, "Three");
    IntegrationTestEntity.insertSequencePatternAndMeme(5, 16, 4, "Four");
    IntegrationTestEntity.insertSequencePatternAndMeme(5, 16, 5, "Five");
    IntegrationTestEntity.insertSequencePatternAndMeme(5, 16, 6, "Six");

    assertCollection(1, testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(0)));
    assertCollection(1, testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(1)));
    assertCollection(1, testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(2)));
    assertCollection(1, testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(3)));
    assertCollection(1, testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(4)));
    assertCollection(1, testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(5)));
    assertCollection(1, testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(6)));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<SequencePattern> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_Fails() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(16L));

    failure.expect(CoreException.class);
    failure.expectMessage("Not allowed to update SequencePattern record.");

    testDAO.update(access, BigInteger.valueOf(1L), inputData);
  }

  @Test
  public void destroy() throws Exception {
    IntegrationTestEntity.insertSequencePatternMeme(110, "Acorns");
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(110));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(110));
  }

}
