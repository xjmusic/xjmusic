// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.payload.PayloadObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.lib.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProgramSequenceTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequence subject;

  @Before
  public void setUp() {
    subject = new ProgramSequence()
    ;
  }

  @Test
  public void validate() throws Exception {
    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    subject
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setKey("D# major 7")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutKey() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Key is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutDensity() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Density is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_totalDefaultsToZero() throws Exception {
    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setTempo(100.0)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .validate();

    assertEquals(Integer.valueOf(0), subject.getTotal());
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Tempo is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name", "key", "density", "total", "tempo"), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    UUID id = UUID.randomUUID();
    PayloadObject obj = new PayloadObject()
      .setId(id.toString())
      .setType("program-sequences")
      .setAttributes(ImmutableMap.of(
        "name", "Test Sequence",
        "key", "D minor",
        "total", 32,
        "density", 0.6d,
        "tempo", 120.0d
      ));

    subject.consume(obj);

    assertEquals(id, subject.getId());
    assertEquals("Test Sequence", subject.getName());
    assertEquals("D minor", subject.getKey());
    assertEquals(Integer.valueOf(32), subject.getTotal());
    assertEquals(Double.valueOf(0.6), subject.getDensity());
    assertEquals(Double.valueOf(120), subject.getTempo());
  }

  @Test
  public void setAllFrom_noId() throws CoreException {
    PayloadObject obj = new PayloadObject()
      .setType("program-sequences")
      .setAttributes(ImmutableMap.of(
        "name", "Test Sequence",
        "key", "D minor",
        "total", 32,
        "density", 0.6d,
        "tempo", 120.0d
      ));

    subject.consume(obj);

    assertNull(subject.getId());
    assertEquals("Test Sequence", subject.getName());
    assertEquals("D minor", subject.getKey());
    assertEquals(Integer.valueOf(32), subject.getTotal());
    assertEquals(Double.valueOf(0.6), subject.getDensity());
    assertEquals(Double.valueOf(120), subject.getTempo());
  }

  /*

  FUTURE Implement these Sequence unit tests (adapted of legacy integration tests)

  @Before
  public void setUp() throws Exception {
    reset();

    // inject mocks
    createInjector();

    // Account "bananas"
    insert(of(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(of(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(of(2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    insert(of(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    insert(of(3, UserRoleType.User);
    insert(of(1, 3);

    // Library "palm tree" has sequence "fonds" and sequence "nuts"
    insert(of(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "fonds", 0.342, "C#", 0.286);
    insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    insertSequenceBinding(101, 1, 1, 0);
    insertProgramMeme(1, "chunk");
    insertProgramMeme(1, "smooth");
    insertSequence(2, 2, 1, ProgramType.Rhythm, ProgramState.Published, "nuts", 0.342, "C#", 0.286);

    // Library "boat" has sequence "helm" and sequence "sail"
    insert(of(2, 1, "boat",now()));
    insertSequence(3, 3, 2, ProgramType.Macro, ProgramState.Published, "helm", 0.342, "C#", 0.286);
    insertPattern(3, 3, PatternType.Macro, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    insertSequenceBinding(303, 3, 3, 0);
    insertSequence(4, 2, 2, ProgramType.Detail, ProgramState.Published, "sail", 0.342, "C#", 0.286);

    // Instantiate the test subject
    testDAO = injector.getInstance(SequenceDAO.class);
  }

  private void ofInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
  }

  @Test
  public void of() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(UUID.randomUUID())
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(UUID.randomUUID());

    Sequence result = testDAO.of(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());
  }

  /**
   [#156144567] Artist expects to of a Main-type sequence without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   *
  @Test
  public void of_asArtist() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "User,Artist");
    Sequence inputData = subject
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(UUID.randomUUID())
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(UUID.randomUUID());

    Sequence result = testDAO.of(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutLibraryID() throws Exception {
    Access access = of(ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutUserID() throws Exception {
    Access access = of(ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setLibraryId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setLibraryId(UUID.randomUUID())
      .setName("cannons fifty nine");

    Sequence result = testDAO.clone(access, BigInteger.valueOf(1L), inputData);

    assertNotNull(result);
    assertEquals(0.342, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(0.286, result.getTempo(), 0.1);
    assertEquals(ProgramType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getUserId());

    // Verify enqueued audio clone jobs
    verify(workManager).doProgramClone(eq(BigInteger.valueOf(1L)), any());
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "User");

    Sequence result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Admin");

    Collection<Sequence> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Sequence> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_excludesSequencesInEraseState() throws Exception {
    insertSequence(27, 2, 1, ProgramType.Main, ProgramState.Erase, "fonds", 0.342, "C#", 0.286);
    Access access = of(ImmutableList.of(account1), "User");

    Collection<Sequence> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Sequence> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "User");

    Collection<Sequence> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    Access access = of(ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setName("cannons");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = of(ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setLibraryId(UUID.randomUUID());

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = of(ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setName("cannons")
      .setLibraryId(UUID.randomUUID());

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
    Access access = of(user2, ImmutableList.of(account1), "User");
    Sequence inputData = subject
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(UUID.randomUUID())
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(UUID.randomUUID());

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Sequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(BigInteger.valueOf(2L), result.getLibraryId());
  }

  /**
   [#156030760] Artist expects owner of Sequence or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   *
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2", // John will edit a sequence originally belonging to Jenny
      "roles", "Admin",
      "accounts", "1"
    ));
    Sequence inputData = subject
      .setDensity(0.42)
      .setKey("G minor 7")
      .setLibraryId(UUID.randomUUID())
      .setName("cannons")
      .setTempo(129.4)
      .setType("Main")
      .setUserId(UUID.randomUUID());

    testDAO.update(access, BigInteger.valueOf(3L), inputData);

    Sequence result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(3L), result.getUserId());
  }

  @Test
  public void destroy() throws Exception {
    Access access = of("Admin");

    testDAO.destroy(access, BigInteger.valueOf(2L));

    assertNotExist(testDAO, BigInteger.valueOf(2L));
  }

  /**
   [#154881808] Artist wants to destroy a Sequence along with any Patterns in it, in order to save time.
   *
  @Test
  public void destroy_SucceedsEvenIfSequenceHasMeme() throws Exception {
    Access access = of("Admin");
    insertProgramMeme(2, "Blue");

    testDAO.destroy(access, BigInteger.valueOf(2L));

    assertNotExist(testDAO, BigInteger.valueOf(2L));
  }

  /**
   [#154881808] Artist wants to destroy a Sequence along with any Patterns in it, in order to save time.
   *
  @Test
  public void destroy_FailsIfSequenceHasPattern() throws Exception {
    Access access = of("Admin");
    insertPattern(120, 2, PatternType.Main, PatternState.Published, 16, "Block", 1.0, "C", 120);
    failure.expect(CoreException.class);
    failure.expectMessage("Pattern in Sequence");

    testDAO.destroy(access, BigInteger.valueOf(2L));
  }

  /**
   Theoretically, this case should never exist unless a pattern does also, but once an integration test accidentally createda SequenceBinding binding a Sequence to some other Sequence's Pattern, and this catch and test is designed to mitigate that anyway.
   *
  @Test
  public void destroy_FailsIfSequenceHasSequenceBinding() throws Exception {
    Access access = of("Admin");
    insertSequenceBinding(120, 2, 1, 0);
    failure.expect(CoreException.class);
    failure.expectMessage("SequenceBinding in Sequence");

    testDAO.destroy(access, BigInteger.valueOf(2L));
  }

  @Test
  public void destroy_succeedsAfterChosenForProduction() throws Exception {
    Access access = of("Admin");
    insert(of(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null);
    SegmentContent content1 = new SegmentContent();
    content1.getChoices().add(new Choice()
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(-5));
    insert(segmentFactory.of(BigInteger.valueOf(1))
      .setChainId(UUID.randomUUID())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0.0)
.setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:01:00.000001Z")
      .setUpdatedAt("2017-02-14T12:01:32.000001Z"))

    testDAO.destroy(access, BigInteger.valueOf(2L));

    assertNotExist(testDAO, BigInteger.valueOf(2L));
  }

   */
}
