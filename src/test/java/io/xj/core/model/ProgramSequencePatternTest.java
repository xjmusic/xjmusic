// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;

public class ProgramSequencePatternTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setTotal(64)
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Voice ID is required");

    new ProgramSequencePattern()
      .setName("Test54")
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Sequence ID is required");

    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setTotal(64)
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Type is required");

    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setProgramVoiceId(UUID.randomUUID())
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_failsWithoutTotal() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Total is required");

    new ProgramSequencePattern()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setProgramVoiceId(UUID.randomUUID())
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new ProgramSequencePattern()
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }

  @Test
  public void validate_failsWithoutProgramId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new ProgramSequencePattern()
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .setTypeEnum(ProgramSequencePatternType.Loop)
      .setTotal(64)
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type", "total", "name"), new ProgramSequencePattern().getResourceAttributeNames());
  }

  /*
  // FUTURE implement these Pattern unit tests (adapted of legacy integration tests)


  @Before
  public void setUp() throws Exception {
    reset();

    // inject mocks
    createInjector();

    // Account "oranges"
    insert(of(1, "oranges");

    // John has "user" and "admin" roles, belongs to account "oranges", has "google" auth
    insert(of(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(of(2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "oranges"
    insert(of(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    insert(of(3, UserRoleType.User);
    insert(of(1, 3);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(of(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "leaves", 0.342, "C#", 110.286);
    insertSequence(2, 2, 1, ProgramType.Macro, ProgramState.Published, "coconuts", 8.02, "D", 130.2);

    // Sequence "leaves" has patterns "Ants" and "Caterpillars"
    Program program1 = of();
    Pattern pattern1 = program1.add(new Pattern()
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setTotal(16)
      .setName("Ants")
      .setDensity(0.503)
      .setKey("D minor")
      .setTempo(120.0));
    SequenceBinding bind_program1_sequence1 = program1.add(new SequenceBinding()
      .setProgramSequenceId(1)
      .setOffset(0));
    insertSequenceBinding(110, 1, 1, 0);
    insert(programFactory.of(BigInteger.valueOf(2))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Caterpillars")
      .setDensity(0.503)
      .setKey("E major")
      .setTempo(140.0));
    insertSequenceBinding(211, 1, 2, 1);

    // Instantiate the test subject
    testDAO = injector.getInstance(PatternDAO.class);
  }

  private void ofInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
    programFactory = injector.getInstance(ProgramFactory.class);
  }

  @Test
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Macro)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    Pattern result = testDAO.of(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getProgramSequenceId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.01);
    assertEquals(Integer.valueOf(16), result.getTotal());
  }


   // [#159669804] Artist wants a step sequencer in order to compose rhythm patterns in a familiar way.
  @Test
  public void of_meterDefault() throws Exception {
    insertSequence(27, 2, 1, ProgramType.Rhythm, ProgramState.Published, "beets", 0.342, "D minor", 120);
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Loop)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    Pattern result = testDAO.of(access, inputData);

    assertNotNull(result);
    assertEquals(Integer.valueOf(4), result.getMeterSuper());
    assertEquals(Integer.valueOf(4), result.getMeterSub());
    assertEquals(Integer.valueOf(0), result.getMeterSwing());
  }


   // [#153976073] Artist wants Macro-type Sequence to have Macro-type Pattern
  @Test
  public void of_failsWithWrongTypeForMacroSequence() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Loop)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(CoreException.class);
    failure.expectMessage("Macro-type Pattern in Macro-type Sequence is required");

    testDAO.of(access, inputData);
  }


   // [#153976073] Artist wants Main-type Sequence to have Main-type Pattern
  @Test
  public void of_failsWithWrongTypeForMainSequence() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Loop)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(CoreException.class);
    failure.expectMessage("Main-type Pattern in Main-type Sequence is required");

    testDAO.of(access, inputData);
  }


   // [#153976073] Artist wants Rhythm-type Sequence to have Intro-, Loop-, or Outro- type Pattern
  @Test
  public void of_failsWithWrongTypeForRhythmSequence() throws Exception {
    insertSequence(51, 2, 1, ProgramType.Rhythm, ProgramState.Published, "tester-b", 0.342, "C#", 110.286);
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(CoreException.class);
    failure.expectMessage("Pattern of type (Intro,Loop,Outro) in Rhythm-type Sequence is required");

    testDAO.of(access, inputData);
  }


   // [#153976073] Artist wants Detail-type Sequence to have Intro-, Loop-, or Outro- type Pattern
  @Test
  public void of_failsWithWrongTypeForDetailSequence() throws Exception {
    insertSequence(51, 2, 1, ProgramType.Detail, ProgramState.Published, "tester-b", 0.342, "C#", 110.286);
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(CoreException.class);
    failure.expectMessage("Pattern of type (Intro,Loop,Outro) in Detail-type Sequence is required");

    testDAO.of(access, inputData);
  }

  @Test
  public void of_TotalNotRequiredForMacroSequenceBinding() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Macro)
      .setName("cannons")
      .setTempo(129.4);

    Pattern result = testDAO.of(access, inputData);

    assertNotNull(result);
    assertNull(result.getTotal());
  }


   // [#150279647] Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
   // Reverts legacy [Trello#237] shouldn't be able to of pattern with same offset in sequence

  @Test
  public void of_MultiplePatternsAtSameOffset() throws Exception {
    Access access = of("Admin");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    Pattern result = testDAO.of(access, inputData);
    assertNotNull(result);
  }

  @Test
  public void of_TotalIsRequiredForNonMacroTypeSequenceBinding() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setName("cannons")
      .setTempo(129.4);

    failure.expect(CoreException.class);
    failure.expectMessage("for a pattern of a non-macro-type sequence, total (# beats) must be greater than zero");

    testDAO.of(access, inputData);
  }

  @Test
  public void of_TotalMustBeGreaterThanZeroForNonMacroTypeSequenceBinding() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(0);

    failure.expect(CoreException.class);
    failure.expectMessage("for a pattern of a non-macro-type sequence, total (# beats) must be greater than zero");

    testDAO.of(access, inputData);
  }

  @Test
  public void of_NullOptionalFieldsAllowed() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(null)
      .setKey(null)
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Macro)
      .setName(null)
      .setTempo(null)
      .setTotal(16);

    Pattern result = testDAO.of(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getProgramSequenceId());
    assertNull(result.getDensity());
    assertNull(result.getKey());
    assertNull(result.getName());
    assertNull(result.getTempo());
    assertEquals(Integer.valueOf(16), result.getTotal());
  }

  @Test
  public void of_FailsWithoutSequenceID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setTypeEnum(PatternType.Macro)
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(CoreException.class);
    failure.expectMessage("Sequence ID is required");

    testDAO.of(access, inputData);
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "User");
    Pattern inputData = programFactory.of()
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Macro)
      .setName("cannons fifty nine");

    Pattern result = testDAO.clone(access, BigInteger.valueOf(1L), inputData);

    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("D minor", result.getKey());
    assertEquals(BigInteger.valueOf(2L), result.getProgramSequenceId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120.0, result.getTempo(), 0.1);

    // Verify enqueued audio clone jobs
    verify(workManager).doPatternClone(eq(BigInteger.valueOf(1L)), any());
  }


 //  [#150279647] Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.

  @Test
  public void clone_fromOriginal_toOffsetOfExistingPattern() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "User");
    Pattern inputData = programFactory.of()
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setName("cannons fifty nine");

    Pattern result = testDAO.clone(access, BigInteger.valueOf(1L), inputData);

    assertNotNull(result);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Pattern result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
    assertEquals("Caterpillars", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readAllAtSequenceOffset_Zero() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<Pattern> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(1L), BigInteger.valueOf(0L));

    assertNotNull(result);
    assertEquals(1L, result.size());
    Pattern resultOne = result.iterator().next();
    assertEquals("Ants", resultOne.getName());
  }

  @Test
  public void readAllAtSequenceOffset_One() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<Pattern> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(1L), BigInteger.valueOf(1L));

    assertNotNull(result);
    assertEquals(1L, result.size());
    Pattern resultOne = result.iterator().next();
    assertEquals("Caterpillars", resultOne.getName());
  }


  // [#161076729] Artist wants detail and rhythm patterns to require no sequence-pattern bindings, to keep things simple
  @Test
  public void readAllAtSequenceOffset_RhythmPatternsHaveNoSequenceBindingBinding() throws Exception {
    insertSequence(5, 2, 1, ProgramType.Rhythm, ProgramState.Published, "b-a-N-A-N-a-s", 8.02, "D", 130.2);
    insert(programFactory.of(BigInteger.valueOf(12))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Loop)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Antelope")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0));
    insert(programFactory.of(BigInteger.valueOf(14))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Intro)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Bear")
      .setDensity(0.583)
      .setKey("E major")
      .setTempo(140.0));
    insert(programFactory.of(program15.getId())
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Outro)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Cat")
      .setDensity(0.583)
      .setKey("E major")
      .setTempo(140.0));
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<Pattern> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(69L));

    assertNotNull(result);
    assertEquals(3L, result.size());
    Iterator<Pattern> it = result.iterator();
    assertEquals("Cat", it.next().getName());
    assertEquals("Bear", it.next().getName());
    assertEquals("Antelope", it.next().getName());
  }

  // [#161076729] Artist wants detail and rhythm patterns to require no sequence-pattern bindings, to keep things simple
  @Test
  public void readAllAtSequenceOffset_DetailPatternsHaveNoSequenceBindingBinding() throws Exception {
    insertSequence(5, 2, 1, ProgramType.Detail, ProgramState.Published, "b-a-N-A-N-a-s", 8.02, "D", 130.2);
    insert(programFactory.of(BigInteger.valueOf(12))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Loop)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Antelope")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0));
    insert(programFactory.of(BigInteger.valueOf(14))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Intro)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Bear")
      .setDensity(0.583)
      .setKey("E major")
      .setTempo(140.0));
    insert(programFactory.of(program15.getId())
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Outro)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Cat")
      .setDensity(0.583)
      .setKey("E major")
      .setTempo(140.0));
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<Pattern> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(5L), BigInteger.valueOf(69L));

    assertNotNull(result);
    assertEquals(3L, result.size());
    Iterator<Pattern> it = result.iterator();
    assertEquals("Cat", it.next().getName());
    assertEquals("Bear", it.next().getName());
    assertEquals("Antelope", it.next().getName());
  }

   // [#150279647] Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
  @Test
  public void readAllAtSequenceOffset_multiplePatternsAtOffset() throws Exception {
    insert(programFactory.of(BigInteger.valueOf(5))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Army Ants")
      .setDensity(0.683)
      .setKey("Eb minor")
      .setTempo(122.4));
    insertSequenceBinding(510, 1, 5, 0);
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<Pattern> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(1L), BigInteger.valueOf(0L));

    assertNotNull(result);
    assertEquals(2L, result.size());
    Iterator<Pattern> it = result.iterator();
    assertEquals("Army Ants", it.next().getName());
    assertEquals("Ants", it.next().getName());
  }


  @Test
  public void readAllAtSequenceOffset_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "143"
    ));

    Collection<Pattern> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(1L), BigInteger.valueOf(1L));

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<Pattern> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Pattern> resultIt = result.iterator();
    assertEquals("Ants", resultIt.next().getName());
    assertEquals("Caterpillars", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<Pattern> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void readAll_excludesPatternsInEraseState() throws Exception {
    insert(programFactory.of(BigInteger.valueOf(27))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Erase)
      .setTotal(16)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0));
    insertSequenceBinding(2710, 1, 27, 0);
    Access access = of(ImmutableList.of(account1), "User");

    Collection<Pattern> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<Pattern> resultIt = result.iterator();
    assertEquals("Ants", resultIt.next().getName());
    assertEquals("Caterpillars", resultIt.next().getName());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void update() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setTotal(32)
      .setName(null)
      .setDensity(null)
      .setKey("")
      .setTempo((double) 0);

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertNull(result.getName());
    assertNull(result.getDensity());
    assertNull(result.getTempo());
    assertNull(result.getKey());
    assertEquals(Integer.valueOf(32), result.getTotal());
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
  }

   // [#159669804] Artist wants a step sequencer in order to compose rhythm patterns in a familiar way.
  @Test
  public void update_meter() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setTotal(32)
      .setName(null)
      .setDensity(null)
      .setKey("")
      .setTempo((double) 0)
      .setMeterSuper(48)
      .setMeterSub(16)
      .setMeterSwing(35);

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals(Integer.valueOf(48), result.getMeterSuper());
    assertEquals(Integer.valueOf(16), result.getMeterSub());
    assertEquals(Integer.valueOf(35), result.getMeterSwing());
  }

   // [#150279647] Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
  @Test
  public void update_toOffsetOfExistingPattern() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setTotal(16)
      .setName("Caterpillars")
      .setDensity(0.583)
      .setKey("E major")
      .setTempo(140.0);

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
  }

  @Test
  public void update_FailsWithoutSequenceID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setTypeEnum(PatternType.Macro)
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(CoreException.class);
    failure.expectMessage("Sequence ID is required");

    testDAO.update(access, BigInteger.valueOf(1L), inputData);
  }

  @Test
  public void update_TotalNotRequiredForMacroSequenceBinding() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setTypeEnum(PatternType.Macro)
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Macro)
      .setName("cannons")
      .setTempo(129.4);

    testDAO.update(access, BigInteger.valueOf(1L), inputData);
  }

  @Test
  public void update_TotalIsRequiredForNonMacroTypeSequenceBinding() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setName("cannons")
      .setTempo(129.4);

    failure.expect(CoreException.class);
    failure.expectMessage("for a pattern of a non-macro-type sequence, total (# beats) must be greater than zero");

    testDAO.update(access, BigInteger.valueOf(1L), inputData);
  }

  @Test
  public void update_TotalMustBeGreaterThanZeroForNonMacroTypeSequenceBinding() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(0);

    failure.expect(CoreException.class);
    failure.expectMessage("for a pattern of a non-macro-type sequence, total (# beats) must be greater than zero");

    testDAO.update(access, BigInteger.valueOf(1L), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentSequence() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Pattern inputData = programFactory.of()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Macro)
      .setName("Smash!")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(CoreException.class);
    failure.expectMessage("Sequence does not exist");

    try {
      testDAO.update(access, BigInteger.valueOf(2L), inputData);

    } catch (Exception e) {
      Pattern result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals("Caterpillars", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(1L));

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test(expected = CoreException.class)
  public void destroy_failsIfNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

  @Test
  public void destroy_SucceedsEvenIfSequenceHasManyChildren() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "Artist");
    insertSequenceBinding(1901, 1, 1, 0, Instant.parse("2017-02-14T12:01:00.000001Z"));
    insertSequenceBindingMeme(1901, "mashup");
    insertVoice(2051, 1, InstrumentType.Percussive, "Smash");
    insertVoice(2052, 1, InstrumentType.Percussive, "Boom");

    Pattern p3 = programFactory.of(BigInteger.valueOf(3))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Ants")
      .setDensity(0.503)
      .setKey("D minor")
      .setTempo(120.0);
    p3.add(new Event()
      .setProgramVoiceId(UUID.randomUUID())
      .setPosition(0.0)
      .setDuration(1.0)
      .setName("KICK")
      .setNote("C")
      .setTonality(0.8)
      .setVelocity(1.0));
    p3.add(new Event()
      .setProgramVoiceId(UUID.randomUUID())
      .setPosition(1.0)
      .setDuration(1.0)
      .setName("KICK")
      .setNote("C")
      .setTonality(0.8)
      .setVelocity(1.0));
    p3.add(new SequenceChord()
      .setPosition(0.0)
      .setName("C"));
    insert(p3);
    insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);

    testDAO.destroy(access, BigInteger.valueOf(3L));

    // Assert total annihilation
    assertNotExist(testDAO, BigInteger.valueOf(1L));
    assertNotExist(injector.getInstance(SequenceBindingMemeDAO.class), BigInteger.valueOf(2001L));
  }

  // future test: PatternDAO cannot destroy record unless user has account access

    @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(of(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(of(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(of(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(of(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "leaves", 0.342, "C#", 110.286);

    // Sequence "leaves" has voices "Intro" and "Outro"
    insertPattern(1, 1, PatternType.Main, PatternState.Published, 4, "Intro", 0.583, "D minor", 120.0);
    insertSequenceBinding(110, 1, 1, 0);
    insertPattern(2, 1, PatternType.Main, PatternState.Published, 4, "Outro", 0.583, "E major", 140.0);
    insertSequenceBinding(211, 1, 2, 1);

    // Voice "Caterpillars" has voices "Drums" and "Bass"
    insertVoice(1, 1, InstrumentType.Percussive, "Drums");
    insertVoice(2, 1, InstrumentType.Harmonic, "Bass");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    insertPatternEvent(1, 1, 0, 1.0, "BOOM", "C", 0.8, 1.0);
    insertPatternEvent(1, 1, 1.0, 1.0, "SMACK", "G", 0.1, 0.8);
    insertPatternEvent(1, 1, 2.5, 1.0, "BOOM", "C", 0.8, 0.6);
    insertPatternEvent(1, 1, 3.0, 1.0, "SMACK", "G", 0.1, 0.9);

    // Instantiate the test subject
    testDAO = injector.getInstance(PatternEventDAO.class);
  }


   @Test
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.4)
      .setName("BOOM")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutVoiceID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.0)
      .setName("BOOM")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0);

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutNote() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.0)
      .setName("BOOM")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setProgramVoiceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Event result = testDAO.readOne(access, BigInteger.valueOf(1001001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequencePatternId());
    assertEquals(BigInteger.valueOf(1L), result.getProgramVoiceId());
    assertEquals(Double.valueOf(1.0), result.getDuration());
    assertEquals("SMACK", result.getName());
    assertEquals("G", result.getNote());
    assertEquals(Double.valueOf(1.0), result.getPosition());
    assertEquals(Double.valueOf(0.1), result.getTonality());
    assertEquals(Double.valueOf(0.8), result.getVelocity());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<Event> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(4L, result.size());
    Iterator<Event> resultIt = result.iterator();
    assertEquals("BOOM", resultIt.next().getName());
    assertEquals("SMACK", resultIt.next().getName());
    assertEquals("BOOM", resultIt.next().getName());
    assertEquals("SMACK", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<Event> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutVoiceID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.0)
      .setName("BOOM")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setProgramSequencePatternId(UUID.randomUUID())
      .setVelocity(1.0);

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.0)
      .setName("BOOM")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setProgramVoiceId(UUID.randomUUID())
      .setVelocity(1.0);

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutNote() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.0)
      .setName("BOOM")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setProgramVoiceId(UUID.randomUUID());

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentVoice() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.0)
      .setName("SMACK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID());

    try {
      testDAO.update(access, BigInteger.valueOf(1001002L), inputData);

    } catch (Exception e) {
      Event result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001002L));
      assertNotNull(result);
      assertEquals("BOOM", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getProgramVoiceId());
      throw e;
    }
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.0)
      .setName("SMACK")
      .setNote("C")
      .setPosition(0.0)
      .setTonality(1.0)
      .setVelocity(1.0)
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID());

    try {
      testDAO.update(access, BigInteger.valueOf(1001002L), inputData);

    } catch (Exception e) {
      Event result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001002L));
      assertNotNull(result);
      assertEquals("BOOM", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getProgramSequencePatternId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Event inputData = new Event()
      .setDuration(1.2)
      .setName("POPPYCOCK")
      .setNote("C")
      .setPosition(0.42)
      .setTonality(0.92)
      .setVelocity(0.72)
      .setProgramSequencePatternId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID());

    testDAO.update(access, BigInteger.valueOf(1001000L), inputData);

    Event result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1001000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals((Double) 1.2, result.getDuration());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(0.92, result.getTonality(), 0.01);
    assertEquals(0.72, result.getVelocity(), 0.01);
    assertEquals(BigInteger.valueOf(1L), result.getProgramVoiceId());
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequencePatternId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(1001000L));

    assertNotExist(testDAO, BigInteger.valueOf(1001000L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(1001000L));
  }


    @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(of(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(of(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(of(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(of(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "leaves", 0.342, "C#", 110.286);

    // Sequence "leaves" has patterns "Ants" and "Caterpillars"
    insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    insertSequenceBinding(110, 1, 1, 0);
    insertPattern(2, 1, PatternType.Main, PatternState.Published, 16, "Caterpillars", 0.583, "E major", 140.0);
    insertSequenceBinding(211, 1, 2, 1);

    // Pattern "Caterpillars" has entities "C minor" and "D major"
    insertSequenceChord(2, 0, "C minor");
    insertSequenceChord(2, 4, "D major");

    // Instantiate the test subject
    testDAO = injector.getInstance(SequenceChordDAO.class);
  }

  @Test
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    SequenceChord inputData = new SequenceChord()
      .setPosition(4.0)
      .setName("G minor 7")
      .setProgramSequencePatternId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutPatternID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    SequenceChord inputData = new SequenceChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutName() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    SequenceChord inputData = new SequenceChord()
      .setPosition(4.0)
      .setProgramSequencePatternId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    SequenceChord result = testDAO.readOne(access, BigInteger.valueOf(2001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getProgramSequencePatternId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(2001L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<SequenceChord> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(2L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<SequenceChord> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    SequenceChord inputData = new SequenceChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(2002L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    SequenceChord inputData = new SequenceChord()
      .setPosition(4.0)
      .setProgramSequencePatternId(UUID.randomUUID());

    testDAO.update(access, BigInteger.valueOf(2001L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    SequenceChord inputData = new SequenceChord()
      .setPosition(4.0)
      .setProgramSequencePatternId(UUID.randomUUID())
      .setName("D minor");

    try {
      testDAO.update(access, BigInteger.valueOf(2001L), inputData);

    } catch (Exception e) {
      SequenceChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2001L));
      assertNotNull(result);
      assertEquals("D major", result.getName());
      assertEquals(BigInteger.valueOf(2L), result.getProgramSequencePatternId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    SequenceChord inputData = new SequenceChord()
      .setProgramSequencePatternId(UUID.randomUUID())
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, BigInteger.valueOf(2000L), inputData);

    SequenceChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2000L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals(Double.valueOf(4.0), result.getPosition());
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequencePatternId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(2000L));

    assertNotExist(testDAO, BigInteger.valueOf(2000L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(2000L));
  }

   */


}
