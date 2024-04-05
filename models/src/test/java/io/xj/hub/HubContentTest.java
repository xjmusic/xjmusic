// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.ProgramSequencePatternEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HubContentTest extends ContentTest {
  HubContent subject;

  @BeforeEach
  public void setUp() throws Exception {
    // user
    user1 = buildUser();

    // project
    project1 = buildProject();
    project1_user = buildProjectUser(project1, user1);

    // Library content all created at this known time
    library1 = buildLibrary(project1);

    // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
    template1 = buildTemplate(project1, "test1", UUID.randomUUID().toString());
    template1_binding = buildTemplateBinding(template1, library1);
    template2 = buildTemplate(project1, "test2", UUID.randomUUID().toString());

    // Instrument 1
    instrument1 = buildInstrument(library1, InstrumentType.Drum, InstrumentMode.Event, "808 Drums");
    instrument1_meme = buildInstrumentMeme(instrument1, "Ants");
    instrument1_audio = buildInstrumentAudio(instrument1, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

    // Instrument 2
    instrument2 = buildInstrument(library1, InstrumentType.Pad, InstrumentMode.Chord, "Pad");
    instrument2_meme = buildInstrumentMeme(instrument2, "Peanuts");
    instrument2_audio = buildInstrumentAudio(instrument2, "Chord Fm", "a0b9fg73k107s74kf9b4h8d9e009f7-g0e73982.wav", 0.02f, 1.123f, 140.0f, 0.52f, "BING", "F,A,C", 0.9f);

    // Program 1, main-type, has sequence with chords, bound to many offsets
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

    // Program 2, beat-type, has unbound sequence with pattern with events
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

    // ingest all content
    subject = new HubContent(List.of(
      program1_sequence_binding1,
      instrument1,
      instrument1_audio,
      instrument1_meme,
      instrument2,
      instrument2_audio,
      instrument2_meme,
      library1,
      program2_sequence_pattern1,
      program2_sequence_pattern2,
      program1,
      program1_meme,
      program1_voice,
      program2,
      program2_meme,
      program2_sequence_pattern1_event1,
      program2_sequence_pattern1_event2,
      program2_voice,
      project1,
      project1_user,
      program2_sequence,
      program1_sequence,
      program1_sequence_binding2,
      program1_sequence_binding1_meme1,
      program1_sequence_binding1_meme2,
      program1_sequence_chord0,
      program1_sequence_chord0_voicing0,
      program1_sequence_chord1_voicing1,
      program1_sequence_chord1,
      template1,
      template2,
      template1_binding,
      program2_voice_track1,
      program2_voice_track2,
      user1
    ), false);

    // test error
    subject.addError(new Error("test"));
  }

  @Test
  public void getInstrumentTypeOfEvent() {
    assertEquals(InstrumentType.Drum, subject.getInstrumentTypeOfEvent(program2_sequence_pattern1_event1));
  }

  @Test
  public void hasInstrumentsOfMode() {
    assertFalse(subject.hasInstrumentsOfMode(InstrumentMode.Loop));
    assertTrue(subject.hasInstrumentsOfMode(InstrumentMode.Event));
  }

  @Test
  public void hasInstrumentsOfType() {
    assertFalse(subject.hasInstrumentsOfType(InstrumentType.Bass));
    assertFalse(subject.hasInstrumentsOfType(InstrumentType.Hook));
    assertFalse(subject.hasInstrumentsOfType(InstrumentType.Percussion));
    assertFalse(subject.hasInstrumentsOfType(InstrumentType.Stab));
    assertFalse(subject.hasInstrumentsOfType(InstrumentType.Sticky));
    assertFalse(subject.hasInstrumentsOfType(InstrumentType.Stripe));
    assertTrue(subject.hasInstrumentsOfType(InstrumentType.Drum));
    assertTrue(subject.hasInstrumentsOfType(InstrumentType.Pad));
  }

  @Test
  public void hasInstrumentsOfTypeAndMode() {
    assertTrue(subject.hasInstrumentsOfTypeAndMode(InstrumentType.Drum, InstrumentMode.Event));
    assertFalse(subject.hasInstrumentsOfTypeAndMode(InstrumentType.Drum, InstrumentMode.Loop));
  }

  @Test
  public void serialize_deserialize() throws JsonProcessingException {
    final ObjectMapper mapper = new ObjectMapper();

    var serialized = mapper.writeValueAsString(subject);
    var deserialized = mapper.readValue(serialized, HubContent.class);

    assertEquals(subject.toString(), deserialized.toString());
  }

  @Test
  void from() {
    var payload = new HubContentPayload()
      .setDemo(true)
      .setPrograms(List.of(program1, program2))
      .setProgramMemes(List.of(program1_meme, program2_meme))
      .setProgramSequences(List.of(program2_sequence, program1_sequence))
      .setProgramSequenceBindings(List.of(program1_sequence_binding1, program1_sequence_binding2))
      .setProgramSequenceBindingMemes(List.of(program1_sequence_binding1_meme1, program1_sequence_binding1_meme2))
      .setProgramSequenceChords(List.of(program1_sequence_chord0, program1_sequence_chord1))
      .setProgramSequenceChordVoicings(List.of(program1_sequence_chord0_voicing0, program1_sequence_chord1_voicing1))
      .setProgramSequencePatterns(List.of(program2_sequence_pattern1, program2_sequence_pattern2))
      .setProgramSequencePatternEvents(List.of(program2_sequence_pattern1_event1, program2_sequence_pattern1_event2))
      .setProgramVoices(List.of(program1_voice, program2_voice))
      .setProgramVoiceTracks(List.of(program2_voice_track1, program2_voice_track2))
      .setInstruments(List.of(instrument1, instrument2))
      .setInstrumentAudios(List.of(instrument1_audio, instrument2_audio))
      .setInstrumentMemes(List.of(instrument1_meme, instrument2_meme))
      .setLibraries(List.of(library1))
      .setTemplates(List.of(template1, template2))
      .setTemplateBindings(List.of(template1_binding))
      .setProjects(List.of(project1));

    var result = HubContent.from(payload);

    assertEquals(33, result.size());
    assertTrue(result.getDemo());
  }

  @Test
  void getAvailableOffsets() {
    var result = subject.getAvailableOffsets(program1_sequence_binding1);

    assertEquals(2, result.size());
  }

  @Test
  void getAudiosOfInstrumentId() {
    var result = subject.getAudiosOfInstrument(instrument1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getAudiosOfInstrument() {
    var result = subject.getAudiosOfInstrument(instrument2);

    assertEquals(1, result.size());
  }

  @Test
  void getBindingsOfSequence() {
    var result = subject.getBindingsOfSequence(program1_sequence);

    assertEquals(2, result.size());
  }

  @Test
  void getBindingsOfSequenceId() {
    var result = subject.getBindingsOfSequence(program1_sequence.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getSequenceBindingMemesOfProgram() {
    var result = subject.getSequenceBindingMemesOfProgram(program1);

    assertEquals(2, result.size());
  }

  @Test
  void getSequenceBindingMemesOfProgramId() {
    var result = subject.getSequenceBindingMemesOfProgram(program1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getBindingsAtOffsetOfProgram() {
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1, 0, false).size());
    assertEquals(0, subject.getBindingsAtOffsetOfProgram(program1, 1, false).size());
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1, 0, true).size());
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1, 1, true).size());
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1, 2, true).size());
  }

  @Test
  void getBindingsAtOffsetOfProgramId() {
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1.getId(), 0, false).size());
    assertEquals(0, subject.getBindingsAtOffsetOfProgram(program1.getId(), 1, false).size());
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1.getId(), 0, true).size());
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1.getId(), 1, true).size());
    assertEquals(1, subject.getBindingsAtOffsetOfProgram(program1.getId(), 2, true).size());
  }

  @Test
  void getChordsOfSequence() {
    var result = subject.getChordsOfSequence(program1_sequence);

    assertEquals(2, result.size());
  }

  @Test
  void getChordsOfSequenceId() {
    var result = subject.getChordsOfSequence(program1_sequence.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getEventsOfPattern() {
    var result = subject.getEventsOfPattern(program2_sequence_pattern1);

    assertEquals(2, result.size());
  }

  @Test
  void getEventsOfPatternId() {
    var result = subject.getEventsOfPattern(program2_sequence_pattern1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getEventsOfTrack() {
    var result = subject.getEventsOfTrack(program2_voice_track1);

    assertEquals(1, result.size());
  }

  @Test
  void getEventsOfTrackId() {
    var result = subject.getEventsOfTrack(program2_voice_track1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getEventsOfPatternAndTrack() {
    var result = subject.getEventsOfPatternAndTrack(program2_sequence_pattern1, program2_voice_track1);

    assertEquals(1, result.size());
  }

  @Test
  void getEventsOfPatternAndTrackId() {
    var result = subject.getEventsOfPatternAndTrack(program2_sequence_pattern1.getId(), program2_voice_track1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getInstrument() {
    var result = subject.getInstrument(instrument1.getId());

    assertEquals(instrument1, result.orElseThrow());
  }

  @Test
  void getInstrumentAudio() {
    var result = subject.getInstrumentAudio(instrument1_audio.getId());

    assertEquals(instrument1_audio, result.orElseThrow());
  }

  @Test
  void getAudiosOfInstrumentTypesAndModes() {
    var result = subject.getAudiosOfInstrumentTypesAndModes(List.of(InstrumentType.Drum), List.of(InstrumentMode.Event));

    assertEquals(1, result.size());
  }

  @Test
  void getAudiosOfInstrumentTypes() {
    var result = subject.getAudiosOfInstrumentTypes(List.of(InstrumentType.Drum));

    assertEquals(1, result.size());
  }

  @Test
  void getInstrumentAudios() {
    var result = subject.getInstrumentAudios();

    assertEquals(2, result.size());
  }

  @Test
  void getMemesOfInstrumentId() {
    var result = subject.getMemesOfInstrument(instrument1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getInstrumentMemes() {
    var result = subject.getInstrumentMemes();

    assertEquals(2, result.size());
  }

  @Test
  void getInstruments() {
    var result = subject.getInstruments();

    assertEquals(2, result.size());
  }

  @Test
  void getUsers() {
    var result = subject.getUsers();

    assertEquals(1, result.size());
    assertEquals(user1.getId(), result.stream().findFirst().orElseThrow().getId());
  }

  @Test
  void getProjectUsers() {
    var result = subject.getProjectUsers();

    assertEquals(1, result.size());
    assertEquals(project1_user.getId(), result.stream().findFirst().orElseThrow().getId());
  }

  @Test
  void getInstrumentsOfLibrary() {
    var result = subject.getInstrumentsOfLibrary(library1);

    assertEquals(2, result.size());
  }

  @Test
  void getInstrumentsOfLibraryId() {
    var result = subject.getInstrumentsOfLibrary(library1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getInstrumentsOfTypes() {
    var result = subject.getInstrumentsOfTypes(List.of(InstrumentType.Drum));

    assertEquals(1, result.size());
  }

  @Test
  void getInstrumentTypeOfAudioId() {
    var result = subject.getInstrumentTypeOfAudio(instrument1_audio.getId());

    assertEquals(InstrumentType.Drum, result);
  }

  @Test
  void getMemesOfProgramId() {
    var result = subject.getMemesOfProgram(program1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getMemesAtBeginning() {
    var result = subject.getMemesAtBeginning(program1);

    assertEquals(3, result.size());
  }

  @Test
  void getPatternIdOfEventId() {
    var result = subject.getPatternIdOfEvent(program2_sequence_pattern1_event1.getId());

    assertEquals(program2_sequence_pattern1.getId(), result);
  }

  @Test
  void getPatternsOfSequence() {
    var result = subject.getPatternsOfSequence(program2_sequence);

    assertEquals(2, result.size());
  }

  @Test
  void getPatternsOfSequenceId() {
    var result = subject.getPatternsOfSequence(program2_sequence.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getPatternsOfVoice() {
    var result = subject.getPatternsOfVoice(program2_voice);

    assertEquals(2, result.size());
  }

  @Test
  void getPatternsOfVoiceId() {
    var result = subject.getPatternsOfVoice(program2_voice.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getProgram() {
    var result = subject.getProgram(program1.getId());

    assertEquals(program1, result.orElseThrow());
  }

  @Test
  void getPrograms() {
    var result = subject.getPrograms();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramsOfLibrary() {
    var result = subject.getProgramsOfLibrary(library1);

    assertEquals(2, result.size());
  }

  @Test
  void getProgramsOfLibraryId() {
    var result = subject.getProgramsOfLibrary(library1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getProgramMemes() {
    var result = subject.getProgramMemes();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequence() {
    var result = subject.getProgramSequence(program1_sequence.getId());

    assertEquals(program1_sequence, result.orElseThrow());
  }

  @Test
  void getSequenceOfBinding() {
    var result = subject.getSequenceOfBinding(program1_sequence_binding1);

    assertEquals(program1_sequence, result.orElseThrow());
  }

  @Test
  void getProgramSequences() {
    var result = subject.getProgramSequences();

    assertEquals(2, result.size());
  }

  @Test
  void getSequencesOfProgramId() {
    var result = subject.getSequencesOfProgram(program1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getProgramSequenceBinding() {
    var result = subject.getProgramSequenceBinding(program1_sequence_binding1.getId());

    assertEquals(program1_sequence_binding1, result.orElseThrow());
  }

  @Test
  void getProgramSequenceBindings() {
    var result = subject.getProgramSequenceBindings();

    assertEquals(2, result.size());
  }

  @Test
  void getSequenceBindingsOfProgram() {
    var result = subject.getSequenceBindingsOfProgram(program1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequenceBindingMemes() {
    var result = subject.getProgramSequenceBindingMemes();

    assertEquals(2, result.size());
  }

  @Test
  void getMemesOfSequenceBinding() {
    var result = subject.getMemesOfSequenceBinding(program1_sequence_binding1);

    assertEquals(2, result.size());
  }

  @Test
  void getMemesOfSequenceBindingId() {
    var result = subject.getMemesOfSequenceBinding(program1_sequence_binding1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequencePattern() {
    var result = subject.getProgramSequencePattern(program2_sequence_pattern1.getId());

    assertEquals(program2_sequence_pattern1, result.orElseThrow());
  }

  @Test
  void getProgramSequencePatterns() {
    var result = subject.getProgramSequencePatterns();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequencePatternEvent() {
    var result = subject.getProgramSequencePatternEvent(program2_sequence_pattern1_event1.getId());

    assertEquals(program2_sequence_pattern1_event1, result.orElseThrow());
  }

  @Test
  void getProgramSequencePatternEvents() {
    var result = subject.getProgramSequencePatternEvents();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequenceChord() {
    var result = subject.getProgramSequenceChord(program1_sequence_chord0.getId());

    assertEquals(program1_sequence_chord0, result.orElseThrow());
  }

  @Test
  void getProgramSequenceChords() {
    var result = subject.getProgramSequenceChords();

    assertEquals(2, result.size());
  }

  @Test
  void testGetProgramSequenceChords() {
    var result = subject.getSequenceChordsOfProgram(program1_sequence.getProgramId());

    assertEquals(2, result.size());
  }

  @Test
  void getProgramSequenceChordVoicings() {
    var result = subject.getProgramSequenceChordVoicings();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramVoice() {
    var result = subject.getProgramVoice(program1_voice.getId());

    assertEquals(program1_voice, result.orElseThrow());
  }

  @Test
  void getProgramVoices() {
    var result = subject.getProgramVoices();

    assertEquals(2, result.size());
  }

  @Test
  void getProgramVoiceTrack() {
    var result = subject.getProgramVoiceTrack(program2_voice_track1.getId());

    assertEquals(program2_voice_track1, result.orElseThrow());
  }

  @Test
  void getProgramVoiceTracks() {
    var result = subject.getProgramVoiceTracks();

    assertEquals(2, result.size());
  }

  @Test
  void getTracksOfProgramId() {
    var result = subject.getTracksOfProgram(program2.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getTracksOfVoice() {
    var result = subject.getTracksOfVoice(program2_voice);

    assertEquals(2, result.size());
  }

  @Test
  void getTracksOfVoiceId() {
    var result = subject.getTracksOfVoice(program2_voice.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getTemplates() {
    var result = subject.getTemplates();

    assertEquals(2, result.size());
  }

  @Test
  void getTemplate() {
    var result = subject.getTemplate(template1.getId());

    assertEquals(template1, result.orElseThrow());
  }

  @Test
  void getTemplateBindings() {
    var result = subject.getTemplateBindings();

    assertEquals(1, result.size());
  }

  @Test
  void getBindingsOfTemplate() {
    var result = subject.getBindingsOfTemplate(template1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getLibrary() {
    var result = subject.getLibrary(library1.getId());

    assertEquals(library1, result.orElseThrow());
  }

  @Test
  void getLibraries() {
    var result = subject.getLibraries();

    assertEquals(1, result.size());
  }

  @Test
  void getProject() {
    var result = subject.getProject();

    assertEquals(project1, result);
  }

  /**
   HubContent has proper method to delete any entity of id
   https://www.pivotaltracker.com/story/show/186986806
   */
  @Test
  void delete() {
    subject.delete(Program.class, program1.getId());

    assertFalse(subject.getProgram(program1.getId()).isPresent());
  }

  @Test
  void getTrackOfEvent() {
    var result = subject.getTrackOfEvent(program2_sequence_pattern1_event1);

    assertEquals(program2_voice_track1, result.orElseThrow());
  }

  @Test
  void getTrackNames() {
    var result = subject.getTrackNamesOfVoice(program2_voice);

    assertEquals(2, result.size());
  }

  @Test
  void getVoicingsOfChord() {
    var result = subject.getVoicingsOfChord(program1_sequence_chord0);

    assertEquals(1, result.size());
  }

  @Test
  void getVoicingsOfChordId() {
    var result = subject.getVoicingsOfChord(program1_sequence_chord0.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getSequenceChordVoicingsOfProgram() {
    var result = subject.getSequenceChordVoicingsOfProgram(program1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getVoicingsOfChordAndVoice() {
    var result = subject.getVoicingsOfChordAndVoice(program1_sequence_chord0, program1_voice);

    assertEquals(1, result.size());
  }

  @Test
  void getVoicingsOfChordIdAndVoiceId() {
    var result = subject.getVoicingsOfChordAndVoice(program1_sequence_chord0.getId(), program1_voice.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getVoiceOfEvent() {
    var result = subject.getVoiceOfEvent(program2_sequence_pattern1_event1);

    assertEquals(program2_voice, result.orElseThrow());
  }

  @Test
  void getVoicesOfProgram() {
    var result = subject.getVoicesOfProgram(program2);

    assertEquals(1, result.size());
  }

  @Test
  void getVoicesOfProgramId() {
    var result = subject.getVoicesOfProgram(program2.getId());

    assertEquals(1, result.size());
  }

  @Test
  void put() {
    var event = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track1, 2.0f, 1.5f, "C", 1.0f);

    subject.put(event);

    assertTrue(subject.getProgramSequencePatternEvent(event.getId()).isPresent());
  }

  @Test
  void update() throws Exception {
    // method returns true if update changed a value
    assertTrue(subject.update(ProgramSequencePatternEvent.class, program2_sequence_pattern1_event1.getId(), "tones", "C#, D#, F"));

    // same method returns false because update did not change any value
    assertFalse(subject.update(ProgramSequencePatternEvent.class, program2_sequence_pattern1_event1.getId(), "tones", "C#, D#, F"));

    var result = subject.getProgramSequencePatternEvent(program2_sequence_pattern1_event1.getId()).orElseThrow();

    assertEquals("C#, D#, F", result.getTones());
  }

  @Test
  void clear() {
    subject.clear();

    assertEquals(0, subject.size());
    assertEquals(0, subject.getErrors().size());
  }

  @Test
  void putAll() {
    var event1 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track1, 2.0f, 1.5f, "C", 1.0f);
    var event2 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track1, 2.0f, 1.5f, "C", 1.0f);

    subject.putAll(List.of(event1, event2));

    assertTrue(subject.getProgramSequencePatternEvent(event1.getId()).isPresent());
    assertTrue(subject.getProgramSequencePatternEvent(event2.getId()).isPresent());
  }

  @Test
  void size() {
    assertEquals(35, subject.size());
  }

  @Test
  void get() {
    assertTrue(subject.get(ProgramSequencePatternEvent.class, program2_sequence_pattern1_event1.getId()).isPresent());
  }

  @Test
  void getAll() {
    assertEquals(2, subject.getAll(Program.class).size());
  }

  @Test
  void setInstruments() throws Exception {
    subject.setInstruments(List.of());

    assertTrue(subject.getInstruments().isEmpty());
  }

  @Test
  void setUsers() throws Exception {
    subject.setUsers(List.of());

    assertTrue(subject.getUsers().isEmpty());
  }

  @Test
  void setProjectUsers() throws Exception {
    subject.setProjectUsers(List.of());

    assertTrue(subject.getProjectUsers().isEmpty());
  }

  @Test
  void setInstrumentAudios() throws Exception {
    subject.setInstrumentAudios(List.of());

    assertTrue(subject.getInstrumentAudios().isEmpty());
  }

  @Test
  void setInstrumentMemes() throws Exception {
    subject.setInstrumentMemes(List.of());

    assertTrue(subject.getInstrumentMemes().isEmpty());
  }

  @Test
  void setLibraries() throws Exception {
    subject.setLibraries(List.of());

    assertTrue(subject.getLibraries().isEmpty());
  }

  @Test
  void setPrograms() throws Exception {
    subject.setPrograms(List.of());

    assertTrue(subject.getPrograms().isEmpty());
  }

  @Test
  void setProgramMemes() throws Exception {
    subject.setProgramMemes(List.of());

    assertTrue(subject.getProgramMemes().isEmpty());
  }

  @Test
  void setProgramSequences() throws Exception {
    subject.setProgramSequences(List.of());

    assertTrue(subject.getProgramSequences().isEmpty());
  }

  @Test
  void setProgramSequenceBindings() throws Exception {
    subject.setProgramSequenceBindings(List.of());

    assertTrue(subject.getProgramSequenceBindings().isEmpty());
  }

  @Test
  void setProgramSequenceBindingMemes() throws Exception {
    subject.setProgramSequenceBindingMemes(List.of());

    assertTrue(subject.getProgramSequenceBindingMemes().isEmpty());
  }

  @Test
  void setProgramSequenceChords() throws Exception {
    subject.setProgramSequenceChords(List.of());

    assertTrue(subject.getProgramSequenceChords().isEmpty());
  }

  @Test
  void setProgramSequenceChordVoicings() throws Exception {
    subject.setProgramSequenceChordVoicings(List.of());

    assertTrue(subject.getProgramSequenceChordVoicings().isEmpty());
  }

  @Test
  void setProgramSequencePatterns() throws Exception {
    subject.setProgramSequencePatterns(List.of());

    assertTrue(subject.getProgramSequencePatterns().isEmpty());
  }

  @Test
  void setProgramSequencePatternEvents() throws Exception {
    subject.setProgramSequencePatternEvents(List.of());

    assertTrue(subject.getProgramSequencePatternEvents().isEmpty());
  }

  @Test
  void setProgramVoices() throws Exception {
    subject.setProgramVoices(List.of());

    assertTrue(subject.getProgramVoices().isEmpty());
  }

  @Test
  void setProgramVoiceTracks() throws Exception {
    subject.setProgramVoiceTracks(List.of());

    assertTrue(subject.getProgramVoiceTracks().isEmpty());
  }

  @Test
  void setProject() {
    var project2 = buildProject();
    subject.setProject(project2);

    assertEquals(subject.getProject().getId(), project2.getId());
  }

  /**
   For reverse compatibility, HubContent can deserialize a payload with multiple projects- it takes the first one
   */
  @Test
  void setProjects() {
    var project2 = buildProject();
    subject.setProjects(List.of(project2));

    assertEquals(subject.getProject().getId(), project2.getId());
  }

  @Test
  void setTemplates() throws Exception {
    subject.setTemplates(List.of());

    assertTrue(subject.getTemplates().isEmpty());
  }

  @Test
  void setTemplateBindings() throws Exception {
    subject.setTemplateBindings(List.of());

    assertTrue(subject.getTemplateBindings().isEmpty());
  }

  @Test
  void addError() {
    var content = subject.addError(new Error("test"));

    Collection<Error> result = content.getErrors();

    assertEquals(2, result.size());
  }

  @Test
  void getErrors() {
    Collection<Error> result = subject.getErrors();

    assertEquals(1, result.size());
  }

  @Test
  void setErrors() {
    subject.setErrors(List.of(new Error("test"), new Error("test"), new Error("test")));

    Collection<Error> result = subject.getErrors();

    assertEquals(3, result.size());
  }

  @Test
  void getSequencePatternEventsOfProgram() {
    var result = subject.getSequencePatternEventsOfProgram(program2.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getEventsOfProgramId() {
    var result = subject.getSequencePatternEventsOfProgram(program2_sequence_pattern1.getProgramId());

    assertEquals(2, result.size());
  }

  @Test
  void getSequencePatternsOfProgram() {
    var result = subject.getSequencePatternsOfProgram(program2);

    assertEquals(2, result.size());
  }

  @Test
  void getPatternsOfProgramId() {
    var result = subject.getSequencePatternsOfProgram(program2_sequence_pattern1.getProgramId());

    assertEquals(2, result.size());
  }

  @Test
  void getInstrumentsOfType() {
    var result = subject.getInstrumentsOfType(InstrumentType.Drum);

    assertEquals(1, result.size());
  }

  @Test
  void getInstrumentsOfTypesAndModes() {
    var result = subject.getInstrumentsOfTypesAndModes(List.of(InstrumentType.Drum), List.of(InstrumentMode.Event));

    assertEquals(1, result.size());
  }

  @Test
  void getInstrumentTypeOfAudio() {
    var result = subject.getInstrumentTypeOfAudio(instrument1_audio.getId());

    assertEquals(InstrumentType.Drum, result);
  }

  @Test
  void getMemesOfInstrument() {
    var result = subject.getMemesOfInstrument(instrument1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getPatternIdOfEvent() {
    var result = subject.getPatternIdOfEvent(program2_sequence_pattern1_event1.getId());

    assertEquals(program2_sequence_pattern1.getId(), result);
  }

  @Test
  void getProgramsOfType() {
    var result = subject.getProgramsOfType(ProgramType.Main);

    assertEquals(1, result.size());
  }

  @Test
  void getMemesOfProgram() {
    var result = subject.getMemesOfProgram(program1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getSequencesOfProgram() {
    var result = subject.getSequencesOfProgram(program1.getId());

    assertEquals(1, result.size());
  }

  @Test
  void getSequenceChordsOfProgram() {
    var result = subject.getSequenceChordsOfProgram(program1.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getTracksOfProgram() {
    var result = subject.getTracksOfProgram(program2.getId());

    assertEquals(2, result.size());
  }

  @Test
  void getTracksOfProgramType() {
    var result = subject.getTracksOfProgramType(ProgramType.Beat);

    assertEquals(2, result.size());
  }

  @Test
  void getTemplateBinding() {
    var result = subject.getTemplateBinding(template1_binding.getId());

    assertEquals(template1_binding, result.orElseThrow());
  }

  @Test
  void getTrackNamesOfVoice() {
    var result = subject.getTrackNamesOfVoice(program2_voice);

    assertEquals(2, result.size());
  }

  @Test
  void getInstrumentMeme() {
    var result = subject.getInstrumentMeme(instrument1_meme.getId());

    assertEquals(instrument1_meme, result.orElseThrow());
  }

  @Test
  void getProgramMeme() {
    var result = subject.getProgramMeme(program1_meme.getId());

    assertEquals(program1_meme, result.orElseThrow());
  }

  @Test
  void getProgramSequenceBindingMeme() {
    var result = subject.getProgramSequenceBindingMeme(program1_sequence_binding1_meme1.getId());

    assertEquals(program1_sequence_binding1_meme1, result.orElseThrow());
  }

  @Test
  void getProgramSequenceChordVoicing() {
    var result = subject.getProgramSequenceChordVoicing(program1_sequence_chord0_voicing0.getId());

    assertEquals(program1_sequence_chord0_voicing0, result.orElseThrow());
  }

  @Test
  void setAll() throws Exception {
    subject.setAll(Program.class, List.of());

    assertTrue(subject.getAll(Program.class).isEmpty());
  }

  @Test
  void getPatternsOfSequenceAndVoice() {
    assertEquals(2, subject.getPatternsOfSequenceAndVoice(program2_sequence.getId(), program2_voice.getId()).size());
    assertEquals(0, subject.getPatternsOfSequenceAndVoice(UUID.randomUUID(), program2_voice.getId()).size());
    assertEquals(0, subject.getPatternsOfSequenceAndVoice(program2_sequence.getId(), UUID.randomUUID()).size());
  }

  @Test
  void getDemo() {
    subject.setDemo(false);
    assertFalse(subject.getDemo());
    subject.setDemo(true);
    assertTrue(subject.getDemo());
  }

  @Test
  void forTemplate_boundToLibrary() {
    var result = subject.forTemplate(template1);

    assertEquals(31, result.size());
  }

  @Test
  void forTemplate_boundToProgram() {
    subject.put(buildTemplateBinding(template2, program1));
    var result = subject.forTemplate(template2);

    assertEquals(14, result.size());
  }

  @Test
  void forTemplate_boundToInstrument() {
    subject.put(buildTemplateBinding(template2, instrument1));
    var result = subject.forTemplate(template2);

    assertEquals(5, result.size());
  }

  @Test
  void forTemplate_duplicatesOriginalObjects() {
    var copy = subject.forTemplate(template1);

    copy.getInstrument(instrument1.getId()).orElseThrow().setName("different");
    copy.getInstrumentMeme(instrument1_meme.getId()).orElseThrow().setName("different");
    copy.getInstrumentAudio(instrument1_audio.getId()).orElseThrow().setName("different");
    copy.getProgram(program1.getId()).orElseThrow().setName("different");
    copy.getProgramMeme(program1_meme.getId()).orElseThrow().setName("different");
    copy.getProgramVoice(program1_voice.getId()).orElseThrow().setName("different");
    copy.getProgramSequence(program1_sequence.getId()).orElseThrow().setName("different");
    copy.getProgramSequenceChord(program1_sequence_chord0.getId()).orElseThrow().setName("different");
    copy.getProgramSequenceChordVoicing(program1_sequence_chord0_voicing0.getId()).orElseThrow().setNotes("different");

    assertEquals("original", subject.getInstrument(instrument1.getId()).orElseThrow().getName());
    assertEquals("original", subject.getInstrumentMeme(instrument1_meme.getId()).orElseThrow().getName());
    assertEquals("original", subject.getInstrumentAudio(instrument1_audio.getId()).orElseThrow().getName());
    assertEquals("original", subject.getProgram(program1.getId()).orElseThrow().getName());
    assertEquals("original", subject.getProgramMeme(program1_meme.getId()).orElseThrow().getName());
    assertEquals("original", subject.getProgramVoice(program1_voice.getId()).orElseThrow().getName());
    assertEquals("original", subject.getProgramSequence(program1_sequence.getId()).orElseThrow().getName());
    assertEquals("original", subject.getProgramSequenceChord(program1_sequence_chord0.getId()).orElseThrow().getName());
    assertEquals("original", subject.getProgramSequenceChordVoicing(program1_sequence_chord0_voicing0.getId()).orElseThrow().getNotes());
  }
}
