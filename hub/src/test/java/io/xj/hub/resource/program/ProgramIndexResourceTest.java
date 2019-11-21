//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.program;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.access.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.User;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;

import static io.xj.core.access.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProgramIndexResourceTest extends CoreTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ProgramDAO programDAO;
  private Access access;
  private ProgramIndexResource subject;
  private User user101;
  private Library library25;

  @Before
  public void setUp() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
        }
      }));
    Account account1 = Account.create();
    access = Access.create(ImmutableList.of(account1), "User,Artist");
    user101 = User.create();
    library25 = Library.create();
    subject = new ProgramIndexResource();
    subject.setInjector(injector);
  }

  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program1 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    Program program2 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "trunk", "B", 120.0, 0.6);
    Collection<Program> programs = ImmutableList.of(program1, program2);
    when(programDAO.readMany(same(access), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(programs);
    subject.libraryId = library25.getId().toString();

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("programs", ImmutableList.of(program1.getId().toString(), program2.getId().toString()));
  }

  /*

  FUTURE: implement these tests with ?include=entity,entity type parameter



  @Test
  public void readAll_notOverwhelmingAmountOfSubEntities() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    //
    Program program701 = Program.create(101, 25, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, 0.6);
    program701.add(ProgramMeme.create("Ants"));
    ProgramSequence sequence902 = program701.add(ProgramSequence.create(16, "decay", 0.25, "F#", 110.3));
    program701.add(ProgramSequenceChord.create(sequence902, 0.0, "G minor"));
    program701.add(ProgramSequenceChord.create(sequence902, 4.0, "C major"));
    program701.add(ProgramSequenceChord.create(sequence902, 8.0, "F7"));
    program701.add(ProgramSequenceChord.create(sequence902, 12.0, "G7"));
    program701.add(ProgramSequenceChord.create(sequence902, 16.0, "F minor"));
    program701.add(ProgramSequenceChord.create(sequence902, 20.0, "Bb major"));
    ProgramSequenceBinding binding902_0 = program701.add(ProgramSequenceBinding.create(sequence902, 0));
    ProgramSequenceBinding binding902_1 = program701.add(ProgramSequenceBinding.create(sequence902, 1));
    ProgramSequenceBinding binding902_2 = program701.add(ProgramSequenceBinding.create(sequence902, 2));
    ProgramSequenceBinding binding902_3 = program701.add(ProgramSequenceBinding.create(sequence902, 3));
    ProgramSequenceBinding binding902_4 = program701.add(ProgramSequenceBinding.create(sequence902, 4));
    program701.add(ProgramSequenceBinding.create(sequence902, 5));
    program701.add(ProgramSequenceBindingMeme.create(binding902_0, "Gravel"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_1, "Gravel"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_2, "Gravel"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_3, "Rocks"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_1, "Fuzz"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_2, "Fuzz"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_3, "Fuzz"));
    program701.add(ProgramSequenceBindingMeme.create(binding902_4, "Noise"));
    //
    Program program35 = Program.create(101, 25, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6);
    insert(ProgramMeme.create("Basic"));
    ProgramVoice voiceDrums = insert(ProgramVoice.create(InstrumentType.Percussive, "Drums"));
    ProgramSequence sequence35a = insert(ProgramSequence.create(16, "Base", 0.5, "C", 110.3));
    ProgramSequencePattern pattern35a1 = insert(ProgramSequencePattern.create(sequence35a, voiceDrums, ProgramPatternType.Loop, 4, "Drop"));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "CLOCK")), 0.0, 1.0, "C2", 1.0));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "SNORT")), 1.0, 1.0, "G5", 0.8));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "KICK")), 2.5, 1.0, "C2", 0.6));
    insert(ProgramSequencePatternEvent.create(pattern35a1, insert(ProgramVoiceTrack.create(voiceDrums, "SNARL")), 3.0, 1.0, "G5", 0.9));
    ProgramSequencePattern pattern35a2 = insert(ProgramSequencePattern.create(sequence35a, voiceDrums, ProgramPatternType.Loop, 4, "Drop Alt"));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "CLACK")), 0.0, 1.0, "B5", 0.9));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "SNARN")), 1.0, 1.0, "D2", 1.0));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "CLICK")), 2.5, 1.0, "E4", 0.7));
    insert(ProgramSequencePatternEvent.create(pattern35a2, insert(ProgramVoiceTrack.create(voiceDrums, "SNAP")), 3.0, 1.0, "c3", 0.5));
    //
    Collection<Program> programs = ImmutableList.of(
      program701,
      program35
    );
    when(programDAO.readMany(same(access), eq(ImmutableList.of(BigInteger.valueOf(25)))))
      .thenReturn(programs);
    subject.libraryId = "25";

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("programs", ImmutableList.of("701", "35"));
  }

   */
}
