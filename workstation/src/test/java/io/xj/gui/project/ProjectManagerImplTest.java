package io.xj.gui.project;

import io.xj.engine.ContentFixtures;
import io.xj.model.HubTopology;
import io.xj.model.InstrumentConfig;
import io.xj.model.ProgramConfig;
import io.xj.model.TemplateConfig;
import io.xj.model.entity.EntityFactory;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentState;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramState;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramSequenceBinding;
import io.xj.model.pojos.ProgramSequenceBindingMeme;
import io.xj.model.pojos.ProgramSequenceChord;
import io.xj.model.pojos.ProgramSequenceChordVoicing;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoiceTrack;
import io.xj.engine.http.HttpClientProvider;
import io.xj.engine.hub_client.HubClientFactory;
import io.xj.engine.hub_client.HubClientFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.xj.engine.ContentFixtures.buildInstrument;
import static io.xj.engine.ContentFixtures.buildProgram;
import static io.xj.engine.ContentFixtures.buildProject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ProjectManagerImplTest {
  private final String pathToProjectFile;
  private final String pathToAudioFile;
  private final String baseDir;
  private ProjectManager subject;

  @Mock
  HttpClientProvider httpClientProvider;

  public ProjectManagerImplTest() throws URISyntaxException {
    baseDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("project")).toURI()).getAbsolutePath();
    pathToProjectFile = baseDir + File.separator + "test-project.xj";
    pathToAudioFile = baseDir + File.separator + "test-audio.wav";
  }

  @BeforeEach
  void setUp() {
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClientFactory hubClientFactory = new HubClientFactoryImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory, 3);
    subject = new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory);
    subject.openProjectFromLocalFile(pathToProjectFile);
  }

  @Test
  void openProjectFromLocalFile() {
    subject.getContent().clear();

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
    assertEquals(baseDir + File.separator, subject.getProjectPathPrefix());
  }

  @Test
  void getProject() {
    assertEquals(UUID.fromString("23bcaded-2186-4697-912e-5f47bae9e9a0"), subject.getProject().orElseThrow().getId());
  }

  @Test
  void getDemoBaseUrl() {
    subject.getContent().clear();
    String tempPath = System.getProperty("java.io.tmpdir");

    subject.createProjectFromDemoTemplate("https://audio.test.xj.io/", "test", tempPath + "test", "test", "1.0.0");

    assertEquals("https://audio.test.xj.io/", subject.getDemoBaseUrl());
  }

  @Test
  void closeProject() {
    subject.closeProject();

    assertNull(subject.getContent());
  }

  @Test
  void createTemplate() throws Exception {
    var result = subject.createTemplate("Test Template");

    assertEquals("Test Template", subject.getContent().getTemplate(result.getId()).orElseThrow().getName());
    var config = new TemplateConfig(result.getConfig());
    assertNotNull(config);
  }

  @Test
  void createLibrary() throws Exception {
    var result = subject.createLibrary("Test Library");

    assertEquals("Test Library", subject.getContent().getLibrary(result.getId()).orElseThrow().getName());
  }

  @Test
  void createProgram() throws Exception {
    var library = subject.getContent().getLibraries().stream().findFirst().orElseThrow();

    var result = subject.createProgram(library, "Test Program");

    assertEquals("Test Program", subject.getContent().getProgram(result.getId()).orElseThrow().getName());
    var config = new ProgramConfig(result.getConfig());
    assertNotNull(config);
    assertNotNull(result.getType());
    assertNotNull(result.getState());
    assertNotNull(result.getKey());
    assertNotNull(result.getTempo());
  }

  /**
   When creating a new Program, source default values from
   1. Program in the same library
   <p>
   Workstation creating new program/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createProgram_attributesFromProgramInSameLibrary() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var program = ContentFixtures.buildProgram(library, ProgramType.Beat, ProgramState.Published, "Lorem Ipsum Program", "C", 120);
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherProgram = ContentFixtures.buildProgram(otherLibrary, ProgramType.Macro, ProgramState.Draft, "Other Program", "D", 130);
    subject.getContent().putAll(List.of(project, library, program, otherLibrary, otherProgram));

    var result = subject.createProgram(library, "Test Program");

    assertEquals(program.getType(), result.getType());
    assertEquals(program.getState(), result.getState());
    assertEquals(program.getKey(), result.getKey());
    assertEquals(program.getTempo(), result.getTempo());
  }

  /**
   When creating a new Program, source default values from the first available
   2. any Programs in the project
   in lieu of
   1. Program in the same library
   <p>
   Workstation creating new program/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createProgram_attributesFromProgramInProject() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherProgram = ContentFixtures.buildProgram(otherLibrary, ProgramType.Macro, ProgramState.Draft, "Other Program", "D", 130);
    subject.getContent().putAll(List.of(project, library, otherLibrary, otherProgram));

    var result = subject.createProgram(library, "Test Program");

    assertEquals(otherProgram.getType(), result.getType());
    assertEquals(otherProgram.getState(), result.getState());
    assertEquals(otherProgram.getKey(), result.getKey());
    assertEquals(otherProgram.getTempo(), result.getTempo());
  }

  @Test
  void createInstrument() throws Exception {
    var library = subject.getContent().getLibraries().stream().findFirst().orElseThrow();

    var result = subject.createInstrument(library, "Test Instrument");

    assertEquals("Test Instrument", subject.getContent().getInstrument(result.getId()).orElseThrow().getName());
    var config = new InstrumentConfig(result.getConfig());
    assertNotNull(config);
    assertNotNull(result.getType());
    assertNotNull(result.getState());
    assertNotNull(result.getMode());
    assertNotNull(result.getVolume());
  }

  /**
   When creating a new Instrument, source default values from the first available
   1. Instrument in the same library
   <p>
   Workstation creating new instrument/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createInstrument_attributesFromInstrumentInSameLibrary() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var instrument = ContentFixtures.buildInstrument(library, InstrumentType.Bass, InstrumentMode.Chord, InstrumentState.Published, "Lorem Ipsum Instrument");
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherInstrument = ContentFixtures.buildInstrument(otherLibrary, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Draft, "Other Instrument");
    subject.getContent().putAll(List.of(project, library, instrument, otherLibrary, otherInstrument));

    var result = subject.createInstrument(library, "Test Instrument");

    assertEquals(instrument.getType(), result.getType());
    assertEquals(instrument.getState(), result.getState());
    assertEquals(instrument.getMode(), result.getMode());
  }

  /**
   When creating a new Instrument, source default values from the first available
   2. any Instruments in the project
   in lieu of
   1. Instrument in the same library
   <p>
   Workstation creating new instrument/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createInstrument_attributesFromInstrumentInProject() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherInstrument = ContentFixtures.buildInstrument(otherLibrary, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Draft, "Other Instrument");
    subject.getContent().putAll(List.of(project, library, otherLibrary, otherInstrument));

    var result = subject.createInstrument(library, "Test Instrument");

    assertEquals(otherInstrument.getType(), result.getType());
    assertEquals(otherInstrument.getState(), result.getState());
    assertEquals(otherInstrument.getMode(), result.getMode());
  }

  @Test
  void createInstrumentAudio() throws Exception {
    var instrument = subject.getContent().getInstruments().stream().findFirst().orElseThrow();

    var result = subject.createInstrumentAudio(instrument, pathToAudioFile);

    assertEquals("test-audio", subject.getContent().getInstrumentAudio(result.getId()).orElseThrow().getName());
    assertEquals("Pad-test-audio-F-A-C.wav", subject.getContent().getInstrumentAudio(result.getId()).orElseThrow().getWaveformKey());
  }

  /**
   When creating a new Instrument Audio, source default values from the first available
   1. Instrument Audios in the same instrument
   2. Instrument Audios in the same library
   3. Programs in the same library
   4. any Instrument Audios in the project
   5. any Programs in the project
   6. defaults
   <p>
   Workstation creating new instrument/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createInstrumentAudio_attributesFromAudioInSameInstrument() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var instrument = ContentFixtures.buildInstrument(library, InstrumentType.Bass, InstrumentMode.Chord, InstrumentState.Published, "Lorem Ipsum Instrument");
    var audio = ContentFixtures.buildInstrumentAudio(instrument, "Lorem Ipsum Audio", "lorem-ipsum.wav", 0, 4, 120, 1, "X", "C4", 1);
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherInstrument = ContentFixtures.buildInstrument(otherLibrary, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Draft, "Other Instrument");
    var otherAudio = ContentFixtures.buildInstrumentAudio(otherInstrument, "Other Audio", "other-audio.wav", 0.1f, 4.2f, 130.0f, 0.9f, "HIT", "D3", 0.9f);
    subject.getContent().putAll(List.of(project, library, instrument, audio, otherLibrary, otherInstrument, otherAudio));

    var result = subject.createInstrumentAudio(instrument, pathToAudioFile);

    assertEquals(audio.getTones(), result.getTones());
    assertEquals(audio.getEvent(), result.getEvent());
    assertEquals(audio.getIntensity(), result.getIntensity());
    assertEquals(audio.getTempo(), result.getTempo());
    assertEquals(audio.getLoopBeats(), result.getLoopBeats());
    assertEquals(audio.getVolume(), result.getVolume());
  }

  /**
   When creating a new Instrument Audio, source default values from the first available
   2. Instrument Audios in the same library
   in lieu of
   1. Instrument Audios in the same instrument
   <p>
   Workstation creating new instrument/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createInstrumentAudio_attributesFromAudioInSameLibrary() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var instrument = ContentFixtures.buildInstrument(library, InstrumentType.Bass, InstrumentMode.Chord, InstrumentState.Published, "Lorem Ipsum Instrument");
    var instrument2 = ContentFixtures.buildInstrument(library, InstrumentType.Bass, InstrumentMode.Chord, InstrumentState.Published, "Lorem Ipsum Instrument");
    var audio = ContentFixtures.buildInstrumentAudio(instrument2, "Lorem Ipsum Audio", "lorem-ipsum.wav", 0, 4, 120, 1, "X", "C4", 1);
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherInstrument = ContentFixtures.buildInstrument(otherLibrary, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Draft, "Other Instrument");
    var otherAudio = ContentFixtures.buildInstrumentAudio(otherInstrument, "Other Audio", "other-audio.wav", 0.1f, 4.2f, 130.0f, 0.9f, "HIT", "D3", 0.9f);
    subject.getContent().putAll(List.of(project, library, instrument, instrument2, audio, otherLibrary, otherInstrument, otherAudio));

    var result = subject.createInstrumentAudio(instrument, pathToAudioFile);

    assertEquals(audio.getTones(), result.getTones());
    assertEquals(audio.getEvent(), result.getEvent());
    assertEquals(audio.getIntensity(), result.getIntensity());
    assertEquals(audio.getTempo(), result.getTempo());
    assertEquals(audio.getLoopBeats(), result.getLoopBeats());
    assertEquals(audio.getVolume(), result.getVolume());
  }

  /**
   When creating a new Instrument Audio, source default values from the first available
   3. Programs in the same library
   in lieu of
   2. Instrument Audios in the same library
   1. Instrument Audios in the same instrument
   <p>
   Workstation creating new instrument/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createInstrumentAudio_tempoFromProgramInSameLibrary() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var instrument = ContentFixtures.buildInstrument(library, InstrumentType.Bass, InstrumentMode.Chord, InstrumentState.Published, "Lorem Ipsum Instrument");
    var program = ContentFixtures.buildProgram(library, ProgramType.Beat, ProgramState.Published, "Lorem Ipsum Program", "C", 150);
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherInstrument = ContentFixtures.buildInstrument(otherLibrary, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Draft, "Other Instrument");
    var otherAudio = ContentFixtures.buildInstrumentAudio(otherInstrument, "Other Audio", "other-audio.wav", 0.1f, 4.2f, 130.0f, 0.9f, "HIT", "D3", 0.9f);
    subject.getContent().putAll(List.of(project, library, instrument, program, otherLibrary, otherInstrument, otherAudio));

    var result = subject.createInstrumentAudio(instrument, pathToAudioFile);

    assertEquals(program.getTempo(), result.getTempo());
  }

  /**
   When creating a new Instrument Audio, source default values from the first available
   4. any Instrument Audios in the project
   in lieu of
   3. Programs in the same library
   2. Instrument Audios in the same library
   1. Instrument Audios in the same instrument
   <p>
   Workstation creating new instrument/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createInstrumentAudio_attributesFromAnyAudio() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var instrument = ContentFixtures.buildInstrument(library, InstrumentType.Bass, InstrumentMode.Chord, InstrumentState.Published, "Lorem Ipsum Instrument");
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherInstrument = ContentFixtures.buildInstrument(otherLibrary, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Draft, "Other Instrument");
    var otherAudio = ContentFixtures.buildInstrumentAudio(otherInstrument, "Other Audio", "other-audio.wav", 0.1f, 4.2f, 130.0f, 0.9f, "HIT", "D3", 0.9f);
    subject.getContent().putAll(List.of(project, library, instrument, otherLibrary, otherInstrument, otherAudio));

    var result = subject.createInstrumentAudio(instrument, pathToAudioFile);

    assertEquals(otherAudio.getTones(), result.getTones());
    assertEquals(otherAudio.getEvent(), result.getEvent());
    assertEquals(otherAudio.getIntensity(), result.getIntensity());
    assertEquals(otherAudio.getTempo(), result.getTempo());
    assertEquals(otherAudio.getLoopBeats(), result.getLoopBeats());
    assertEquals(otherAudio.getVolume(), result.getVolume());
  }

  /**
   When creating a new Instrument Audio, source default values from the first available
   5. any Programs in the project
   in lieu of
   4. any Instrument Audios in the project
   3. Programs in the same library
   2. Instrument Audios in the same library
   1. Instrument Audios in the same instrument
   <p>
   Workstation creating new instrument/instrument, populates with defaults
   https://github.com/xjmusic/xjmusic/issues/277
   */
  @Test
  void createInstrumentAudio_attributesFromAnyProgram() throws Exception {
    subject.getContent().clear();
    var project = ContentFixtures.buildProject("Testing");
    var library = ContentFixtures.buildLibrary(project, "Test Library");
    var instrument = ContentFixtures.buildInstrument(library, InstrumentType.Bass, InstrumentMode.Chord, InstrumentState.Published, "Lorem Ipsum Instrument");
    var otherLibrary = ContentFixtures.buildLibrary(project, "Other Library");
    var otherProgram = ContentFixtures.buildProgram(otherLibrary, ProgramType.Beat, ProgramState.Published, "Lorem Ipsum Program", "C", 150);
    subject.getContent().putAll(List.of(project, library, instrument, otherProgram, otherLibrary));

    var result = subject.createInstrumentAudio(instrument, pathToAudioFile);

    assertEquals(otherProgram.getTempo(), result.getTempo());
  }

  @Test
  void moveProgram() throws Exception {
    var otherLibrary = subject.createLibrary("Other Library");

    subject.moveProgram(UUID.fromString("28d2208b-0a5f-44d5-9096-cc4157c36fb3"), otherLibrary.getId());

    var result = subject.getContent().getProgram(UUID.fromString("28d2208b-0a5f-44d5-9096-cc4157c36fb3"));
    assertEquals(otherLibrary.getId(), result.orElseThrow().getLibraryId());
  }

  @Test
  void moveInstrument() throws Exception {
    var otherLibrary = subject.createLibrary("Other Library");

    subject.moveInstrument(UUID.fromString("5cd9560b-e577-4f71-b263-ccf604b3bb30"), otherLibrary.getId());

    var result = subject.getContent().getInstrument(UUID.fromString("5cd9560b-e577-4f71-b263-ccf604b3bb30"));
    assertEquals(otherLibrary.getId(), result.orElseThrow().getLibraryId());
  }

  /**
   Duplicating a Template, should use provided name https://github.com/xjmusic/xjmusic/issues/342
   */
  @Test
  void duplicateTemplate() throws Exception {
    var duplicate = subject.duplicateTemplate(UUID.fromString("6cfd24de-dc92-436e-9a7e-def8c9e2d351"), "Duplicated Template");

    assertEquals("Duplicated Template", duplicate.getName());
    assertEquals(1, subject.getContent().getBindingsOfTemplate(duplicate.getId()).size());
  }

  /**
   Duplicating a Template, should enumerate to next available unique name https://github.com/xjmusic/xjmusic/issues/342
   */
  @Test
  void duplicateTemplate_enumeratesToNextUniqueName() throws Exception {
    var duplicate = subject.duplicateTemplate(UUID.fromString("6cfd24de-dc92-436e-9a7e-def8c9e2d351"), "test1");

    assertEquals("test1 2", subject.getContent().getTemplate(duplicate.getId()).orElseThrow().getName());
  }

  @Test
  void duplicateLibrary() throws Exception {
    var duplicate = subject.duplicateLibrary(UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "Duplicated Library");

    assertEquals("Duplicated Library", subject.getContent().getLibrary(duplicate.getId()).orElseThrow().getName());
    // 2 programs
    assertEquals(2, subject.getContent().getProgramsOfLibrary(duplicate.getId()).size());
    var programs = subject.getContent().getProgramsOfLibrary(duplicate.getId()).stream().sorted(Comparator.comparing(Program::getName)).toList();
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
    assertEquals(2, subject.getContent().getInstrumentsOfLibrary(duplicate.getId()).size());
    var instruments = subject.getContent().getInstrumentsOfLibrary(duplicate.getId()).stream().sorted(Comparator.comparing(Instrument::getName)).toList();
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

  /**
   Duplicating a Library, should enumerate to next available unique name https://github.com/xjmusic/xjmusic/issues/342
   */
  @Test
  void duplicateLibrary_enumeratesToNextUniqueName() throws Exception {
    var duplicate = subject.duplicateLibrary(UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "leaves");

    assertEquals("leaves 2", subject.getContent().getLibrary(duplicate.getId()).orElseThrow().getName());
  }

  @Test
  void duplicateProgram() throws Exception {
    var duplicate1 = subject.duplicateProgram(UUID.fromString("7ad65895-27b8-453d-84f1-ef2a2a2f09eb"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "Duplicated Program 1");
    var duplicate2 = subject.duplicateProgram(UUID.fromString("28d2208b-0a5f-44d5-9096-cc4157c36fb3"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "Duplicated Program 2");

    // 2 programs
    var program1 = subject.getContent().getProgram(duplicate1.getId()).orElseThrow();
    assertEquals("Duplicated Program 1", program1.getName());
    var program2 = subject.getContent().getProgram(duplicate2.getId()).orElseThrow();
    assertEquals("Duplicated Program 2", program2.getName());
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

  /**
   Duplicating a Program, should enumerate to next available unique name https://github.com/xjmusic/xjmusic/issues/342
   */
  @Test
  void duplicateProgram_enumeratesToNextUniqueName() throws Exception {
    var duplicate = subject.duplicateProgram(UUID.fromString("7ad65895-27b8-453d-84f1-ef2a2a2f09eb"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "coconuts");

    assertEquals("coconuts 2", subject.getContent().getProgram(duplicate.getId()).orElseThrow().getName());
  }

  @Test
  void duplicateProgramSequence() throws Exception {
    var duplicate1 = subject.duplicateProgramSequence(UUID.fromString("2ff1c4e0-0f45-4457-900d-c7efef699e86"));
    var duplicate2 = subject.duplicateProgramSequence(UUID.fromString("d1be946c-ea2c-4f74-a9df-18a659b99fc8"));

    // 1 sequence per program
    var program1_sequence = subject.getContent().getProgramSequence(duplicate1.getId()).stream().findFirst().orElseThrow();
    var program2_sequence = subject.getContent().getProgramSequence(duplicate2.getId()).stream().findFirst().orElseThrow();
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
    // 0 bindings -- we don't duplicate the bindings because this would cause duplicate bindings at all the same offsets
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
  void duplicateProgramSequencePattern() throws Exception {
    var duplicate1 = subject.duplicateProgramSequencePattern(UUID.fromString("0457d46f-5d6c-495e-a3e8-8bb9740cee18"));
    var duplicate2 = subject.duplicateProgramSequencePattern(UUID.fromString("9ed7fbaa-540e-4539-b886-8788caf3dbff"));

    var program1_sequence_pattern1 = subject.getContent().getProgramSequencePattern(duplicate1.getId()).orElseThrow();
    var program1_sequence_pattern2 = subject.getContent().getProgramSequencePattern(duplicate2.getId()).orElseThrow();
    assertEquals("Duplicate of decay", program1_sequence_pattern1.getName());
    assertEquals("Duplicate of growth", program1_sequence_pattern2.getName());
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
  void duplicateInstrument() throws Exception {
    var duplicate1 = subject.duplicateInstrument(UUID.fromString("9097d757-ae8f-4d68-b449-8ec96602ca83"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "Duplicated Instrument 1");
    var duplicate2 = subject.duplicateInstrument(UUID.fromString("5cd9560b-e577-4f71-b263-ccf604b3bb30"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "Duplicated Instrument 2");

    // 2 instruments
    var instrument1 = subject.getContent().getInstrument(duplicate1.getId()).orElseThrow();
    assertEquals("Duplicated Instrument 1", instrument1.getName());
    var instrument2 = subject.getContent().getInstrument(duplicate2.getId()).orElseThrow();
    assertEquals("Duplicated Instrument 2", instrument2.getName());
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

  /**
   Duplicating an Instrument, should enumerate to next available unique name https://github.com/xjmusic/xjmusic/issues/342
   */
  @Test
  void duplicateInstrument_enumeratesToNextUniqueName() throws Exception {
    var duplicate = subject.duplicateInstrument(UUID.fromString("9097d757-ae8f-4d68-b449-8ec96602ca83"), UUID.fromString("aa613771-358d-4960-b5de-690ff6fd3a55"), "808 Drums");

    assertEquals("808 Drums 2", subject.getContent().getInstrument(duplicate.getId()).orElseThrow().getName());
  }
}
