// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.MessageLite;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Instrument;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceBindingMeme;
import io.xj.ProgramSequenceChord;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.User;
import io.xj.UserRole;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.testing.HubIntegrationTestModule;
import io.xj.hub.testing.HubIntegrationTestProvider;
import io.xj.hub.testing.HubTestConfiguration;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubIntegrationTestModule()));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("bananas")
      .build());
    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("john")
      .setEmail("john@email.com")
      .setAvatarUrl("http://pictures.com/john.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.Admin)
      .build());

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("jenny")
      .setEmail("jenny@email.com")
      .setAvatarUrl("http://pictures.com/jenny.gif")
      .build());
    test.insert(UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(fake.user2.getId())
      .setType(UserRole.Type.User)
      .build());
    test.insert(AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setUserId(fake.user3.getId())
      .build());

    // Library "palm tree" has program "fonds" and program "nuts"
    fake.library1 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("palm tree")
      .build());
    fake.program1 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("fonds")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    fake.program1_sequence1 = test.insert(ProgramSequence.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setTotal(4)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0)
      .build());
    var sequenceBinding1a_0 = test.insert(ProgramSequenceBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramSequenceId(fake.program1_sequence1.getId())
      .setOffset(0)
      .build());
    test.insert(ProgramSequenceBindingMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(sequenceBinding1a_0.getProgramId())
      .setProgramSequenceBindingId(sequenceBinding1a_0.getId())
      .setName("chunk")
      .build());
    test.insert(ProgramSequenceBindingMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(sequenceBinding1a_0.getProgramId())
      .setProgramSequenceBindingId(sequenceBinding1a_0.getId())
      .setName("smooth")
      .build());
    fake.program2 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Program.Type.Rhythm)
      .setState(Program.State.Published)
      .setName("nuts")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());

    // Library "boat" has program "helm" and program "sail"
    fake.library2 = test.insert(Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("boat")
      .build());
    fake.program3 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Macro)
      .setState(Program.State.Published)
      .setName("helm")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    fake.program3_sequence1 = test.insert(ProgramSequence.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3.getId())
      .setTotal(16)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0)
      .build());
    test.insert(ProgramSequenceBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program3_sequence1.getProgramId())
      .setProgramSequenceId(fake.program3_sequence1.getId())
      .setOffset(0)
      .build());
    fake.program4 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Detail)
      .setState(Program.State.Published)
      .setName("sail")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());

    // Instantiate the test subject
    testDAO = injector.getInstance(ProgramDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program subject = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setKey("G minor 7")
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState(Program.State.Published)
      .setType(Program.Type.Main)
      .build();

    Program result = testDAO.create(
      hubAccess, subject);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(Program.Type.Main, result.getType());
  }

  /**
   [#156144567] Artist expects to of a Main-type program without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "User,Artist");
    Program inputData = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setKey("G minor 7")
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setDensity(0.6)
      .setState(Program.State.Published)
      .setType(Program.Type.Main)
      .build();

    Program result = testDAO.create(
      hubAccess, inputData);

    assertNotNull(result);
    assertEquals("G minor 7", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.1);
    assertEquals(Program.Type.Main, result.getType());
  }

  /**
   [#170290553] Clone sub-entities of program
   <p>
   [#175808105] Cloned Program should have same Voices and Chord Voicings
   <p>
   [#175947247] Artist expects to be able to clone Program without error
   */
  @Test
  public void clone_fromOriginal() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program inputData = Program.newBuilder()
      .setLibraryId(fake.library2.getId())
      .setName("cannons fifty nine")
      .build();
    test.insert(ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setName("cinnamon")
      .build());
    var voice = test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setType(Instrument.Type.Percussive)
      .setName("drums")
      .build());
    var track = test.insert(ProgramVoiceTrack.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(voice.getProgramId())
      .setProgramVoiceId(voice.getId())
      .setName("Kick")
      .build());
    var programSequenceChord = test.insert(ProgramSequenceChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramSequenceId(fake.program1_sequence1.getId())
      .setPosition(0)
      .setName("D")
      .build());
    test.insert(ProgramSequenceChordVoicing.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramSequenceChordId(programSequenceChord.getId())
      .setNotes("D2,F#2,A2")
      .build());
    var pattern = test.insert(ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramVoiceId(voice.getId())
      .setProgramSequenceId(fake.program1_sequence1.getId())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(8)
      .setName("jam")
      .build());
    test.insert(ProgramSequencePatternEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(pattern.getProgramId())
      .setProgramSequencePatternId(pattern.getId())
      .setProgramVoiceTrackId(track.getId())
      .setPosition(0)
      .setDuration(1)
      .setNote("C")
      .setVelocity(1)
      .build());

    DAOCloner<Program> resultCloner = testDAO.clone(hubAccess, fake.program1.getId(), inputData);

    Program result = resultCloner.getClone();
    assertNotNull(result);
    assertEquals(0.6, result.getDensity(), 0.01);
    assertEquals("C#", result.getKey());
    assertEquals(fake.library2.getId(), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120, result.getTempo(), 0.1);
    assertEquals(Program.Type.Main, result.getType());
    // Cloned ProgramMeme
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME)
      .where(io.xj.hub.tables.ProgramMeme.PROGRAM_MEME.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoice.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramVoice.PROGRAM_VOICE)
      .where(io.xj.hub.tables.ProgramVoice.PROGRAM_VOICE.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramVoiceTrack belongs to ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramVoiceTrack.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK)
      .where(io.xj.hub.tables.ProgramVoiceTrack.PROGRAM_VOICE_TRACK.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequence.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE)
      .where(io.xj.hub.tables.ProgramSequence.PROGRAM_SEQUENCE.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChord belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChord.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD)
      .where(io.xj.hub.tables.ProgramSequenceChord.PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceChordVoicing belongs to ProgramSequenceChord
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceChordVoicing.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(io.xj.hub.tables.ProgramSequenceChordVoicing.PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBinding belongs to ProgramSequence
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBinding.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING)
      .where(io.xj.hub.tables.ProgramSequenceBinding.PROGRAM_SEQUENCE_BINDING.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramSequenceBindingMeme belongs to ProgramSequenceBinding
    assertEquals(2, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequenceBindingMeme.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(2), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME)
      .where(io.xj.hub.tables.ProgramSequenceBindingMeme.PROGRAM_SEQUENCE_BINDING_MEME.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePattern belongs to ProgramSequence and ProgramVoice
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePattern.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN)
      .where(io.xj.hub.tables.ProgramSequencePattern.PROGRAM_SEQUENCE_PATTERN.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
    // Cloned ProgramSequencePatternEvent belongs to ProgramSequencePattern and ProgramVoiceTrack
    assertEquals(1, resultCloner.getChildClones().stream()
      .filter(e -> ProgramSequencePatternEvent.class.equals(e.getClass())).count());
    assertEquals(Integer.valueOf(1), test.getDSL()
      .selectCount().from(io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT)
      .where(io.xj.hub.tables.ProgramSequencePatternEvent.PROGRAM_SEQUENCE_PATTERN_EVENT.PROGRAM_ID.eq(UUID.fromString(result.getId())))
      .fetchOne(0, int.class));
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");

    Program result = testDAO.readOne(hubAccess, fake.program2.getId());

    assertNotNull(result);
    assertEquals(Program.Type.Rhythm, result.getType());
    assertEquals(Program.State.Published, result.getState());
    assertEquals(fake.program2.getId(), result.getId());
    assertEquals(fake.library1.getId(), result.getLibraryId());
    assertEquals("nuts", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "User");
    failure.expect(DAOException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(hubAccess, fake.program1.getId());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  /**
   [#176372189] Fabricator should get distinct Chord Voicing Types
   */
  @Test
  public void readManyWithChildEntities() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program inputData = Program.newBuilder()
      .setLibraryId(fake.library2.getId())
      .setName("cannons fifty nine")
      .build();
    test.insert(ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setName("cinnamon")
      .build());
    var voice = test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1.getId())
      .setType(Instrument.Type.Percussive)
      .setName("drums")
      .build());
    var track = test.insert(ProgramVoiceTrack.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(voice.getProgramId())
      .setProgramVoiceId(voice.getId())
      .setName("Kick")
      .build());
    var programSequenceChord = test.insert(ProgramSequenceChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramSequenceId(fake.program1_sequence1.getId())
      .setPosition(0)
      .setName("D")
      .build());
    test.insert(ProgramSequenceChordVoicing.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramSequenceChordId(programSequenceChord.getId())
      .setNotes("D2,F#2,A2")
      .build());
    var pattern = test.insert(ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program1_sequence1.getProgramId())
      .setProgramVoiceId(voice.getId())
      .setProgramSequenceId(fake.program1_sequence1.getId())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(8)
      .setName("jam")
      .build());
    test.insert(ProgramSequencePatternEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(pattern.getProgramId())
      .setProgramSequencePatternId(pattern.getId())
      .setProgramVoiceTrackId(track.getId())
      .setPosition(0)
      .setDuration(1)
      .setNote("C")
      .setVelocity(1)
      .build());

    Collection<MessageLite> results = testDAO.readManyWithChildEntities(hubAccess, ImmutableList.of(fake.program1.getId()));

    assertEquals(12, results.size());
    assertContains(Program.class, 1, results);
    assertContains(ProgramMeme.class, 1, results);
    assertContains(ProgramVoice.class, 1, results);
    assertContains(ProgramVoiceTrack.class, 1, results);
    assertContains(ProgramSequence.class, 1, results);
    assertContains(ProgramSequenceBinding.class, 1, results);
    assertContains(ProgramSequenceBindingMeme.class, 2, results);
    assertContains(ProgramSequencePattern.class, 1, results);
    assertContains(ProgramSequencePatternEvent.class, 1, results);
    assertContains(ProgramSequenceChord.class, 1, results);
    assertContains(ProgramSequenceChordVoicing.class, 1, results);
  }

  /**
   Assert that the results contain an exact count of classes in the results

   @param type    of class to search for
   @param total   count of instances to assert
   @param results to search within
   @param <N>     type of entity
   */
  private <N extends MessageLite> void assertContains(Class<N> type, int total, Collection<MessageLite> results) {
    assertEquals(String.format("Exactly %s count of %s class in results",
      total, type.getSimpleName()), total, results.stream()
      .filter(r -> type.isAssignableFrom(r.getClass()))
      .count());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString()).build()), "User");

    Collection<Program> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User");
    Program subject = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("cannons")
      .setLibraryId(UUID.randomUUID().toString())
      .build();

    try {
      testDAO.update(hubAccess, fake.program1.getId(), subject);

    } catch (Exception e) {
      Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
      assertNotNull(result);
      assertEquals("fonds", result.getName());
      assertEquals(fake.library1.getId(), result.getLibraryId());
      assertSame(DAOException.class, e.getClass());
    }
  }

  @Test
  public void update_Name() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program subject = Program.newBuilder()
      .setId(fake.program1.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setState(Program.State.Published)
      .setType(Program.Type.Main)
      .build();

    testDAO.update(hubAccess, fake.program1.getId(), subject);

    Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
    assertNotNull(result);
    assertEquals("cannons", result.getName());
    assertEquals(fake.library2.getId(), result.getLibraryId());
  }

  /**
   [#175789099] Artist should always be able to change program type
   <p>
   DEPRECATES [#170390872] prevent user from changing program type of a Rhythm program, when it has any Tracks and/or Voices.
   */
  @Test
  public void update_artistCanAlwaysChangeType() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program2.getId())
      .setType(Instrument.Type.Percussive)
      .setName("Drums")
      .build());
    Program subject = Program.newBuilder()
      .setId(fake.program2.getId())
      .setDensity(1.0)
      .setKey("G minor 7")
      .setLibraryId(fake.library1.getId())
      .setName("cannons")
      .setTempo(129.4)
      .setState(Program.State.Published)
      .setType(Program.Type.Main)
      .build();

    testDAO.update(hubAccess, fake.program2.getId(), subject);
  }

  /**
   [#156030760] Artist expects owner of Program or Instrument to always remain the same as when it was ofd, even after being updated by another user.
   DEPRECATED, future will be replaced by [#166724453] Instruments and Programs have author history
   */
  @Test
  public void update_Name_PreservesOriginalOwner() throws Exception {
    // John will edit a program originally belonging to Jenny
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Admin");
    Program subject = Program.newBuilder()
      .setId(fake.program1.getId())
      .setKey("G minor 7")
      .setDensity(1.0)
      .setLibraryId(fake.library2.getId())
      .setName("cannons")
      .setState(Program.State.Published)
      .setTempo(129.4)
      .setType(Program.Type.Main)
      .build();

    testDAO.update(hubAccess, fake.program1.getId(), subject);

    Program result = testDAO.readOne(HubAccess.internal(), fake.program1.getId());
    assertNotNull(result);
  }

  @Test
  public void destroy() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");

    testDAO.destroy(hubAccess, fake.program2.getId());

    try {
      testDAO.readOne(HubAccess.internal(), fake.program2.getId());
      fail();
    } catch (DAOException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.program35 = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("fonds")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());

    testDAO.destroy(hubAccess, fake.program35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.Program.PROGRAM)
      .where(io.xj.hub.tables.Program.PROGRAM.ID.eq(UUID.fromString(fake.program35.getId())))
      .fetchOne(0, int.class));
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    failure.expect(DAOException.class);
    failure.expectMessage("Program belonging to you does not exist");

    testDAO.destroy(hubAccess, fake.program1.getId());
  }

  /**
   [#170299297] Cannot delete Programs that have a Meme
   */
  @Test
  public void destroy_failsIfHasMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create("Admin");
    Program program = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library2.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("fonds")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    test.insert(ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setName("frozen")
      .build());
    test.insert(ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setName("ham")
      .build());

    failure.expect(DAOException.class);
    failure.expectMessage("Found Program Meme");

    testDAO.destroy(hubAccess, program.getId());
  }

  /**
   [#170299297] As long as program has no meme, destroy all other inner entities
   */
  @Test
  public void destroy_succeedsWithInnerEntitiesButNoMemes() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1), "Artist");
    Program program = test.insert(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(fake.library1.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("fonds")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build());
    var programSequence = test.insert(ProgramSequence.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setTotal(4)
      .setName("Ants")
      .setDensity(0.583)
      .setKey("D minor")
      .setTempo(120.0)
      .build());
    test.insert(ProgramSequenceBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequence.getProgramId())
      .setProgramSequenceId(programSequence.getId())
      .setOffset(0)
      .build());
    var voice = test.insert(ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setType(Instrument.Type.Percussive)
      .setName("drums")
      .build());
    var track = test.insert(ProgramVoiceTrack.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(voice.getProgramId())
      .setProgramVoiceId(voice.getId())
      .setName("Kick")
      .build());
    test.insert(ProgramSequenceChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequence.getProgramId())
      .setProgramSequenceId(programSequence.getId())
      .setPosition(0)
      .setName("D")
      .build());
    var pattern = test.insert(ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequence.getProgramId())
      .setProgramSequenceId(programSequence.getId())
      .setProgramVoiceId(voice.getId())
      .setType(ProgramSequencePattern.Type.Loop)
      .setTotal(8)
      .setName("jam")
      .build());
    test.insert(ProgramSequencePatternEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(pattern.getProgramId())
      .setProgramSequencePatternId(pattern.getId())
      .setProgramVoiceTrackId(track.getId())
      .setPosition(0)
      .setDuration(1)
      .setNote("C")
      .setVelocity(1)
      .build());

    testDAO.destroy(hubAccess, program.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.Program.PROGRAM)
      .where(io.xj.hub.tables.Program.PROGRAM.ID.eq(UUID.fromString(program.getId())))
      .fetchOne(0, int.class));
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<Program> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.library1.getId()));

    assertEquals(2L, result.size());
    Iterator<Program> resultIt = result.iterator();
    assertEquals("fonds", resultIt.next().getName());
    assertEquals("nuts", resultIt.next().getName());
  }

}

