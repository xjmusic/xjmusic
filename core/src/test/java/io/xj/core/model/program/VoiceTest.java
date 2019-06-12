//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.sub.Voice;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class VoiceTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Voice()
      .setType("Harmonic")
      .setDescription("Mic Check One Two")
      .setProgramId(BigInteger.valueOf(251L))
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new Voice()
      .setType("Harmonic")
      .setDescription("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Type is required");

    new Voice()
      .setDescription("Mic Check One Two")
      .setProgramId(BigInteger.valueOf(251L))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'chimney' is not a valid type");

    new Voice()
      .setType("chimney")
      .setDescription("Mic Check One Two")
      .setProgramId(BigInteger.valueOf(251L))
      .validate();
  }

  @Test
  public void validate_failsWithoutDescription() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Description is required");

    new Voice()
      .setType("Harmonic")
      .setProgramId(BigInteger.valueOf(251L))
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type", "description"), new Voice().getResourceAttributeNames());
  }

  /*

  // FUTURE implement these Voice unit tests (adapted from legacy integration tests)

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(newAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(newUserRole(2, UserRoleType.Admin);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(newLibrary(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "leaves", 0.342, "C#", 110.286);

    // Sequence "leaves" has patterns "Ants" and "Caterpillars"
    insert(patternFactory.newPattern(BigInteger.valueOf(5))
      .setSequenceId(BigInteger.valueOf(1))
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Army Ants")
      .setDensity(0.683)
      .setKey("Eb minor")
      .setTempo(122.4));

    insert(patternFactory.newPattern(BigInteger.valueOf(1))
      .setSequenceId(BigInteger.valueOf(1))
      .setTypeEnum(PatternType.Main)
      .setStateEnum(PatternState.Published)
      .setTotal(16)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0));
    insertSequenceBinding(110, 1, 1, 0);
    insert(patternFactory.newPattern(BigInteger.valueOf(2))
      .setSequenceId(BigInteger.valueOf(1))
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
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<Voice> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

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

    Collection<Voice> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

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

    assertNotExist(testDAO, BigInteger.valueOf(1L));
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
    insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);

    testDAO.destroy(access, BigInteger.valueOf(1L));

    // Assert total annihilation
    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  // future test: VoiceDAO cannot delete record unless user has account access


   */

}
