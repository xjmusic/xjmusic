package io.xj.nexus.project;

import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactory;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.hub_client.HubClientFactory;
import io.xj.nexus.hub_client.HubClientFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProjectManagerImplTest {
  private final String pathToProjectFile;
  private final String baseDir;
  private ProjectManager subject;

  @Mock
  HttpClientProvider httpClientProvider;

  public ProjectManagerImplTest() throws URISyntaxException {
    baseDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("project")).toURI()).getAbsolutePath();
    pathToProjectFile = baseDir + File.separator + "test-project.xj";
  }

  @BeforeEach
  void setUp() {
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClientFactory hubClientFactory = new HubClientFactoryImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    subject = new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory, 3, 3);
  }

  @Test
  void openProjectFromLocalFile() {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    assertEquals(UUID.fromString("23bcaded-2186-4697-912e-5f47bae9e9a0"), subject.getProject().orElseThrow().getId());
    assertEquals(1, subject.getContent().getLibraries().size());
    assertEquals(1, subject.getContent().getTemplateBindings().size());
    assertEquals(2, subject.getContent().getInstrumentAudios().size());
    assertEquals(2, subject.getContent().getInstrumentMemes().size());
    assertEquals(2, subject.getContent().getInstruments().size());
    assertEquals(2, subject.getContent().getProgramMemes().size());
    assertEquals(2, subject.getContent().getProgramSequenceBindingMemes().size());
    assertEquals(2, subject.getContent().getProgramSequenceBindings().size());
    assertEquals(2, subject.getContent().getProgramSequencePatternEvents().size());
    assertEquals(2, subject.getContent().getProgramSequencePatterns().size());
    assertEquals(2, subject.getContent().getProgramSequences().size());
    assertEquals(2, subject.getContent().getProgramVoiceTracks().size());
    assertEquals(2, subject.getContent().getProgramVoices().size());
    assertEquals(2, subject.getContent().getPrograms().size());
    assertEquals(2, subject.getContent().getTemplates().size());
  }

  @Test
  void getProjectPathPrefix() {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    assertEquals(baseDir + File.separator, subject.getProjectPathPrefix());
  }

  @Test
  void getProject() {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    assertEquals(UUID.fromString("23bcaded-2186-4697-912e-5f47bae9e9a0"), subject.getProject().orElseThrow().getId());
  }

  @Test
  void getAudioBaseUrl() {
    subject.cloneProjectFromDemoTemplate("https://audio.test.xj.io/", "test", "test", "test");

    assertEquals("https://audio.test.xj.io/", subject.getAudioBaseUrl());
  }

  @Test
  void closeProject() {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    subject.closeProject();

    assertNull(subject.getContent());
  }

  @Test
  void createTemplate() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var result = subject.createTemplate("Test Template");

    assertEquals("Test Template", subject.getContent().getTemplate(result.getId()).orElseThrow().getName());
  }

  @Test
  void createLibrary() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var result = subject.createLibrary("Test Library");

    assertEquals("Test Library", subject.getContent().getLibrary(result.getId()).orElseThrow().getName());
  }

  @Test
  void createProgram() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));
    var library = subject.getContent().getLibraries().stream().findFirst().orElseThrow();

    var result = subject.createProgram(library, "Test Program");

    assertEquals("Test Program", subject.getContent().getProgram(result.getId()).orElseThrow().getName());
  }

  @Test
  void createInstrument() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));
    var library = subject.getContent().getLibraries().stream().findFirst().orElseThrow();

    var result = subject.createInstrument(library, "Test Instrument");

    assertEquals("Test Instrument", subject.getContent().getInstrument(result.getId()).orElseThrow().getName());
  }

  @Test
  void moveProgram() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));
    var otherLibrary = subject.createLibrary("Other Library");

    subject.moveProgram(UUID.fromString("28d2208b-0a5f-44d5-9096-cc4157c36fb3"), otherLibrary.getId());

    var result = subject.getContent().getProgram(UUID.fromString("28d2208b-0a5f-44d5-9096-cc4157c36fb3"));
    assertEquals(otherLibrary.getId(), result.orElseThrow().getLibraryId());
  }

  @Test
  void moveInstrument() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));
    var otherLibrary = subject.createLibrary("Other Library");

    subject.moveInstrument(UUID.fromString("5cd9560b-e577-4f71-b263-ccf604b3bb30"), otherLibrary.getId());

    var result = subject.getContent().getInstrument(UUID.fromString("5cd9560b-e577-4f71-b263-ccf604b3bb30"));
    assertEquals(otherLibrary.getId(), result.orElseThrow().getLibraryId());
  }

  @Test
  void cloneTemplate() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var clone = subject.cloneTemplate(UUID.fromString("6cfd24de-dc92-436e-9a7e-def8c9e2d351"), "Cloned Template");

    assertEquals(1, subject.getContent().getBindingsOfTemplate(clone.getId()).size());
  }

  @Test
  void cloneLibrary() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var clone = subject.cloneLibrary(UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "Cloned Library");

    assertEquals("Cloned Library", subject.getContent().getLibrary(clone.getId()).orElseThrow().getName());
    // 2 programs
    assertEquals(2, subject.getContent().getProgramsOfLibrary(clone.getId()).size());
    var programs = subject.getContent().getProgramsOfLibrary(clone.getId()).stream().sorted(Comparator.comparing(Program::getName)).toList();
    var program1 = programs.get(0);
    assertEquals("Copy of coconuts", program1.getName());
    var program2 = programs.get(1);
    assertEquals("Copy of leaves", program2.getName());
    // 1 meme per program
    assertEquals(1, subject.getContent().getMemesOfProgram(program1.getId()).size());
    var program1_meme = subject.getContent().getMemesOfProgram(program1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Bells", program1_meme.getName());
    assertEquals(1, subject.getContent().getMemesOfProgram(program2.getId()).size());
    var program2_meme = subject.getContent().getMemesOfProgram(program2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Ants", program2_meme.getName());
    // 1 voice per program
    assertEquals(1, subject.getContent().getVoicesOfProgram(program1.getId()).size());
    var program1_voice = subject.getContent().getVoicesOfProgram(program1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Drums", program1_voice.getName());
    assertEquals(1, subject.getContent().getVoicesOfProgram(program2.getId()).size());
    var program2_voice = subject.getContent().getVoicesOfProgram(program2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Birds", program2_voice.getName());
    // 2 tracks in first voice, 0 in the second
    assertEquals(2, subject.getContent().getTracksOfVoice(program1_voice.getId()).size());
    var program1_voice_tracks = subject.getContent().getTracksOfVoice(program1_voice.getId()).stream().sorted(Comparator.comparing(ProgramVoiceTrack::getName)).toList();
    var program1_voice_track1 = program1_voice_tracks.get(0);
    var program1_voice_track2 = program1_voice_tracks.get(1);
    assertEquals("BOOM", program1_voice_track1.getName());
    assertEquals("SMACK", program1_voice_track2.getName());
    assertEquals(0, subject.getContent().getTracksOfVoice(program2_voice.getId()).size());
    // 1 sequence per program
    assertEquals(1, subject.getContent().getSequencesOfProgram(program1.getId()).size());
    var program1_sequence = subject.getContent().getSequencesOfProgram(program1.getId()).stream().findFirst().orElseThrow();
    assertEquals("base", program1_sequence.getName());
    assertEquals(1, subject.getContent().getSequencesOfProgram(program2.getId()).size());
    var program2_sequence = subject.getContent().getSequencesOfProgram(program2.getId()).stream().findFirst().orElseThrow();
    assertEquals("decay", program2_sequence.getName());
    // 2 patterns in first sequence, 0 in the second
    var program1_sequence_patterns = subject.getContent().getPatternsOfSequence(program1_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequencePattern::getName)).toList();
    assertEquals(2, program1_sequence_patterns.size());
    var program1_sequence_pattern1 = program1_sequence_patterns.get(0);
    var program1_sequence_pattern2 = program1_sequence_patterns.get(1);
    assertEquals(2, program1_sequence_patterns.size());
    assertEquals("decay", program1_sequence_pattern1.getName());
    assertEquals("growth", program1_sequence_pattern2.getName());
    assertEquals(0, subject.getContent().getPatternsOfSequence(program2_sequence.getId()).size());
    // 0 events in first pattern, 2 in the second
    assertEquals(0, subject.getContent().getEventsOfPattern(program1_sequence_pattern1.getId()).size());
    var program1_sequence_pattern2_events = subject.getContent().getEventsOfPattern(program1_sequence_pattern2.getId()).stream().sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition)).toList();
    assertEquals(2, program1_sequence_pattern2_events.size());
    var program1_sequence_pattern2_event1 = program1_sequence_pattern2_events.get(0);
    assertEquals(0.0f, program1_sequence_pattern2_event1.getPosition());
    assertEquals("C", program1_sequence_pattern2_event1.getTones());
    assertEquals(program1_voice_track1.getId(), program1_sequence_pattern2_event1.getProgramVoiceTrackId());
    var program1_sequence_pattern2_event2 = program1_sequence_pattern2_events.get(1);
    assertEquals(0.5f, program1_sequence_pattern2_event2.getPosition());
    assertEquals("D", program1_sequence_pattern2_event2.getTones());
    assertEquals(program1_voice_track2.getId(), program1_sequence_pattern2_event2.getProgramVoiceTrackId());
    // 2 bindings in first sequence, 0 in the second
    var program2_sequence_bindings = subject.getContent().getBindingsOfSequence(program2_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequenceBinding::getOffset)).toList();
    assertEquals(2, program2_sequence_bindings.size());
    var program2_sequence_binding1 = program2_sequence_bindings.get(0);
    var program2_sequence_binding2 = program2_sequence_bindings.get(1);
    assertEquals(2, program2_sequence_bindings.size());
    assertEquals(0, program2_sequence_binding1.getOffset());
    assertEquals(5, program2_sequence_binding2.getOffset());
    assertEquals(0, subject.getContent().getBindingsOfSequence(program1_sequence.getId()).size());
    // 0 memes in first binding, 2 in the second
    assertEquals(0, subject.getContent().getMemesOfSequenceBinding(program2_sequence_binding2.getId()).size());
    var program2_sequence_binding1_memes = subject.getContent().getMemesOfSequenceBinding(program2_sequence_binding1.getId()).stream().sorted(Comparator.comparing(ProgramSequenceBindingMeme::getName)).toList();
    assertEquals(2, program2_sequence_binding1_memes.size());
    var program2_sequence_binding1_meme1 = program2_sequence_binding1_memes.get(0);
    assertEquals("Gravel", program2_sequence_binding1_meme1.getName());
    var program2_sequence_binding1_meme2 = program2_sequence_binding1_memes.get(1);
    assertEquals("Road", program2_sequence_binding1_meme2.getName());
    // 0 chords in first sequence, 2 in the second
    assertEquals(0, subject.getContent().getChordsOfSequence(program1_sequence.getId()).size());
    var program2_sequence_chords = subject.getContent().getChordsOfSequence(program2_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequenceChord::getName)).toList();
    assertEquals(2, program2_sequence_chords.size());
    var program2_sequence_chord1 = program2_sequence_chords.get(0);
    var program2_sequence_chord2 = program2_sequence_chords.get(1);
    assertEquals(2, program2_sequence_chords.size());
    assertEquals("A minor", program2_sequence_chord1.getName());
    assertEquals("G minor", program2_sequence_chord2.getName());
    // 0 voicings in first chord, 2 in the second
    assertEquals(0, subject.getContent().getVoicingsOfChord(program2_sequence_chord1.getId()).size());
    var program2_sequence_chord2_voicings = subject.getContent().getVoicingsOfChord(program2_sequence_chord2.getId()).stream().sorted(Comparator.comparing(ProgramSequenceChordVoicing::getNotes)).toList();
    assertEquals(2, program2_sequence_chord2_voicings.size());
    var program2_sequence_chord2_voicing1 = program2_sequence_chord2_voicings.get(0);
    assertEquals("Bb", program2_sequence_chord2_voicing1.getNotes());
    var program2_sequence_chord2_voicing2 = program2_sequence_chord2_voicings.get(1);
    assertEquals("G", program2_sequence_chord2_voicing2.getNotes());
    // 2 instruments
    assertEquals(2, subject.getContent().getInstrumentsOfLibrary(clone.getId()).size());
    var instruments = subject.getContent().getInstrumentsOfLibrary(clone.getId()).stream().sorted(Comparator.comparing(Instrument::getName)).toList();
    var instrument1 = instruments.get(0);
    assertEquals("Copy of 808 Drums", instrument1.getName());
    var instrument2 = instruments.get(1);
    assertEquals("Copy of Pad", instrument2.getName());
    // 1 meme per instrument
    assertEquals(1, subject.getContent().getMemesOfInstrument(instrument1.getId()).size());
    var instrument1_meme = subject.getContent().getMemesOfInstrument(instrument1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Ants", instrument1_meme.getName());
    assertEquals(1, subject.getContent().getMemesOfInstrument(instrument2.getId()).size());
    var instrument2_meme = subject.getContent().getMemesOfInstrument(instrument2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Peanuts", instrument2_meme.getName());
    // 1 audio per instrument
    assertEquals(1, subject.getContent().getAudiosOfInstrument(instrument1.getId()).size());
    var instrument1_audio = subject.getContent().getAudiosOfInstrument(instrument1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Chords Cm to D", instrument1_audio.getName());
    assertEquals(1, subject.getContent().getAudiosOfInstrument(instrument2.getId()).size());
    var instrument2_audio = subject.getContent().getAudiosOfInstrument(instrument2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Chord Fm", instrument2_audio.getName());
  }

  @Test
  void cloneProgram() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var clone1 = subject.cloneProgram(UUID.fromString("7ad65895-27b8-453d-84f1-ef2a2a2f09eb"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"),"Cloned Program 1");
    var clone2 = subject.cloneProgram(UUID.fromString("28d2208b-0a5f-44d5-9096-cc4157c36fb3"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"),"Cloned Program 2");

    // 2 programs
    var program1 = subject.getContent().getProgram(clone1.getId()).orElseThrow();
    assertEquals("Cloned Program 1", program1.getName());
    var program2 = subject.getContent().getProgram(clone2.getId()).orElseThrow();
    assertEquals("Cloned Program 2", program2.getName());
    // 1 meme per program
    assertEquals(1, subject.getContent().getMemesOfProgram(program1.getId()).size());
    var program1_meme = subject.getContent().getMemesOfProgram(program1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Bells", program1_meme.getName());
    assertEquals(1, subject.getContent().getMemesOfProgram(program2.getId()).size());
    var program2_meme = subject.getContent().getMemesOfProgram(program2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Ants", program2_meme.getName());
    // 1 voice per program
    assertEquals(1, subject.getContent().getVoicesOfProgram(program1.getId()).size());
    var program1_voice = subject.getContent().getVoicesOfProgram(program1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Drums", program1_voice.getName());
    assertEquals(1, subject.getContent().getVoicesOfProgram(program2.getId()).size());
    var program2_voice = subject.getContent().getVoicesOfProgram(program2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Birds", program2_voice.getName());
    // 2 tracks in first voice, 0 in the second
    assertEquals(2, subject.getContent().getTracksOfVoice(program1_voice.getId()).size());
    var program1_voice_tracks = subject.getContent().getTracksOfVoice(program1_voice.getId()).stream().sorted(Comparator.comparing(ProgramVoiceTrack::getName)).toList();
    var program1_voice_track1 = program1_voice_tracks.get(0);
    var program1_voice_track2 = program1_voice_tracks.get(1);
    assertEquals("BOOM", program1_voice_track1.getName());
    assertEquals("SMACK", program1_voice_track2.getName());
    assertEquals(0, subject.getContent().getTracksOfVoice(program2_voice.getId()).size());
    // 1 sequence per program
    assertEquals(1, subject.getContent().getSequencesOfProgram(program1.getId()).size());
    var program1_sequence = subject.getContent().getSequencesOfProgram(program1.getId()).stream().findFirst().orElseThrow();
    assertEquals("base", program1_sequence.getName());
    assertEquals(1, subject.getContent().getSequencesOfProgram(program2.getId()).size());
    var program2_sequence = subject.getContent().getSequencesOfProgram(program2.getId()).stream().findFirst().orElseThrow();
    assertEquals("decay", program2_sequence.getName());
    // 2 patterns in first sequence, 0 in the second
    var program1_sequence_patterns = subject.getContent().getPatternsOfSequence(program1_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequencePattern::getName)).toList();
    assertEquals(2, program1_sequence_patterns.size());
    var program1_sequence_pattern1 = program1_sequence_patterns.get(0);
    var program1_sequence_pattern2 = program1_sequence_patterns.get(1);
    assertEquals(2, program1_sequence_patterns.size());
    assertEquals("decay", program1_sequence_pattern1.getName());
    assertEquals("growth", program1_sequence_pattern2.getName());
    assertEquals(0, subject.getContent().getPatternsOfSequence(program2_sequence.getId()).size());
    // 0 events in first pattern, 2 in the second
    assertEquals(0, subject.getContent().getEventsOfPattern(program1_sequence_pattern1.getId()).size());
    var program1_sequence_pattern2_events = subject.getContent().getEventsOfPattern(program1_sequence_pattern2.getId()).stream().sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition)).toList();
    assertEquals(2, program1_sequence_pattern2_events.size());
    var program1_sequence_pattern2_event1 = program1_sequence_pattern2_events.get(0);
    assertEquals(0.0f, program1_sequence_pattern2_event1.getPosition());
    assertEquals("C", program1_sequence_pattern2_event1.getTones());
    assertEquals(program1_voice_track1.getId(), program1_sequence_pattern2_event1.getProgramVoiceTrackId());
    var program1_sequence_pattern2_event2 = program1_sequence_pattern2_events.get(1);
    assertEquals(0.5f, program1_sequence_pattern2_event2.getPosition());
    assertEquals("D", program1_sequence_pattern2_event2.getTones());
    assertEquals(program1_voice_track2.getId(), program1_sequence_pattern2_event2.getProgramVoiceTrackId());
    // 2 bindings in first sequence, 0 in the second
    var program2_sequence_bindings = subject.getContent().getBindingsOfSequence(program2_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequenceBinding::getOffset)).toList();
    assertEquals(2, program2_sequence_bindings.size());
    var program2_sequence_binding1 = program2_sequence_bindings.get(0);
    var program2_sequence_binding2 = program2_sequence_bindings.get(1);
    assertEquals(2, program2_sequence_bindings.size());
    assertEquals(0, program2_sequence_binding1.getOffset());
    assertEquals(5, program2_sequence_binding2.getOffset());
    assertEquals(0, subject.getContent().getBindingsOfSequence(program1_sequence.getId()).size());
    // 0 memes in first binding, 2 in the second
    assertEquals(0, subject.getContent().getMemesOfSequenceBinding(program2_sequence_binding2.getId()).size());
    var program2_sequence_binding1_memes = subject.getContent().getMemesOfSequenceBinding(program2_sequence_binding1.getId()).stream().sorted(Comparator.comparing(ProgramSequenceBindingMeme::getName)).toList();
    assertEquals(2, program2_sequence_binding1_memes.size());
    var program2_sequence_binding1_meme1 = program2_sequence_binding1_memes.get(0);
    assertEquals("Gravel", program2_sequence_binding1_meme1.getName());
    var program2_sequence_binding1_meme2 = program2_sequence_binding1_memes.get(1);
    assertEquals("Road", program2_sequence_binding1_meme2.getName());
    // 0 chords in first sequence, 2 in the second
    assertEquals(0, subject.getContent().getChordsOfSequence(program1_sequence.getId()).size());
    var program2_sequence_chords = subject.getContent().getChordsOfSequence(program2_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequenceChord::getName)).toList();
    assertEquals(2, program2_sequence_chords.size());
    var program2_sequence_chord1 = program2_sequence_chords.get(0);
    var program2_sequence_chord2 = program2_sequence_chords.get(1);
    assertEquals(2, program2_sequence_chords.size());
    assertEquals("A minor", program2_sequence_chord1.getName());
    assertEquals("G minor", program2_sequence_chord2.getName());
    // 0 voicings in first chord, 2 in the second
    assertEquals(0, subject.getContent().getVoicingsOfChord(program2_sequence_chord1.getId()).size());
    var program2_sequence_chord2_voicings = subject.getContent().getVoicingsOfChord(program2_sequence_chord2.getId()).stream().sorted(Comparator.comparing(ProgramSequenceChordVoicing::getNotes)).toList();
    assertEquals(2, program2_sequence_chord2_voicings.size());
    var program2_sequence_chord2_voicing1 = program2_sequence_chord2_voicings.get(0);
    assertEquals("Bb", program2_sequence_chord2_voicing1.getNotes());
    var program2_sequence_chord2_voicing2 = program2_sequence_chord2_voicings.get(1);
    assertEquals("G", program2_sequence_chord2_voicing2.getNotes());
  }

  @Test
  void cloneProgramSequence() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var clone1 = subject.cloneProgramSequence(UUID.fromString("2ff1c4e0-0f45-4457-900d-c7efef699e86"), "Cloned Sequence 1");
    var clone2 = subject.cloneProgramSequence(UUID.fromString("d1be946c-ea2c-4f74-a9df-18a659b99fc8"), "Cloned Sequence 2");

    // 1 sequence per program
    var program1_sequence = subject.getContent().getProgramSequence(clone1.getId()).stream().findFirst().orElseThrow();
    var program2_sequence = subject.getContent().getProgramSequence(clone2.getId()).stream().findFirst().orElseThrow();
    // 2 patterns in first sequence, 0 in the second
    var program1_sequence_patterns = subject.getContent().getPatternsOfSequence(program1_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequencePattern::getName)).toList();
    assertEquals(2, program1_sequence_patterns.size());
    var program1_sequence_pattern1 = program1_sequence_patterns.get(0);
    var program1_sequence_pattern2 = program1_sequence_patterns.get(1);
    assertEquals(2, program1_sequence_patterns.size());
    assertEquals("decay", program1_sequence_pattern1.getName());
    assertEquals("growth", program1_sequence_pattern2.getName());
    assertEquals(0, subject.getContent().getPatternsOfSequence(program2_sequence.getId()).size());
    // Tracks same as original program 1 (none in program 2)
    var program1_voice_tracks = subject.getContent().getTracksOfProgram(UUID.fromString("7ad65895-27b8-453d-84f1-ef2a2a2f09eb")).stream().sorted(Comparator.comparing(ProgramVoiceTrack::getName)).toList();
    var program1_voice_track1 = program1_voice_tracks.get(0);
    var program1_voice_track2 = program1_voice_tracks.get(1);
    // 0 events in first pattern, 2 in the second
    assertEquals(0, subject.getContent().getEventsOfPattern(program1_sequence_pattern1.getId()).size());
    var program1_sequence_pattern2_events = subject.getContent().getEventsOfPattern(program1_sequence_pattern2.getId()).stream().sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition)).toList();
    assertEquals(2, program1_sequence_pattern2_events.size());
    var program1_sequence_pattern2_event1 = program1_sequence_pattern2_events.get(0);
    assertEquals(0.0f, program1_sequence_pattern2_event1.getPosition());
    assertEquals("C", program1_sequence_pattern2_event1.getTones());
    assertEquals(program1_voice_track1.getId(), program1_sequence_pattern2_event1.getProgramVoiceTrackId());
    var program1_sequence_pattern2_event2 = program1_sequence_pattern2_events.get(1);
    assertEquals(0.5f, program1_sequence_pattern2_event2.getPosition());
    assertEquals("D", program1_sequence_pattern2_event2.getTones());
    assertEquals(program1_voice_track2.getId(), program1_sequence_pattern2_event2.getProgramVoiceTrackId());
    // 0 bindings -- we don't clone the bindings because this would cause duplicate bindings at all the same offsets
    assertEquals(0, subject.getContent().getBindingsOfSequence(program2_sequence.getId()).size());
    assertEquals(0, subject.getContent().getBindingsOfSequence(program1_sequence.getId()).size());
    // 0 chords in first sequence, 2 in the second
    assertEquals(0, subject.getContent().getChordsOfSequence(program1_sequence.getId()).size());
    var program2_sequence_chords = subject.getContent().getChordsOfSequence(program2_sequence.getId()).stream().sorted(Comparator.comparing(ProgramSequenceChord::getName)).toList();
    assertEquals(2, program2_sequence_chords.size());
    var program2_sequence_chord1 = program2_sequence_chords.get(0);
    var program2_sequence_chord2 = program2_sequence_chords.get(1);
    assertEquals(2, program2_sequence_chords.size());
    assertEquals("A minor", program2_sequence_chord1.getName());
    assertEquals("G minor", program2_sequence_chord2.getName());
    // 0 voicings in first chord, 2 in the second
    assertEquals(0, subject.getContent().getVoicingsOfChord(program2_sequence_chord1.getId()).size());
    var program2_sequence_chord2_voicings = subject.getContent().getVoicingsOfChord(program2_sequence_chord2.getId()).stream().sorted(Comparator.comparing(ProgramSequenceChordVoicing::getNotes)).toList();
    assertEquals(2, program2_sequence_chord2_voicings.size());
    var program2_sequence_chord2_voicing1 = program2_sequence_chord2_voicings.get(0);
    assertEquals("Bb", program2_sequence_chord2_voicing1.getNotes());
    var program2_sequence_chord2_voicing2 = program2_sequence_chord2_voicings.get(1);
    assertEquals("G", program2_sequence_chord2_voicing2.getNotes());
  }

  @Test
  void cloneProgramSequencePattern() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var clone1 = subject.cloneProgramSequencePattern(UUID.fromString("0457d46f-5d6c-495e-a3e8-8bb9740cee18"), "Cloned Sequence Pattern 1");
    var clone2 = subject.cloneProgramSequencePattern(UUID.fromString("9ed7fbaa-540e-4539-b886-8788caf3dbff"), "Cloned Sequence Pattern 2");

    var program1_sequence_pattern1 = subject.getContent().getProgramSequencePattern(clone1.getId()).orElseThrow();
    var program1_sequence_pattern2 = subject.getContent().getProgramSequencePattern(clone2.getId()).orElseThrow();
    assertEquals("Cloned Sequence Pattern 1", program1_sequence_pattern1.getName());
    assertEquals("Cloned Sequence Pattern 2", program1_sequence_pattern2.getName());
    // Tracks same as original program 1 (none in program 2)
    var program1_voice_tracks = subject.getContent().getTracksOfProgram(UUID.fromString("7ad65895-27b8-453d-84f1-ef2a2a2f09eb")).stream().sorted(Comparator.comparing(ProgramVoiceTrack::getName)).toList();
    var program1_voice_track1 = program1_voice_tracks.get(0);
    var program1_voice_track2 = program1_voice_tracks.get(1);
    // 0 events in first pattern, 2 in the second
    assertEquals(0, subject.getContent().getEventsOfPattern(program1_sequence_pattern1.getId()).size());
    var program1_sequence_pattern2_events = subject.getContent().getEventsOfPattern(program1_sequence_pattern2.getId()).stream().sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition)).toList();
    assertEquals(2, program1_sequence_pattern2_events.size());
    var program1_sequence_pattern2_event1 = program1_sequence_pattern2_events.get(0);
    assertEquals(0.0f, program1_sequence_pattern2_event1.getPosition());
    assertEquals("C", program1_sequence_pattern2_event1.getTones());
    assertEquals(program1_voice_track1.getId(), program1_sequence_pattern2_event1.getProgramVoiceTrackId());
    var program1_sequence_pattern2_event2 = program1_sequence_pattern2_events.get(1);
    assertEquals(0.5f, program1_sequence_pattern2_event2.getPosition());
    assertEquals("D", program1_sequence_pattern2_event2.getTones());
    assertEquals(program1_voice_track2.getId(), program1_sequence_pattern2_event2.getProgramVoiceTrackId());
  }

  @Test
  void cloneInstrument() throws Exception {
    assertTrue(subject.openProjectFromLocalFile(pathToProjectFile));

    var clone1 = subject.cloneInstrument(UUID.fromString("9097d757-ae8f-4d68-b449-8ec96602ca83"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"),"Cloned Instrument 1");
    var clone2 = subject.cloneInstrument(UUID.fromString("5cd9560b-e577-4f71-b263-ccf604b3bb30"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"),"Cloned Instrument 2");

    // 2 instruments
    var instrument1 = subject.getContent().getInstrument(clone1.getId()).orElseThrow();
    assertEquals("Cloned Instrument 1", instrument1.getName());
    var instrument2 = subject.getContent().getInstrument(clone2.getId()).orElseThrow();
    assertEquals("Cloned Instrument 2", instrument2.getName());
    // 1 meme per instrument
    assertEquals(1, subject.getContent().getMemesOfInstrument(instrument1.getId()).size());
    var instrument1_meme = subject.getContent().getMemesOfInstrument(instrument1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Ants", instrument1_meme.getName());
    assertEquals(1, subject.getContent().getMemesOfInstrument(instrument2.getId()).size());
    var instrument2_meme = subject.getContent().getMemesOfInstrument(instrument2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Peanuts", instrument2_meme.getName());
    // 1 audio per instrument
    assertEquals(1, subject.getContent().getAudiosOfInstrument(instrument1.getId()).size());
    var instrument1_audio = subject.getContent().getAudiosOfInstrument(instrument1.getId()).stream().findFirst().orElseThrow();
    assertEquals("Chords Cm to D", instrument1_audio.getName());
    assertEquals(1, subject.getContent().getAudiosOfInstrument(instrument2.getId()).size());
    var instrument2_audio = subject.getContent().getAudiosOfInstrument(instrument2.getId()).stream().findFirst().orElseThrow();
    assertEquals("Chord Fm", instrument2_audio.getName());
  }
}
