// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.analysis;

import io.xj.hub.client.HubContent;
import io.xj.hub.enums.*;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.app.Environment;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ReportDrumInstrumentEventsTest {
  private AnalyzeDrumInstrumentEvents subject;
  private Program program2;
  private Instrument instrument1;

  @Before
  public void setUp() throws Exception {
    // account
    Account account1 = buildAccount("testing");

    // Library content all created at this known time
    Library library1 = buildLibrary(account1, "leaves");

    // Templates: enhanced preview chain creation for artists in Lab UI #178457569
    Template template = buildTemplate(buildAccount("Test"), TemplateType.Preview, "Test", "key123");
    TemplateBinding templateBinding_library1 = buildTemplateBinding(template, library1);

    // Instrument 201
    instrument1 = buildInstrument(library1, InstrumentType.Drum, InstrumentMode.NoteEvent, InstrumentState.Published, "808 Drums");
    InstrumentMeme instrument1_meme1 = buildInstrumentMeme(instrument1, "Ants");
    InstrumentAudio instrument1_audio1 = buildInstrumentAudio(instrument1, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "X", 1.0f);

    // Program 701, main-type, has sequence with chords, bound to many offsets
    Program program1 = buildProgram(library1, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4f, 0.6f);
    ProgramMeme program1_meme1 = buildProgramMeme(program1, "Ants");
    ProgramSequence program1_sequence1 = buildProgramSequence(program1, (short) 16, "decay", 0.25f, "F#");
    ProgramSequenceChord program1_sequence1_chord0 = buildProgramSequenceChord(program1_sequence1, 0.0, "G minor");
    ProgramSequenceBinding program1_binding1 = buildProgramSequenceBinding(program1_sequence1, 0);
    ProgramSequenceBinding program1_binding2 = buildProgramSequenceBinding(program1_sequence1, 5);
    ProgramSequenceBindingMeme program1_binding1_meme0 = buildProgramSequenceBindingMeme(program1_binding1, "Gravel");

    // Program 702, beat-type, has unbound sequence with pattern with events
    program2 = buildProgram(library1, ProgramType.Beat, ProgramState.Published, "coconuts", "F#", 110.3f, 0.6f);
    ProgramMeme program2_meme1 = buildProgramMeme(program2, "Ants");
    ProgramVoice program2_voice1 = buildProgramVoice(program2, InstrumentType.Drum, "Drums");
    ProgramSequence program2_sequence1 = buildProgramSequence(program2, (short) 16, "Base", 0.5f, "C");
    ProgramSequencePattern program2_sequence1_pattern1 = buildProgramSequencePattern(program2_sequence1, program2_voice1, (short) 16, "growth");
    ProgramVoiceTrack program2_trackBoom = buildProgramVoiceTrack(program2_voice1, "BOOM");
    ProgramVoiceTrack program2_trackSmack = buildProgramVoiceTrack(program2_voice1, "BOOM");
    ProgramSequencePatternEvent program2_sequence1_pattern1_event1 = buildProgramSequencePatternEvent(program2_sequence1_pattern1, program2_trackBoom, 0.0f, 1.0f, "C", 1.0f);

    List<Object> entities = List.of(
      template,
      templateBinding_library1,
      library1,
      instrument1,
      instrument1_meme1,
      instrument1_audio1,
      program1,
      program1_binding1,
      program1_binding1_meme0,
      program1_binding2,
      program1_meme1,
      program1_sequence1,
      program1_sequence1_chord0,
      program2,
      program2_meme1,
      program2_sequence1,
      program2_sequence1_pattern1,
      program2_sequence1_pattern1_event1,
      program2_trackBoom,
      program2_trackSmack,
      program2_voice1
    );

    subject = new AnalyzeDrumInstrumentEvents(new HubContent(entities), Environment.getDefault());
  }

  @Test
  public void analysis() {
    assertEquals(2, subject.getEventHistogram().size());
    assertArrayEquals(List.of(program2.getId()).toArray(), subject.getEventHistogram().get("BOOM").getProgramIds().toArray());
    assertArrayEquals(List.of(instrument1.getId()).toArray(), subject.getEventHistogram().get("KICK").getInstrumentIds().toArray());
  }
}
