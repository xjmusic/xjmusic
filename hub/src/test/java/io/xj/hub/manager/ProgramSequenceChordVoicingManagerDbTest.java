// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or destroy programs
@RunWith(MockitoJUnitRunner.class)
public class ProgramSequenceChordVoicingManagerDbTest {
  private ProgramSequenceChordVoicingManager subject;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  private ProgramSequenceChord sequenceChord1a_0;
  private ProgramSequenceChordVoicing sequenceChord1a_0_voicing0;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));
// John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "palm tree" has a program "Ants" and program "Ants"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));
    sequenceChord1a_0 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "C minor"));
    sequenceChord1a_0_voicing0 = test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, InstrumentType.Bass, "C5, Eb5, G5"));
    test.insert(buildProgramSequenceChordVoicing(sequenceChord1a_0, InstrumentType.Bass, "G,B,Db,F"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "ANTS", "C#", 120.0f, 0.6f));
    fake.program702_voice1 = test.insert(buildProgramVoice(fake.program2, InstrumentType.Drum, "Drums"));

    // Library "boat" has a program "helm" and program "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.program3 = test.insert(buildProgram(fake.library2, ProgramType.Macro, ProgramState.Published, "helm", "C#", 120.0f, 0.6f));
    fake.program3_sequence1 = test.insert(buildProgramSequence(fake.program3, 16, "Ants", 0.583f, "D minor"));
    fake.program3_chord1 = test.insert(buildProgramSequenceChord(fake.program1_sequence1, 0.0f, "G7 flat 6"));
    fake.program4 = test.insert(buildProgram(fake.library2, ProgramType.Detail, ProgramState.Published, "sail", "C#", 120.0f, 0.6f));

    // Instantiate the test entity
    subject = injector.getInstance(ProgramSequenceChordVoicingManager.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var entity = new ProgramSequenceChordVoicing();
    entity.setId(UUID.randomUUID());
    entity.setProgramId(fake.program3.getId());
    entity.setProgramSequenceChordId(fake.program3_chord1.getId());
    entity.setType(InstrumentType.Pad);
    entity.setNotes("C5, Eb5, G5");

    var result = subject.create(
      access, entity);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_chord1.getId(), result.getProgramSequenceChordId());
    assertEquals(InstrumentType.Pad, result.getType());
    assertEquals("C5, Eb5, G5", result.getNotes());
  }

  /**
   Creating a voicing type deletes (overwrites) the previously existent voicing
   Re: Lab should be able to create voicing for MP chord where there is none
   https://www.pivotaltracker.com/story/show/182132495
   <p>
   PREVIOUS BEHAVIOR: Cannot create another voicing for a chord with the same type as an existing voicing for that chord https://www.pivotaltracker.com/story/show/181159558
   */
  @Test
  public void create_cannotCreateAnotherForExistingInstrumentType() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var voicing1a = subject.create(access, buildProgramSequenceChordVoicing(fake.program3_chord1, InstrumentType.Pad, "C5, Eb5, G5"));
    var voicing1b = buildProgramSequenceChordVoicing(fake.program3_chord1, InstrumentType.Pad, "A4, C5, E5");


    subject.create(access, voicing1b);

    var e = assertThrows(ManagerException.class, () -> subject.readOne(access, voicing1a.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  /**
   https://www.pivotaltracker.com/story/show/176162975 Endpoint to batch update ProgramSequenceChordVoicing
   */
  @Test
  public void update() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    sequenceChord1a_0_voicing0.setType(InstrumentType.Sticky);
    sequenceChord1a_0_voicing0.setNotes("G1,G2,G3");

    subject.update(access, sequenceChord1a_0_voicing0.getId(), sequenceChord1a_0_voicing0);

    var result = subject.readOne(access, sequenceChord1a_0_voicing0.getId());
    assertNotNull(result);
    assertEquals(sequenceChord1a_0_voicing0.getId(), result.getId());
    assertEquals(InstrumentType.Sticky, result.getType());
    assertEquals("G1,G2,G3", result.getNotes());
  }

  /**
   Updating to an existing voicing type deletes (overwrites) the previously existent voicing
   Re: Lab should be able to create voicing for MP chord where there is none
   https://www.pivotaltracker.com/story/show/182132495
   <p>
   PREVIOUS BEHAVIOR: Cannot update this voicing to a type that already exists for that chord https://www.pivotaltracker.com/story/show/181159558
   */
  @Test
  public void update_cannotUpdateToTypeOfExistingVoicing() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var voicing1a = buildProgramSequenceChordVoicing(fake.program3_chord1, InstrumentType.Pad, "C5, Eb5, G5");
    subject.create(access, voicing1a);
    ProgramSequenceChordVoicing voicing1b = subject.create(access, buildProgramSequenceChordVoicing(fake.program3_chord1, InstrumentType.Drum, "A4, C5, E5"));
    voicing1b.setType(InstrumentType.Pad);

    subject.update(access, voicing1b.getId(), voicing1b);

    var updated = subject.readOne(access, voicing1b.getId());
    assertEquals(InstrumentType.Pad, updated.getType());
  }

  /**
   https://www.pivotaltracker.com/story/show/156144567 Artist expects to of a Main-type programSequenceChordVoicing without crashing the entire platform
   NOTE: This simple test fails to invoke the complexity of database call that is/was creating this issue in production.
   */
  @Test
  public void create_asArtist() throws Exception {
    HubAccess access = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var inputData = new ProgramSequenceChordVoicing();
    inputData.setId(UUID.randomUUID());
    inputData.setProgramId(fake.program3.getId());
    inputData.setProgramSequenceChordId(fake.program3_chord1.getId());
    inputData.setType(InstrumentType.Bass);
    inputData.setNotes("C5, Eb5, G5");

    var result = subject.create(
      access, inputData);

    assertNotNull(result);
    assertEquals(fake.program3.getId(), result.getProgramId());
    assertEquals(fake.program3_chord1.getId(), result.getProgramSequenceChordId());
    assertEquals(InstrumentType.Bass, result.getType());
    assertEquals("C5, Eb5, G5", result.getNotes());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = subject.readOne(access, sequenceChord1a_0_voicing0.getId());

    assertNotNull(result);
    assertEquals(sequenceChord1a_0_voicing0.getId(), result.getId());
    assertEquals(fake.program1.getId(), result.getProgramId());
    assertEquals(sequenceChord1a_0.getId(), result.getProgramSequenceChordId());
    assertEquals(InstrumentType.Bass, result.getType());
    assertEquals("C5, Eb5, G5", result.getNotes());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");

    var e = assertThrows(ManagerException.class,
      () -> subject.readOne(access, sequenceChord1a_0_voicing0.getId()));
    assertEquals("Record does not exist", e.getMessage());
  }

  // future test: readManyInAccount vs readManyInLibraries, positive and negative cases

  @Test
  public void readMany() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceChordVoicing> result = subject.readMany(access, ImmutableList.of(fake.program1.getId()));

    assertEquals(2L, result.size());
    Iterator<ProgramSequenceChordVoicing> resultIt = result.iterator();
    assertEquals("C5, Eb5, G5", resultIt.next().getNotes());
    assertEquals("G,B,Db,F", resultIt.next().getNotes());
  }

  /**
   Chord Search while composing a main program
   https://www.pivotaltracker.com/story/show/178921705
   */
  @Test
  public void readMany_forChords() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<ProgramSequenceChordVoicing> result = subject.readManyForChords(access, ImmutableList.of(sequenceChord1a_0.getId()));

    assertEquals(2L, result.size());
  }

  @Test
  public void readMany_SeesNothingOutsideOfLibrary() throws Exception {
    HubAccess access = HubAccess.create(ImmutableList.of(buildAccount("Testing")), "User, Artist");

    Collection<ProgramSequenceChordVoicing> result = subject.readMany(access, ImmutableList.of(fake.program1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void destroy_failsIfNotInAccount() throws Exception {
    fake.account2 = buildAccount("Testing");
    HubAccess access = HubAccess.create(ImmutableList.of(fake.account2), "Artist");

    var e = assertThrows(ManagerException.class,
      () -> subject.destroy(access, sequenceChord1a_0_voicing0.getId()));
    assertEquals("Voicing belongs to Program in Account you have access to does not exist", e.getMessage());
  }

}

