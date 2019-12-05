// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.payload.Payload;
import io.xj.core.payload.PayloadObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class ProgramTest  {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("state", "type", "name", "key", "tempo", "density"), Program.create().getResourceAttributeNames());
  }

  @Test
  public void validate_FailsWithoutLibraryID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Library ID is required");

    Program.create()
      .setUserId(UUID.randomUUID())
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

    Program.create()
      .setLibraryId(UUID.randomUUID())
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

    Program.create()
      .setState("Published")
      .setType("Main")
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void consumePayload_updateProgramName() throws CoreException {
    Program subject = Program.create(User.create(), Library.create(), ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, 0.6);
    // ↑ will be updated with the following payload ↓
    Payload payload = new Payload();
    payload.setDataOne(new PayloadObject()
      .setType("programs")
      .setId(UUID.randomUUID().toString())
      .setAttribute("name", "Earth to Fire 69"));

    subject.consume(payload);

    assertEquals("Earth to Fire 69", subject.getName());
  }

  /*

// FUTURE adapt any of these legacy tests that seem relevant; delete definitely useless ones

  @Test
  public void getSequenceBindingsOfSequenceAtOffset() throws Exception {
    insertSequenceBinding(999, 701, 902, 5, Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(chainBinding("Library", 10000001)));

    assertEquals(1, ingest.getSequenceBindingsOfProgramAtOffset(program701.getId(), BigInteger.valueOf(2)).size());
    assertEquals(2, ingest.getSequenceBindingsOfProgramAtOffset(program701.getId(), BigInteger.valueOf(5)).size());
  }

    @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(of(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    insert(of(2, "john", "john@email.com", "http://pictures.com/john.gif");
    insert(of(2, UserRoleType.User);
    insert(of(2, UserRoleType.Admin);
    insert(of(1, 2);
    insertUserAuth(2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222");
    insert(of(2, UserAuthType.Google, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    insert(of(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    insert(of(3, UserRoleType.User);
    insert(of(1, 3);

    // Bill has a "user" role but no account membership
    insert(of(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    insert(of(4, UserRoleType.User);

    // Library "palm tree" has sequence "leaves", sequence "coconuts" and sequence "bananas"
    insert(of(1, 1, "palm tree",now()));
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
  public void of() throws Exception {
    Access access = of(user2, ImmutableList.of(account1), "Artist");
    ProgramMeme inputData = new ProgramMeme()
      .setProgramSequenceId(UUID.randomUUID())
      .setName("  !!2gnarLY    ");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutSequenceID() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ProgramMeme inputData = new ProgramMeme()
      .setName("gnarly");

    testDAO.of(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void of_FailsWithoutName() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    ProgramMeme inputData = new ProgramMeme()
      .setProgramSequenceId(UUID.randomUUID());

    testDAO.of(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    ProgramMeme result = testDAO.readOne(access, BigInteger.valueOf(1001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getProgramSequenceId());
    assertEquals("Mold", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1001L));
  }

  @Test
  public void readMany() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");

    Collection<ProgramMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, result.size());
    Iterator<ProgramMeme> resultIt = result.iterator();
    assertEquals("Ants", resultIt.next().getName());
    assertEquals("Mold", resultIt.next().getName());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = of(ImmutableList.of(of()), "Artist");

    Collection<ProgramMeme> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = of(ImmutableList.of(account1), "Artist");
    testDAO.destroy(access, BigInteger.valueOf(1001L));

    assertNotExist(testDAO, BigInteger.valueOf(1001L));
  }

   */

  /*


    @Test
  public void getAllProgramMemes() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(3, ingest.getAllProgramMemes().size());
  }

  @Test
  public void getProgramMemesOfProgram() throws Exception {
    insertProgramMeme(701, "More Ants", Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(2, ingest.getProgramMemesOfProgram(program701.getId()).size());
    assertEquals(1, ingest.getProgramMemesOfProgram(BigInteger.valueOf(702)).size());
    assertEquals(1, ingest.getProgramMemesOfProgram(program703.getId()).size());
  }

  @Test
  public void getMemesAtBeginningOfProgram() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    Collection<MemeEntity> results = ingest.getMemesAtBeginningOfProgram(program701.getId());

    assertEquals(2, results.size());
    Iterator<MemeEntity> resultsIterator = results.iterator();
    MemeEntity result0 = resultsIterator.next();
    assertSame(ProgramMeme.class, result0.getClass());
    assertEquals("Ants", result0.getName());
    MemeEntity result1 = resultsIterator.next();
    assertSame(ProgramBindingMeme.class, result1.getClass());
    assertEquals("Gravel", result1.getName());
  }

  @Test
  public void getAllPatterns() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(2, ingest.getAllPatterns().size());
  }

  @Test
  public void getPatternsOfProgram() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(2, ingest.getPatternsOfProgram(program701.getId()).size());
  }

  @Test
  public void getAllProgramBindingMemes() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(8, ingest.getAllProgramBindingMemes().size());
  }

  @Test
  public void getAllProgramBindingsOfProgram() throws Exception {
    Ingest ingest1 = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));
    assertEquals(6, ingest1.getProgramBindingsOfProgram(program701.getId()).size());

    Ingest ingest2 = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000002)));
    assertEquals(2, ingest2.getProgramBindingsOfProgram(BigInteger.valueOf(751)).size());
  }


  @Test
  public void getAllProgramChords() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    // assertEquals(6, ingest.getAllProgramChords().size());
    // FUTURE programChords directly of Pattern
  }

  @Test
  public void getChordsOfPattern() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

//    assertEquals(6, ingest.getChordsOfPattern(BigInteger.valueOf(902)).size());
//    assertEquals(0, ingest.getChordsOfPattern(BigInteger.valueOf(901)).size());
    // FUTURE programChords directly of Pattern
  }

  @Test
  public void getAllVoices() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(2, ingest.getAllVoices().size());
  }

  @Test
  public void getVoicesOfProgram() throws Exception {
    // FUTURE refactor this to use voices already inserted
    insertVoice(1299, 702, InstrumentType.Harmonic, "Bass", Instant.now());
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    assertEquals(1, ingest.getVoicesOfProgram(program701.getId()).size());
    assertEquals(2, ingest.getVoicesOfProgram(BigInteger.valueOf(702)).size());
  }

  @Test
  public void getAllPatternEvents() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    // FUTURE test pattern events ingested into pattern
//    assertEquals(4, ingest.getAllPatternEvents().size());
  }

  @Test
  public void getEventsOfPatternByVoice() throws Exception {
    Ingest ingest = ingestFactory.ingest(Access.internal(), ImmutableList.of(of("Library", 10000001)));

    // FUTURE test pattern events ingested into pattern, retrieve by voice
//    assertEquals(4, ingest.getEventsOfPatternByVoice(BigInteger.valueOf(901), BigInteger.valueOf(1201)).size());
//    assertEquals(0, ingest.getEventsOfPatternByVoice(BigInteger.valueOf(901), BigInteger.valueOf(1202)).size());
  }

   */

}

