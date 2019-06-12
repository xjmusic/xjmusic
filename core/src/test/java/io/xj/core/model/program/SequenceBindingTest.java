//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.sub.SequenceBinding;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SequenceBindingTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SequenceBinding()
      .setSequenceId(UUID.randomUUID())
      .setOffset(14L)
      .setProgramId(BigInteger.valueOf(357L))
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceUUID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Sequence ID is required");

    new SequenceBinding()
      .setOffset(14L)
      .setProgramId(BigInteger.valueOf(357L))
      .validate();
  }


  @Test
  public void validate_failsWithoutProgramID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new SequenceBinding()
      .setSequenceId(UUID.randomUUID())
      .setOffset(14L)
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Offset is required");

    new SequenceBinding()
      .setSequenceId(UUID.randomUUID())
      .setProgramId(BigInteger.valueOf(357L))
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("offset"), new SequenceBinding().getResourceAttributeNames());
  }

  /*

//   assert result collection
//
//   @param count  to assert
//   @param result to make assertion about
private static void assertCollection(int count, Collection<SequenceBinding> result) {
  assertNotNull(result);
  assertEquals(count, result.size());
}

  @Before
  public void setUp() throws Exception {
    reset();

    // inject mocks
    createInjector();

    // Account "bananas"
    insert(newAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(newUserRole(2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    insert(newUserRole(3, UserRoleType.User);
    insert(newAccountUser(1, 3);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    insert(newLibrary(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "leaves", 0.342, "C#", 110.286);
    insertSequence(2, 2, 1, ProgramType.Macro, ProgramState.Published, "coconuts", 8.02, "D", 130.2);

    // Sequence "leaves" has patterns "Ants" and "Jibbawhammers"
    insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "Ants", 0.583, "D minor", 120.0);
    insertSequenceBinding(110, 1, 1, 0);
    insertPattern(2, 1, PatternType.Main, PatternState.Published, 16, "Jibbawhammers", 0.583, "E major", 140.0);
    insertSequenceBinding(211, 1, 2, 1);

    // Instantiate the test subject
    testDAO = injector.getInstance(SequenceBindingDAO.class);
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
    SequenceBinding inputData = new SequenceBinding()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(16L);

    SequenceBinding result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getSequenceId());
    assertEquals(BigInteger.valueOf(1L), result.getPatternId());
    assertEquals(Long.valueOf(16L), result.getOffset());
  }

  @Test
  public void create_FailsWithoutSequenceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequenceBinding inputData = new SequenceBinding()
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(16L);

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
    SequenceBinding inputData = new SequenceBinding()
      .setSequenceId(BigInteger.valueOf(2L))
      .setOffset(16L);

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
    SequenceBinding inputData = new SequenceBinding()
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

    SequenceBinding result = testDAO.readOne(access, BigInteger.valueOf(211));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
    assertEquals(BigInteger.valueOf(2L), result.getPatternId());
    assertEquals(Long.valueOf(1L), result.getOffset());
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
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<SequenceBinding> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
  }

   [#165803886] Segment memes expected to be taken directly from sequence_pattern binding

  @Test
  public void readAllAtSequenceOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<SequenceBinding> result = testDAO.readAllAtSequenceOffset(access, BigInteger.valueOf(1L), BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(1L, result.size());
  }


  // [#165803886] Segment memes expected to be taken directly from sequence_pattern binding

  @Test
  public void readAllAtSequenceOffset_samePatternBoundMultipleTimes() throws Exception {
    Access access = Access.internal();
    insertSequence(5, 2, 1, ProgramType.Main, ProgramState.Published, "Main Jam", 0.2, "Gb minor", 140);
    insertPattern(15, 5, PatternType.Main, PatternState.Published, 16, "Intro A", 0.5, "Gb minor", 135.0);
    insertSequenceChord(15, 0, "Gb minor");
    insertSequenceChord(15, 8, "G minor");
    insertPattern(16, 5, PatternType.Main, PatternState.Published, 16, "Intro B", 0.5, "G major", 135.0);
    insertSequenceChord(16, 0, "D minor");
    insertSequenceChord(16, 8, "G major");
    insertSequenceBindingAndMeme(5, 15, 0, "Zero");
    insertSequenceBindingAndMeme(5, 15, 1, "One");
    insertSequenceBindingAndMeme(5, 15, 2, "Two");
    insertSequenceBindingAndMeme(5, 15, 3, "Three");
    insertSequenceBindingAndMeme(5, 16, 4, "Four");
    insertSequenceBindingAndMeme(5, 16, 5, "Five");
    insertSequenceBindingAndMeme(5, 16, 6, "Six");

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

    Collection<SequenceBinding> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_Fails() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequenceBinding inputData = new SequenceBinding()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(16L);

    failure.expect(CoreException.class);
    failure.expectMessage("Not allowed to update SequenceBinding record.");

    testDAO.update(access, BigInteger.valueOf(1L), inputData);
  }

  @Test
  public void destroy() throws Exception {
    insertSequenceBindingMeme(110, "Acorns");
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(110));

    assertNotExist(testDAO, BigInteger.valueOf(110));
  }

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(newAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(newUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(newUserRole(2, UserRoleType.User);
    insert(newUserRole(2, UserRoleType.Admin);
    insert(newAccountUser(1, 2);
    insertUserAuth(2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222");
    insert(newUserAccessToken(2, UserAuthType.Google, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    insert(newUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    insert(newUserRole(3, UserRoleType.User);
    insert(newAccountUser(1, 3);

    // Bill has a "user" role but no account membership
    insert(newUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    insert(newUserRole(4, UserRoleType.User);

    // Library "palm tree" has sequence "leaves"
    insert(newLibrary(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "leaves", 0.342, "C#", 120.4);

    // Sequence "leaves" has pattern "growth" and pattern "decay"
    insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "growth", 0.342, "C#", 120.4);
    insertSequenceBinding(110, 1, 1, 0);
    insertPattern(2, 1, PatternType.Main, PatternState.Published, 16, "decay", 0.25, "F#", 110.3);
    insertSequenceBinding(211, 1, 2, 1);

    // Pattern "growth" has memes "ants" and "mold"
    insertSequenceBindingMeme(110, "Gravel");
    insertSequenceBindingMeme(110, "Fuzz");

    // Pattern "decay" has meme "peel"
    insertSequenceBindingMeme(211, "Peel");

    // Instantiate the test subject
    testDAO = injector.getInstance(SequenceBindingMemeDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    SequenceBindingMeme inputData = new SequenceBindingMeme()
      .setSequenceBindingId(BigInteger.valueOf(110L))
      .setName("  !!2gnarLY    ");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutSequenceBindingID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequenceBindingMeme inputData = new SequenceBindingMeme()
      .setName("gnarly");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequenceBindingMeme inputData = new SequenceBindingMeme()
      .setSequenceBindingId(BigInteger.valueOf(110L));

    testDAO.create(access, inputData);
  }

  @Test
  public void create_MacroSequenceBindingMeme() throws Exception {
    insertSequence(15, 2, 1, ProgramType.Macro, ProgramState.Published, "foods", 0.342, "C#", 120.4);
    insertPattern(21, 15, PatternType.Macro, PatternState.Published, 16, "meat", 0.342, "C#", 120.4);
    insertSequenceBinding(21150, 15, 21, 0);
    insertPattern(22, 15, PatternType.Macro, PatternState.Published, 16, "vegetable", 0.25, "F#", 110.3);
    insertSequenceBinding(22151, 15, 22, 1);
    insertSequenceBindingMeme(22151, "Squash");
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequenceBindingMeme inputData = new SequenceBindingMeme()
      .setSequenceBindingId(BigInteger.valueOf(22151))
      .setName("Ham");

    SequenceBindingMeme result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(22151), result.getSequenceBindingId());
    assertEquals("Ham", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    SequenceBindingMeme result = testDAO.readOne(access, BigInteger.valueOf(110001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(110L), result.getSequenceBindingId());
    assertEquals("Fuzz", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(110001L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<SequenceBindingMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(110L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<SequenceBindingMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(110L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    testDAO.destroy(access, BigInteger.valueOf(110000L));

    assertNotExist(testDAO, BigInteger.valueOf(110000L));
  }

  @Test
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.destroy(access, BigInteger.valueOf(110000L));
  }


 */

}
