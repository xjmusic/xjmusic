//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ProgramVoice;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class ProgramVoiceTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramVoice()
      .setType("Harmonic")
      .setName("Mic Check One Two")
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new ProgramVoice()
      .setType("Harmonic")
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Type is required");

    new ProgramVoice()
      .setName("Mic Check One Two")
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'chimney' is not a valid type");

    new ProgramVoice()
      .setType("chimney")
      .setName("Mic Check One Two")
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new ProgramVoice()
      .setType("Harmonic")
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type", "name"), new ProgramVoice().getResourceAttributeNames());
  }

  /*

  // FUTURE implement these Voice unit tests (adapted of legacy integration tests)

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
    insert(patternFactory.of(BigInteger.valueOf(5))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Army Ants")
      .setDensity(0.683)
      .setKey("Eb minor")
      .setTempo(122.4));

    insert(patternFactory.of(BigInteger.valueOf(1))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0));
    insertSequenceBinding(110, 1, 1, 0);
    insert(patternFactory.of(BigInteger.valueOf(2))
      .setProgramSequenceId(UUID.randomUUID())
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Caterpillars")
      .setDensity(0.583)
      .setKey("E major")
      .setTempo(140.0));
    insertSequenceBinding(211, 1, 2, 1);

    // Pattern "Ants" has Voices "Head" and "Body"
    insertVoice(1, 1, InstrumentType.Percussive, "This is a percussive voice");
    insertVoice(2, 1, InstrumentType.Melodic, "This is melodious");
    insertVoice(3, 1, InstrumentType.Harmonic, "This is harmonious");
    insertVoice(4, 1, InstrumentType.Vocal, "This is a vocal voice");

    // Instantiate the test subject
    testDAO = injector.getInstance(VoiceDAO.class);
  }

  @Test
  public void of() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Voice inputData = new Voice()
      .setProgramSequenceId(UUID.randomUUID())
      .setType("Harmonic")
      .setName("This is harmonious");

    Voice result = testDAO.of(access, inputData);

    assertNotNull(result);
    assertEquals(InstrumentType.Harmonic, result.getType());
    assertEquals("This is harmonious", result.getName());
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutSequenceID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Voice inputData = new Voice()
      .setType("Harmonic")
      .setName("This is harmonious");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutType() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Voice inputData = new Voice()
      .setProgramSequenceId(UUID.randomUUID())
      .setName("This is harmonious");

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Voice result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
    assertEquals(InstrumentType.Melodic, result.getType());
    assertEquals("This is melodious", result.getName());
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

    Collection<Voice> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(4L, result.size());
    Iterator<Voice> resultIt = result.iterator();
    assertEquals("This is a percussive voice", resultIt.next().getName());
    assertEquals("This is melodious", resultIt.next().getName());
    assertEquals("This is harmonious", resultIt.next().getName());
    assertEquals("This is a vocal voice", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<Voice> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Voice inputData = new Voice()
      .setType("Harmonic")
      .setName("This is harmonious");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsWithoutType() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Voice inputData = new Voice()
      .setProgramSequenceId(UUID.randomUUID())
      .setName("This is harmonious");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = CoreException.class)
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Voice inputData = new Voice()
      .setProgramSequenceId(UUID.randomUUID())
      .setType("Melodic")
      .setName("This is melodious");

    try {
      testDAO.update(access, BigInteger.valueOf(3L), inputData);

    } catch (Exception e) {
      Voice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(3L));
      assertNotNull(result);
      assertEquals(InstrumentType.Harmonic, result.getType());
      assertEquals("This is harmonious", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    Voice inputData = new Voice()
      .setProgramSequenceId(UUID.randomUUID())
      .setType("Melodic")
      .setName("This is melodious; Yoza!");

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    Voice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals("This is melodious; Yoza!", result.getName());
    assertEquals(InstrumentType.Melodic, result.getType());
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(1L));

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test(expected = CoreException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(account2), "Artist");

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

  @Test
  public void delete_SuccessEvenIfSequenceHasChildren() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "Artist");
    insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);

    testDAO.destroy(access, BigInteger.valueOf(1L));

    // Assert total annihilation
    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  // future test: VoiceDAO cannot delete record unless user has account access


   */

}
