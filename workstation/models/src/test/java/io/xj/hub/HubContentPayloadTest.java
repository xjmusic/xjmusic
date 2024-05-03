// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HubContentPayloadTest extends ContentTest {
  HubContentPayload subject;

  @BeforeEach
  public void setUp() throws Exception {
    // project
    project1 = buildProject();

    // Library content all created at this known time
    library1 = buildLibrary(project1);

    // Templates: enhanced preview chain creation for artists in Lab UI https://github.com/xjmusic/workstation/issues/205
    template1 = buildTemplate(project1, "test1", UUID.randomUUID().toString());
    template1_binding = buildTemplateBinding(template1, library1);
    template2 = buildTemplate(project1, "test2", UUID.randomUUID().toString());

    // Instrument 201
    instrument1 = buildInstrument(library1, InstrumentType.Drum, InstrumentMode.Event, "808 Drums");
    instrument1_meme = buildInstrumentMeme(instrument1, "Ants");
    instrument1_audio = buildInstrumentAudio(instrument1, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

    // Instrument 301
    instrument2 = buildInstrument(library1, InstrumentType.Pad, InstrumentMode.Chord, "Pad");
    instrument2_meme = buildInstrumentMeme(instrument2, "Peanuts");
    instrument2_audio = buildInstrumentAudio(instrument2, "Chord Fm", "a0b9fg73k107s74kf9b4h8d9e009f7-g0e73982.wav", 0.02f, 1.123f, 140.0f, 0.52f, "BING", "F,A,C", 0.9f);

    // Program 701, main-type, has sequence with chords, bound to many offsets
    program1 = buildProgram(library1, ProgramType.Main, "leaves", "C#", 120.4f);
    program1_meme = buildProgramMeme(program1, "Ants");
    program1_voice = buildProgramVoice(program1, InstrumentType.Stripe, "Birds");
    program1_sequence = buildProgramSequence(program1, (short) 8, "decay", 0.25f, "F#");
    program1_sequence_chord0 = buildProgramSequenceChord(program1_sequence, 0.0, "G minor");
    program1_sequence_chord1 = buildProgramSequenceChord(program1_sequence, 2.0, "A minor");
    program1_sequence_chord0_voicing0 = buildProgramSequenceChordVoicing(program1_sequence_chord0, program1_voice, "G");
    program1_sequence_chord1_voicing1 = buildProgramSequenceChordVoicing(program1_sequence_chord1, program1_voice, "Bb");
    program1_sequence_binding1 = buildProgramSequenceBinding(program1_sequence, 0);
    program1_sequence_binding2 = buildProgramSequenceBinding(program1_sequence, 5);
    program1_sequence_binding1_meme1 = buildProgramSequenceBindingMeme(program1_sequence_binding1, "Gravel");
    program1_sequence_binding1_meme2 = buildProgramSequenceBindingMeme(program1_sequence_binding1, "Road");

    // Program 702, beat-type, has unbound sequence with pattern with events
    program2 = buildProgram(library1, ProgramType.Beat, "coconuts", "F#", 110.3f);
    program2_meme = buildProgramMeme(program2, "Bells");
    program2_voice = buildProgramVoice(program2, InstrumentType.Drum, "Drums");
    program2_sequence = buildProgramSequence(program2, (short) 16, "Base", 0.5f, "C");
    program2_sequence_pattern1 = buildProgramSequencePattern(program2_sequence, program2_voice, (short) 16, "growth");
    program2_sequence_pattern2 = buildProgramSequencePattern(program2_sequence, program2_voice, (short) 12, "decay");
    program2_voice_track1 = buildProgramVoiceTrack(program2_voice, "BOOM");
    program2_voice_track2 = buildProgramVoiceTrack(program2_voice, "SMACK");
    program2_sequence_pattern1_event1 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track1, 0.0f, 1.0f, "C", 1.0f);
    program2_sequence_pattern1_event2 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track2, 0.5f, 1.1f, "D", 0.9f);

    // add all content to the payload
    subject = new HubContentPayload();
    subject.setProjects(List.of(project1));
    subject.setLibraries(List.of(library1));
    subject.setTemplates(List.of(template1, template2));
    subject.setTemplateBindings(List.of(template1_binding));
    subject.setInstruments(List.of(instrument1, instrument2));
    subject.setInstrumentMemes(List.of(instrument1_meme, instrument2_meme));
    subject.setInstrumentAudios(List.of(instrument1_audio, instrument2_audio));
    subject.setPrograms(List.of(program1, program2));
    subject.setProgramMemes(List.of(program1_meme, program2_meme));
    subject.setProgramVoices(List.of(program1_voice, program2_voice));
    subject.setProgramVoiceTracks(List.of(program2_voice_track1, program2_voice_track2));
    subject.setProgramSequences(List.of(program2_sequence, program1_sequence));
    subject.setProgramSequenceBindings(List.of(program1_sequence_binding1, program1_sequence_binding2));
    subject.setProgramSequenceBindingMemes(List.of(program1_sequence_binding1_meme1, program1_sequence_binding1_meme2));
    subject.setProgramSequenceChords(List.of(program1_sequence_chord0, program1_sequence_chord1));
    subject.setProgramSequenceChordVoicings(List.of(program1_sequence_chord0_voicing0, program1_sequence_chord1_voicing1));
    subject.setProgramSequencePatterns(List.of(program2_sequence_pattern1, program2_sequence_pattern2));
    subject.setProgramSequencePatternEvents(List.of(program2_sequence_pattern1_event1, program2_sequence_pattern1_event2));
    subject.setDemo(true);
  }

  @Test
  void getInstruments() {
    var result = subject.getInstruments();

    assertEquals(2, result.size());
  }

  @Test
  void getInstrumentAudios() {
    var result = subject.getInstrumentAudios();

    assertEquals(2, result.size());
  }

  @Test
  void getInstrumentMemes() {
    var result = subject.getInstrumentMemes();

    assertEquals(2, result.size());
  }

  @Test
  void getPrograms() {
    var result = subject.getPrograms();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramMemes() {
    var result = subject.getProgramMemes();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequences() {
    var result = subject.getProgramSequences();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequenceBindings() {
    var result = subject.getProgramSequenceBindings();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequenceBindingMemes() {
    var result = subject.getProgramSequenceBindingMemes();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequenceChords() {
    var result = subject.getProgramSequenceChords();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequenceChordVoicings() {
    var result = subject.getProgramSequenceChordVoicings();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequencePatterns() {
    var result = subject.getProgramSequencePatterns();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequencePatternEvents() {
    var result = subject.getProgramSequencePatternEvents();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramVoices() {
    var result = subject.getProgramVoices();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramVoiceTracks() {
    var result = subject.getProgramVoiceTracks();

    assertEquals(2, result.size());
  }

  @Test
  void getTemplates() {
    var result = subject.getTemplates();

    assertEquals(2, result.size());
  }

  @Test
  void getTemplateBindings() {
    var result = subject.getTemplateBindings();

    assertEquals(1, result.size());
  }

  @Test
  void getAllEntities() {
    var result = subject.getAllEntities();

    assertEquals(33, result.size());
  }

  @Test
  void getDemo() {
    var result = subject.getDemo();

    assertEquals(true, result);
  }
}
