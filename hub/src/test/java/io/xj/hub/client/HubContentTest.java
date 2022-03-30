// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.client;

import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.*;
import io.xj.hub.tables.pojos.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HubContentTest {
  private HubContent subject;
  private ProgramSequencePatternEvent program702_pattern901_boomEvent;

  @Before
  public void setUp() throws Exception {
    // account
    Account account1 = buildAccount("testing");

    // Library content all created at this known time
    Library library10000001 = buildLibrary(account1, "leaves");

    // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
    Template template1 = buildTemplate(account1, "test", UUID.randomUUID().toString());
    TemplateBinding templateBinding1 = buildTemplateBinding(template1, library10000001);

    // Instrument 201
    Instrument instrument201 = buildInstrument(library10000001, InstrumentType.Drum, InstrumentMode.NoteEvent, InstrumentState.Published, "808 Drums");
    InstrumentMeme instrument201_meme0 = buildInstrumentMeme(instrument201, "Ants");
    InstrumentAudio instrument201_audio402 = buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

    // Program 701, main-type, has sequence with chords, bound to many offsets
    Program program701 = buildProgram(library10000001, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4f, 0.6f);
    ProgramMeme program701_meme0 = buildProgramMeme(program701, "Ants");
    ProgramSequence sequence902 = buildProgramSequence(program701, (short) 16, "decay", 0.25f, "F#");
    ProgramSequenceChord sequence902_chord0 = buildProgramSequenceChord(sequence902, 0.0, "G minor");
    ProgramSequenceBinding binding902_0 = buildProgramSequenceBinding(sequence902, 0);
    ProgramSequenceBinding sequence902_binding0 = buildProgramSequenceBinding(sequence902, 5);
    ProgramSequenceBindingMeme sequence902_binding0_meme0 = buildProgramSequenceBindingMeme(binding902_0, "Gravel");

    // Program 702, beat-type, has unbound sequence with pattern with events
    Program program702 = buildProgram(library10000001, ProgramType.Beat, ProgramState.Published, "coconuts", "F#", 110.3f, 0.6f);
    ProgramMeme program702_meme0 = buildProgramMeme(program702, "Ants");
    ProgramVoice program702_voice1 = buildProgramVoice(program702, InstrumentType.Drum, "Drums");
    ProgramSequence sequence702a = buildProgramSequence(program702, (short) 16, "Base", 0.5f, "C");
    ProgramSequencePattern pattern901 = buildProgramSequencePattern(sequence702a, program702_voice1, (short) 16, "growth");
    ProgramVoiceTrack trackBoom = buildProgramVoiceTrack(program702_voice1, "BOOM");
    ProgramVoiceTrack trackSmack = buildProgramVoiceTrack(program702_voice1, "BOOM");
    program702_pattern901_boomEvent = buildProgramSequencePatternEvent(pattern901, trackBoom, 0.0f, 1.0f, "C", 1.0f);

    // ingest all content
    subject = new HubContent(List.of(
      library10000001,
      template1,
      templateBinding1,
      instrument201,
      instrument201_meme0,
      instrument201_audio402,
      program701,
      program701_meme0,
      sequence902,
      sequence902_chord0,
      binding902_0,
      sequence902_binding0,
      sequence902_binding0_meme0,
      program702,
      program702_meme0,
      program702_voice1,
      sequence702a,
      pattern901,
      trackBoom,
      trackSmack,
      program702_pattern901_boomEvent
    ));
  }

  @Test
  public void getInstrumentTypeForEvent() throws HubClientException {
    assertEquals(InstrumentType.Drum, subject.getInstrumentTypeForEvent(program702_pattern901_boomEvent));
  }
}
