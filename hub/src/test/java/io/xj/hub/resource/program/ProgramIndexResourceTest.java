//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.program;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.PatternType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Pattern;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.Voice;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;

import static io.xj.core.access.impl.Access.CONTEXT_KEY;
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

  @Before
  public void setUp() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
        }
      }));
    access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "programs", "1"
    ));
    subject = new ProgramIndexResource();
    subject.setInjector(injector);
  }

  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Collection<Program> programs = ImmutableList.of(
      newProgram(1, 101, 25, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, now()),
      newProgram(2, 101, 25, ProgramType.Main, ProgramState.Published, "trunk", "B", 120.0, now())
    );
    when(programDAO.readMany(same(access), eq(ImmutableList.of(BigInteger.valueOf(25)))))
      .thenReturn(programs);
    subject.libraryId = "25";

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("programs", ImmutableList.of("1", "2"));
  }

  @Test
  public void readAll_notOverwhelmingAmountOfSubEntities() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    //
    Program program701 = newProgram(701, 101, 25, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, now());
    program701.add(newProgramMeme("Ants"));
    Sequence sequence902 = program701.add(newSequence(16, "decay", 0.25, "F#", 110.3));
    program701.add(newSequenceChord(sequence902, 0.0, "G minor"));
    program701.add(newSequenceChord(sequence902, 4.0, "C major"));
    program701.add(newSequenceChord(sequence902, 8.0, "F7"));
    program701.add(newSequenceChord(sequence902, 12.0, "G7"));
    program701.add(newSequenceChord(sequence902, 16.0, "F minor"));
    program701.add(newSequenceChord(sequence902, 20.0, "Bb major"));
    SequenceBinding binding902_0 = program701.add(newSequenceBinding(sequence902, 0));
    SequenceBinding binding902_1 = program701.add(newSequenceBinding(sequence902, 1));
    SequenceBinding binding902_2 = program701.add(newSequenceBinding(sequence902, 2));
    SequenceBinding binding902_3 = program701.add(newSequenceBinding(sequence902, 3));
    SequenceBinding binding902_4 = program701.add(newSequenceBinding(sequence902, 4));
    program701.add(newSequenceBinding(sequence902, 5));
    program701.add(newSequenceBindingMeme(binding902_0, "Gravel"));
    program701.add(newSequenceBindingMeme(binding902_1, "Gravel"));
    program701.add(newSequenceBindingMeme(binding902_2, "Gravel"));
    program701.add(newSequenceBindingMeme(binding902_3, "Rocks"));
    program701.add(newSequenceBindingMeme(binding902_1, "Fuzz"));
    program701.add(newSequenceBindingMeme(binding902_2, "Fuzz"));
    program701.add(newSequenceBindingMeme(binding902_3, "Fuzz"));
    program701.add(newSequenceBindingMeme(binding902_4, "Noise"));
    //
    Program program35 = newProgram(35, 101, 25, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, now());
    program35.add(newProgramMeme("Basic"));
    Voice voiceDrums = program35.add(newVoice(InstrumentType.Percussive, "Drums"));
    Sequence sequence35a = program35.add(newSequence(16, "Base", 0.5, "C", 110.3));
    Pattern pattern35a1 = program35.add(newPattern(sequence35a, voiceDrums, PatternType.Loop, 4, "Drop"));
    program35.add(newEvent(pattern35a1, program35.add(newTrack(voiceDrums, "CLOCK")), 0.0, 1.0, "C2", 1.0));
    program35.add(newEvent(pattern35a1, program35.add(newTrack(voiceDrums, "SNORT")), 1.0, 1.0, "G5", 0.8));
    program35.add(newEvent(pattern35a1, program35.add(newTrack(voiceDrums, "KICK")), 2.5, 1.0, "C2", 0.6));
    program35.add(newEvent(pattern35a1, program35.add(newTrack(voiceDrums, "SNARL")), 3.0, 1.0, "G5", 0.9));
    Pattern pattern35a2 = program35.add(newPattern(sequence35a, voiceDrums, PatternType.Loop, 4, "Drop Alt"));
    program35.add(newEvent(pattern35a2, program35.add(newTrack(voiceDrums, "CLACK")), 0.0, 1.0, "B5", 0.9));
    program35.add(newEvent(pattern35a2, program35.add(newTrack(voiceDrums, "SNARN")), 1.0, 1.0, "D2", 1.0));
    program35.add(newEvent(pattern35a2, program35.add(newTrack(voiceDrums, "CLICK")), 2.5, 1.0, "E4", 0.7));
    program35.add(newEvent(pattern35a2, program35.add(newTrack(voiceDrums, "SNAP")), 3.0, 1.0, "c3", 0.5));
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
}
