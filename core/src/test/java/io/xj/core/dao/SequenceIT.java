// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.impl.SegmentContent;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.work.WorkManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

// future test: permissions of different users to readMany vs. create vs. update or destroy sequences
@RunWith(MockitoJUnitRunner.class)
public class SequenceIT {
  @Spy
  private final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private SequenceDAO testDAO;

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

    // Library "palm tree" has sequence "fonds" and sequence "nuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Main, SequenceState.Published, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertSequencePattern(101, 1, 1, 0);
    IntegrationTestEntity.insertSequenceMeme(1, "leafy");
    IntegrationTestEntity.insertSequenceMeme(1, "smooth");
    IntegrationTestEntity.insertSequence(2, 2, 1, SequenceType.Rhythm, SequenceState.Published, "nuts", 0.342, "C#", 0.286);

    // Library "boat" has sequence "helm" and sequence "sail"
    IntegrationTestEntity.insertLibrary(2, 1, "boat");
    IntegrationTestEntity.insertSequence(3, 3, 2, SequenceType.Macro, SequenceState.Published, "helm", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(3, 3, PatternType.Macro, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertSequencePattern(303, 3, 3, 0);
    IntegrationTestEntity.insertSequence(4, 2, 2, SequenceType.Detail, SequenceState.Published, "sail", 0.342, "C#", 0.286);

    // Instantiate the test subject
    testDAO = injector.getInstance(SequenceDAO.class);
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
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2L));

    Sequence result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(SequenceType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());
  }

  /**
   [#156144567] Artist expects to create a Main-type sequence without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2L));

    Sequence result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(SequenceType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutUserID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setLibraryId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons fifty nine");

    Sequence result = testDAO.clone(access, BigInteger.valueOf(1L), inputData);

    assertNotNull(result);
    assertEquals(0.342, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(0.286, result.getTempo(), 0.1);
    assertEquals(SequenceType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());

    // Verify enqueued audio clone jobs
    verify(workManager).doSequenceClone(eq(BigInteger.valueOf(1L)), any());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Sequence result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));

    Collection<Sequence> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Sequence> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_excludesSequencesInEraseState() throws Exception {
    IntegrationTestEntity.insertSequence(27, 2, 1, SequenceType.Main, SequenceState.Erase, "fonds", 0.342, "C#", 0.286);
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Sequence> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Sequence> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAllBoundToChain() throws Exception {
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertChainSequence(1, 1);

    Collection<Sequence> result = testDAO.readAllBoundToChain(Access.internal(), BigInteger.valueOf(1L));

    assertEquals(1L, result.size());
    Sequence result0 = result.iterator().next();
    assertEquals("fonds", result0.getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    Collection<Sequence> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setName("cannons");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setLibraryId(BigInteger.valueOf(3L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setName("cannons")
      .setLibraryId(BigInteger.valueOf(3L));

    try {
      testDAO.update(access, BigInteger.valueOf(3L), inputData);

    } catch (Exception e) {
      Sequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
      assertNotNull(result);
      assertEquals("helm", result.getName());
      assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Sequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Sequence or Instrument to always remain the same as when it was created, even after being updated by another user.
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2", // John will edit a sequence originally belonging to Jenny
      "roles", "Admin",
      "accounts", "1"
    ));
    Sequence inputData = new Sequence()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(BigInteger.valueOf(2L))
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(BigInteger.valueOf(3L));

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Sequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(3L), result.getUserId());
  }

  @Test
  public void destroy() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(2L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(2L));
  }

  /**
   [#154881808] Artist wants to destroy a Sequence along with any Patterns in it, in order to save time.
   */
  @Test
  public void destroy_SucceedsEvenIfSequenceHasMeme() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertSequenceMeme(2, "Blue");

    testDAO.destroy(access, BigInteger.valueOf(2L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(2L));
  }

  /**
   [#154881808] Artist wants to destroy a Sequence along with any Patterns in it, in order to save time.
   */
  @Test
  public void destroy_FailsIfSequenceHasPattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertPattern(120, 2, PatternType.Main, PatternState.Published, 16, "Block", 1.0, "C", 120);
    failure.expect(CoreException.class);
    failure.expectMessage("Pattern in Sequence");

    testDAO.destroy(access, BigInteger.valueOf(2L));
  }

  /**
   Theoretically, this case should never exist unless a pattern does also, but once an integration test accidentally created a SequencePattern binding a Sequence to some other Sequence's Pattern, and this catch and test is designed to mitigate that anyway.
   */
  @Test
  public void destroy_FailsIfSequenceHasSequencePattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertSequencePattern(120, 2, 1, 0);
    failure.expect(CoreException.class);
    failure.expectMessage("SequencePattern in Sequence");

    testDAO.destroy(access, BigInteger.valueOf(2L));
  }

  @Test
  public void destroy_succeedsAfterChosenForProduction() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    SegmentContent content1 = new SegmentContent();
    content1.getChoices().add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setSequencePatternId(BigInteger.valueOf(303))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(-5));
    IntegrationTestEntity.insertSegment_NoContent(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav");

    testDAO.destroy(access, BigInteger.valueOf(2L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(2L));
  }


  @Test
  public void erase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertSequence(1001, 2, 1, SequenceType.Main, SequenceState.Published, "fonds", 0.342, "C#", 0.286);

    testDAO.erase(access, BigInteger.valueOf(1001L));

    Sequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001L));
    assertNotNull(result);
    assertEquals(SequenceState.Erase, result.getState());
  }

  @Test
  public void erase_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("Sequence does not exist");

    testDAO.erase(access, BigInteger.valueOf(1L));
  }

  @Test
  public void erase_FailsIfSequenceHasMeme() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertPattern(1001, 1, PatternType.Main, PatternState.Published, 16, "Intro", 0.6, "C", 120.0);
    IntegrationTestEntity.insertSequencePattern(100110, 1, 1001, 0);

    failure.expect(CoreException.class);
    failure.expectMessage("Meme in Sequence");

    testDAO.erase(access, BigInteger.valueOf(1L));
  }

  @Test
  public void erase_SucceedsEvenWithChildren() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertSequence(1001, 2, 1, SequenceType.Main, SequenceState.Published, "fonds", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(1002, 1001, PatternType.Main, PatternState.Published, 16, "Intro", 0.6, "C", 120.0);
    IntegrationTestEntity.insertSequencePattern(100210010, 1001, 1002, 0);

    testDAO.erase(access, BigInteger.valueOf(1001L));

    Sequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001L));
    assertNotNull(result);
    assertEquals(SequenceState.Erase, result.getState());
  }
}
