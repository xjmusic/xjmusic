//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.math.BigInteger;

import static io.xj.core.testing.Assert.assertSameItems;
import static io.xj.core.testing.AssertPayloadObject.assertPayloadObject;
import static org.junit.Assert.assertEquals;

public class ProgramImplTest extends CoreTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("createdAt", "updatedAt", "state", "type", "name", "key", "tempo", "density"), programFactory.newProgram().getResourceAttributeNames());
  }

  @Test
  public void validate_FailsWithoutLibraryID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Library ID is required");

    programFactory.newProgram()
      .setUserId(BigInteger.valueOf(3))
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main")
      .validate();
  }

  @Test
  public void validate_FailsWithoutUserID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    programFactory.newProgram()
      .setLibraryId(BigInteger.valueOf(2L))
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main")
      .validate();
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    programFactory.newProgram()
      .setState("Published")
      .setType("Main")
      .setUserId(BigInteger.valueOf(3))
      .setLibraryId(BigInteger.valueOf(1L))
      .validate();
  }

  @Test
  public void deserialize_updateComplexProgram() throws IOException, CoreException {
    Payload updateComplexProgram = deserializePayload("{\"data\":{\"id\":\"12\",\"attributes\":{\"density\":null,\"key\":\"Ebm\",\"name\":\"Earth to Fire 69\",\"tempo\":121,\"type\":\"Macro\",\"state\":\"Published\"},\"relationships\":{\"library\":{\"data\":{\"type\":\"libraries\",\"id\":\"3\"}},\"patterns\":{\"data\":[]},\"patternEvents\":{\"data\":[]},\"programMemes\":{\"data\":[]},\"sequences\":{\"data\":[{\"type\":\"sequences\",\"id\":\"2c13022b-ebc7-4e3e-be56-737b57dde9c6\"},{\"type\":\"sequences\",\"id\":\"416d9986-2fa1-44e2-8e44-2cd0b6edd61c\"}]},\"sequenceBindings\":{\"data\":[{\"type\":\"sequence-bindings\",\"id\":\"00e16e13-9ebb-4d0d-8f62-d13b022fccd3\"},{\"type\":\"sequence-bindings\",\"id\":\"336b693a-988d-4e04-82ef-e4b26f9eb549\"},{\"type\":\"sequence-bindings\",\"id\":\"b8f7617a-10a3-4ffb-8abd-31efb262b743\"}]},\"sequenceBindingMemes\":{\"data\":[{\"type\":\"sequence-binding-memes\",\"id\":\"c5c20347-968e-4992-848a-66d28531c51c\"},{\"type\":\"sequence-binding-memes\",\"id\":\"34fd41a8-de9f-4278-ac5f-588eefddd953\"},{\"type\":\"sequence-binding-memes\",\"id\":\"a6a230c7-9501-4636-9ed2-25dbc3bbbe3e\"}]},\"sequenceChords\":{\"data\":[]},\"voices\":{\"data\":[]}},\"type\":\"programs\"},\"included\":[{\"attributes\":{\"density\":0.6,\"key\":\"Ebm\",\"name\":\"Passion Volcano\",\"tempo\":121},\"relationships\":{\"program\":{\"data\":{\"type\":\"programs\",\"id\":\"12\"}},\"sequenceBindings\":{\"data\":[{\"type\":\"sequence-bindings\",\"id\":\"336b693a-988d-4e04-82ef-e4b26f9eb549\"},{\"type\":\"sequence-bindings\",\"id\":\"b8f7617a-10a3-4ffb-8abd-31efb262b743\"}]},\"sequenceChords\":{\"data\":[]},\"patterns\":{\"data\":[]}},\"type\":\"sequences\",\"id\":\"2c13022b-ebc7-4e3e-be56-737b57dde9c6\"},{\"attributes\":{\"density\":0.6,\"key\":\"B\",\"name\":\"Exploding\",\"tempo\":121},\"relationships\":{\"program\":{\"data\":{\"type\":\"programs\",\"id\":\"12\"}},\"sequenceBindings\":{\"data\":[{\"type\":\"sequence-bindings\",\"id\":\"00e16e13-9ebb-4d0d-8f62-d13b022fccd3\"}]},\"sequenceChords\":{\"data\":[]},\"patterns\":{\"data\":[]}},\"type\":\"sequences\",\"id\":\"416d9986-2fa1-44e2-8e44-2cd0b6edd61c\"},{\"attributes\":{\"offset\":1},\"relationships\":{\"program\":{\"data\":{\"type\":\"programs\",\"id\":\"12\"}},\"sequence\":{\"data\":{\"type\":\"sequences\",\"id\":\"416d9986-2fa1-44e2-8e44-2cd0b6edd61c\"}}},\"type\":\"sequence-bindings\",\"id\":\"00e16e13-9ebb-4d0d-8f62-d13b022fccd3\"},{\"attributes\":{\"offset\":0},\"relationships\":{\"program\":{\"data\":{\"type\":\"programs\",\"id\":\"12\"}},\"sequence\":{\"data\":{\"type\":\"sequences\",\"id\":\"2c13022b-ebc7-4e3e-be56-737b57dde9c6\"}}},\"type\":\"sequence-bindings\",\"id\":\"336b693a-988d-4e04-82ef-e4b26f9eb549\"},{\"attributes\":{\"offset\":2},\"relationships\":{\"program\":{\"data\":{\"type\":\"programs\",\"id\":\"12\"}},\"sequence\":{\"data\":{\"type\":\"sequences\",\"id\":\"2c13022b-ebc7-4e3e-be56-737b57dde9c6\"}}},\"type\":\"sequence-bindings\",\"id\":\"b8f7617a-10a3-4ffb-8abd-31efb262b743\"},{\"attributes\":{\"name\":\"Fire\"},\"relationships\":{\"sequenceBinding\":{\"data\":{\"type\":\"sequence-bindings\",\"id\":\"b8f7617a-10a3-4ffb-8abd-31efb262b743\"}}},\"type\":\"sequence-binding-memes\",\"id\":\"c5c20347-968e-4992-848a-66d28531c51c\"},{\"attributes\":{\"name\":\"Fire\"},\"relationships\":{\"sequenceBinding\":{\"data\":{\"type\":\"sequence-bindings\",\"id\":\"00e16e13-9ebb-4d0d-8f62-d13b022fccd3\"}}},\"type\":\"sequence-binding-memes\",\"id\":\"34fd41a8-de9f-4278-ac5f-588eefddd953\"},{\"attributes\":{\"name\":\"Earth\"},\"relationships\":{\"sequenceBinding\":{\"data\":{\"type\":\"sequence-bindings\",\"id\":\"336b693a-988d-4e04-82ef-e4b26f9eb549\"}}},\"type\":\"sequence-binding-memes\",\"id\":\"a6a230c7-9501-4636-9ed2-25dbc3bbbe3e\"}]}");
    Program program = newProgram(12, 101, 3, ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, now());

    program.consume(updateComplexProgram);

    assertEquals("Earth to Fire 69", program.getName());
    assertEquals(2, program.getSequences().size());
    assertEquals(3, program.getSequenceBindings().size());
    assertEquals(3, program.getSequenceBindingMemes().size());
  }

  @Test
  public void toPayloadObject() throws IOException {
    Program program = newProgram(12, 101, 3, ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, now());
    Sequence sequenceA = program.add(newSequence(0, "Passion Volcano", 0.6, "Ebm", 121.0));
    Sequence sequenceB = program.add(newSequence(0, "Exploding", 0.6, "B", 121.0));
    SequenceBinding sequenceBinding0 = program.add(newSequenceBinding(sequenceA, 0));
    SequenceBinding sequenceBinding1 = program.add(newSequenceBinding(sequenceB, 1));
    SequenceBinding sequenceBinding2 = program.add(newSequenceBinding(sequenceA, 2));
    SequenceBindingMeme sequenceBindingMeme0 = program.add(newSequenceBindingMeme(sequenceBinding0, "Earth"));
    SequenceBindingMeme sequenceBindingMeme1 = program.add(newSequenceBindingMeme(sequenceBinding1, "Fire"));
    SequenceBindingMeme sequenceBindingMeme2 = program.add(newSequenceBindingMeme(sequenceBinding2, "Fire"));

    assertPayloadObject(sequenceBinding0.toPayloadObject(program.getAllSubEntities()))
      .hasMany(SequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme0));
    assertPayloadObject(sequenceBinding1.toPayloadObject(program.getAllSubEntities()))
      .hasMany(SequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme1));
    assertPayloadObject(sequenceBinding2.toPayloadObject(program.getAllSubEntities()))
      .hasMany(SequenceBindingMeme.class, ImmutableList.of(sequenceBindingMeme2));
  }



  /*

 // FUTURE write ProgramImplTest

  @Test
  public void getSequenceBindingsOfSequenceAtOffset() throws Exception {
    insertSequenceBinding(999, 701, 902, 5, Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(chainBinding("Library", 10000001)));

    assertEquals(1, ingest.getSequenceBindingsOfProgramAtOffset(BigInteger.valueOf(701), BigInteger.valueOf(2)).size());
    assertEquals(2, ingest.getSequenceBindingsOfProgramAtOffset(BigInteger.valueOf(701), BigInteger.valueOf(5)).size());
  }

  // FUTURE Implement these Program unit tests (adapted from legacy integration tests)

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

    // Library "palm tree" has sequence "leaves", sequence "coconuts" and sequence "bananas"
    insert(newLibrary(1, 1, "palm tree",now()));
    insertSequence(1, 2, 1, ProgramType.Main, ProgramState.Published, "leaves", 0.342, "C#", 120.4);
    insertSequence(2, 2, 1, ProgramType.Main, ProgramState.Published, "coconuts", 0.25, "F#", 110.3);
    insertSequence(3, 2, 1, ProgramType.Main, ProgramState.Published, "bananas", 0.27, "Gb", 100.6);

    // Sequence "leaves" has memes "ants" and "mold"
    insertProgramMeme(1, "Ants");
    insertProgramMeme(1, "Mold");

    // Sequence "bananas" has meme "peel"
    insertProgramMeme(3, "Peel");

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramMemeDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    ProgramMeme inputData = new ProgramMeme()
      .setSequenceId(BigInteger.valueOf(1L))
      .setName("  !!2gnarLY    ");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutSequenceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ProgramMeme inputData = new ProgramMeme()
      .setName("gnarly");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    ProgramMeme inputData = new ProgramMeme()
      .setSequenceId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    ProgramMeme result = testDAO.readOne(access, BigInteger.valueOf(1001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
    assertEquals("Mold", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1001L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<ProgramMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<ProgramMeme> resultIt = result.iterator();
    assertEquals("Ants", resultIt.next().getName());
    assertEquals("Mold", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<ProgramMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    testDAO.destroy(access, BigInteger.valueOf(1001L));

    assertNotExist(testDAO, BigInteger.valueOf(1001L));
  }

   */

  /*

  // FUTURE implement these unit tests (adapted from legacy ingest tests)

    @Test
  public void getAllProgramMemes() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(3, ingest.getAllProgramMemes().size());
  }

  @Test
  public void getProgramMemesOfProgram() throws Exception {
    // FUTURE refactor this to use program memes already inserted
    insertProgramMeme(701, "More Ants", Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getProgramMemesOfProgram(BigInteger.valueOf(701)).size());
    assertEquals(1, ingest.getProgramMemesOfProgram(BigInteger.valueOf(702)).size());
    assertEquals(1, ingest.getProgramMemesOfProgram(BigInteger.valueOf(703)).size());
  }

  @Test
  public void getMemesAtBeginningOfProgram() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    Collection<Meme> results = ingest.getMemesAtBeginningOfProgram(BigInteger.valueOf(701));

    assertEquals(2, results.size());
    Iterator<Meme> resultsIterator = results.iterator();
    Meme result0 = resultsIterator.next();
    assertSame(ProgramMeme.class, result0.getClass());
    assertEquals("Ants", result0.getName());
    Meme result1 = resultsIterator.next();
    assertSame(ProgramBindingMeme.class, result1.getClass());
    assertEquals("Gravel", result1.getName());
  }

  @Test
  public void getAllPatterns() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getAllPatterns().size());
  }

  @Test
  public void getPatternsOfProgram() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getPatternsOfProgram(BigInteger.valueOf(701)).size());
  }

  @Test
  public void getAllProgramBindingMemes() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(8, ingest.getAllProgramBindingMemes().size());
  }

  @Test
  public void getAllProgramBindingsOfProgram() throws Exception {
    Ingest ingest1 = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));
    assertEquals(6, ingest1.getProgramBindingsOfProgram(BigInteger.valueOf(701)).size());

    Ingest ingest2 = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000002)));
    assertEquals(2, ingest2.getProgramBindingsOfProgram(BigInteger.valueOf(751)).size());
  }


  @Test
  public void getAllProgramChords() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    // assertEquals(6, ingest.getAllProgramChords().size());
    // FUTURE programChords directly from Pattern
  }

  @Test
  public void getChordsOfPattern() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

//    assertEquals(6, ingest.getChordsOfPattern(BigInteger.valueOf(902)).size());
//    assertEquals(0, ingest.getChordsOfPattern(BigInteger.valueOf(901)).size());
    // FUTURE programChords directly from Pattern
  }

  @Test
  public void getAllVoices() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(2, ingest.getAllVoices().size());
  }

  @Test
  public void getVoicesOfProgram() throws Exception {
    // FUTURE refactor this to use voices already inserted
    insertVoice(1299, 702, InstrumentType.Harmonic, "Bass", Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    assertEquals(1, ingest.getVoicesOfProgram(BigInteger.valueOf(701)).size());
    assertEquals(2, ingest.getVoicesOfProgram(BigInteger.valueOf(702)).size());
  }

  @Test
  public void getAllPatternEvents() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    // FUTURE test pattern events ingested into pattern
//    assertEquals(4, ingest.getAllPatternEvents().size());
  }

  @Test
  public void getEventsOfPatternByVoice() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(newChainBinding("Library", 10000001)));

    // FUTURE test pattern events ingested into pattern, retrieve by voice
//    assertEquals(4, ingest.getEventsOfPatternByVoice(BigInteger.valueOf(901), BigInteger.valueOf(1201)).size());
//    assertEquals(0, ingest.getEventsOfPatternByVoice(BigInteger.valueOf(901), BigInteger.valueOf(1202)).size());
  }

   */

}

