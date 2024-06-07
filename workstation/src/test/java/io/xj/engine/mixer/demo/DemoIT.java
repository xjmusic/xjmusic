// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.mixer.demo;

import io.xj.engine.FabricationContentOneFixtures;
import io.xj.engine.FabricationContentTwoFixtures;
import io.xj.engine.audio.AudioCache;
import io.xj.engine.audio.AudioCacheImpl;
import io.xj.engine.audio.AudioLoader;
import io.xj.engine.audio.AudioLoaderImpl;
import io.xj.engine.http.HttpClientProvider;
import io.xj.engine.hub_client.HubClientFactory;
import io.xj.engine.hub_client.HubClientFactoryImpl;
import io.xj.engine.mixer.ActiveAudio;
import io.xj.engine.mixer.AudioFileWriter;
import io.xj.engine.mixer.AudioFileWriterImpl;
import io.xj.engine.mixer.EnvelopeProvider;
import io.xj.engine.mixer.EnvelopeProviderImpl;
import io.xj.engine.mixer.Mixer;
import io.xj.engine.mixer.MixerConfig;
import io.xj.engine.mixer.MixerFactory;
import io.xj.engine.mixer.MixerFactoryImpl;
import io.xj.engine.util.InternalResource;
import io.xj.gui.project.ProjectManager;
import io.xj.gui.project.ProjectManagerImpl;
import io.xj.model.entity.EntityFactory;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentState;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.Library;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoice;
import io.xj.model.pojos.ProgramVoiceTrack;
import io.xj.model.pojos.Project;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.Template;
import io.xj.model.util.ValueUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@ExtendWith(MockitoExtension.class)
public class DemoIT {
  static final long TEMPO = 121;
  static final String INTERNAL_RESOURCE_REFERENCE_AUDIO_FILE_PREFIX = "/demo_reference_outputs/";
  static final String INTERNAL_RESOURCE_INSTRUMENT_AUDIO_PREFIX = "/demo_source_audio/";
  static final String SOURCE_FILE_SUFFIX = ".wav";
  static final Project project = FabricationContentOneFixtures.buildProject();
  static final Template template = FabricationContentOneFixtures.buildTemplate(project, "Demo");
  static final Chain chain = FabricationContentTwoFixtures.buildChain(template);
  static final Program program = FabricationContentOneFixtures.buildProgram(ProgramType.Beat, "C", TEMPO);
  static final ProgramSequence sequence = FabricationContentOneFixtures.buildProgramSequence(program, 4, "Demo", 1.0f, "C");
  static final ProgramVoice voice = FabricationContentOneFixtures.buildProgramVoice(program, InstrumentType.Drum, "Demo Beat");
  static final ProgramVoiceTrack track = FabricationContentOneFixtures.buildProgramVoiceTrack(voice, "Demo Beat");
  static final ProgramSequencePattern pattern = FabricationContentOneFixtures.buildProgramSequencePattern(sequence, voice, 4, "Demo Beat");
  static final Segment segment = FabricationContentTwoFixtures.buildSegment(chain, 0, "C", 4, 1, TEMPO);
  static final SegmentChoice choice = FabricationContentTwoFixtures.buildSegmentChoice(segment, program);
  static final SegmentChoiceArrangement arrangement = FabricationContentTwoFixtures.buildSegmentChoiceArrangement(choice);
  static final Library library = buildLibrary(project);
  static final Instrument instrument = buildInstrument(library);
  static final InstrumentAudio kick1 = FabricationContentOneFixtures.buildAudio(instrument, "kick1", "kick1" + SOURCE_FILE_SUFFIX, 0, .701f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio kick2 = FabricationContentOneFixtures.buildAudio(instrument, "kick2", "kick2" + SOURCE_FILE_SUFFIX, 0, .865f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio marac = FabricationContentOneFixtures.buildAudio(instrument, "marac", "maracas" + SOURCE_FILE_SUFFIX, 0, .025f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio snare = FabricationContentOneFixtures.buildAudio(instrument, "snare", "snare" + SOURCE_FILE_SUFFIX, 0, .092f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio lotom = FabricationContentOneFixtures.buildAudio(instrument, "lotom", "tom1" + SOURCE_FILE_SUFFIX, 0, .360f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio clhat = FabricationContentOneFixtures.buildAudio(instrument, "clhat", "cl_hihat" + SOURCE_FILE_SUFFIX, 0, .052f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio ding = FabricationContentOneFixtures.buildAudio(instrument, "ding", "ding" + SOURCE_FILE_SUFFIX, 0, 3.733f, 120, 1.0f, "X", "C5", 1.0f);
  static final Map<UUID, InstrumentAudio> audioById = Stream.of(
    kick1,
    kick2,
    marac,
    snare,
    lotom,
    clhat,
    ding
  ).collect(Collectors.toMap(InstrumentAudio::getId, instrumentAudio -> instrumentAudio));
  static final InstrumentAudio[] demoSequence = {
    kick2,
    marac,
    clhat,
    marac,
    snare,
    marac,
    clhat,
    kick2,
    clhat,
    marac,
    kick1,
    marac,
    snare,
    lotom,
    clhat,
    marac
  };
  private final String instrumentPathPrefix;
  private final ProjectManager projectManager;
  private MixerFactory mixerFactory;
  private AudioCache audioCache;

  @Mock
  private HttpClientProvider httpClientProvider;

  public DemoIT() throws IOException {
    String contentStoragePathPrefix = Files.createTempDirectory("xj_demo").toFile().getAbsolutePath() + File.separator;
    Files.createDirectory(Paths.get(contentStoragePathPrefix, "libraries"));
    Files.createDirectory(Paths.get(contentStoragePathPrefix, "libraries", "Demo-Library"));
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubClientFactory hubClientFactory = new HubClientFactoryImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory, 3);
    projectManager = new ProjectManagerImpl(jsonProvider, entityFactory, httpClientProvider, hubClientFactory);
    projectManager.setProjectPathPrefix(contentStoragePathPrefix);
    projectManager.getContent().put(instrument);
    projectManager.getContent().put(library);
    instrumentPathPrefix = Files.createDirectory(Paths.get(contentStoragePathPrefix, "libraries", "Demo-Library", "Test-Drums")).toAbsolutePath().toString();
    audioById.values().forEach(audio -> {
      try {
        var from = new InternalResource(INTERNAL_RESOURCE_INSTRUMENT_AUDIO_PREFIX + audio.getWaveformKey()).getFile().toPath();
        var to = Paths.get(instrumentPathPrefix, audio.getWaveformKey());
        Files.copy(from, to);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @BeforeEach
  public void beforeEach() {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    AudioLoader audioLoader = new AudioLoaderImpl(projectManager);
    audioCache = new AudioCacheImpl(projectManager, audioLoader);
    this.mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
  }

  /**
   assert mix output equals reference audio

   @param encoding      encoding
   @param frameRate     frame rate
   @param sampleBits    sample bits
   @param channels      channels
   @param referenceName name
   @throws Exception on failure
   */
  @SuppressWarnings("SameParameterValue")
  void assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding encoding, int frameRate, int sampleBits, int channels, float seconds, String referenceName) throws Exception {
    String filename = Files.createTempFile("demo-output", ".wav").toAbsolutePath().toString();
    mixAndWriteOutput(encoding, frameRate, sampleBits, channels, seconds, filename);
    assertFileMatchesResourceFile(getReferenceAudioFilename(referenceName), filename);
  }

  /**
   assert file matches resource file

   @param referenceFilePath expected
   @param targetFilePath    actual
   @throws IOException on failure
   */
  private void assertFileMatchesResourceFile(String referenceFilePath, String targetFilePath) throws IOException {
    Path generatedFilePath = Paths.get(targetFilePath);
    InternalResource resource = new InternalResource(referenceFilePath);
    byte[] generatedFileBytes = Files.readAllBytes(generatedFilePath);
    byte[] testFileBytes = Files.readAllBytes(resource.getFile().toPath());
    assertArrayEquals(testFileBytes, generatedFileBytes, String.format("Generated file %s does not match reference file %s", generatedFilePath, referenceFilePath));
  }

  /**
   Execute a mix and write output to file

   @param outputEncoding   encoding
   @param outputFrameRate  frame rate
   @param outputSampleBits bits per sample
   @param outputChannels   channels
   @param outputSeconds    seconds
   @param outputFilePath   file path to write output
   @throws Exception on failure
   */
  void mixAndWriteOutput(AudioFormat.Encoding outputEncoding, int outputFrameRate, int outputSampleBits, int outputChannels, float outputSeconds, String outputFilePath) throws Exception {
    audioCache.initialize(
      outputFrameRate,
      outputSampleBits,
      outputChannels
    );
    AudioFormat audioFormat = new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
      (outputChannels * outputSampleBits / 8), outputFrameRate, false);
    AudioFileWriter audioFileWriter = new AudioFileWriterImpl(audioFormat);
    MixerConfig mixerConfig = new MixerConfig(audioFormat);
    mixerConfig.setTotalSeconds(outputSeconds);
    Mixer mixer = mixerFactory.createMixer(mixerConfig);

    List<SegmentChoiceArrangementPick> picks = new ArrayList<>();

    // set up the music
    picks.add(buildPick(ding, 0.0f, 4.0f));
    for (int i = 0; i < demoSequence.length; i++) {
      picks.add(buildPick(demoSequence[i], (float) i / 4, 0.25f));
    }

    // mix it -- for the demo, 1 segment = the mixer buffer length
    mixer.mix(picks.stream()
      .map((SegmentChoiceArrangementPick pick) ->
        new ActiveAudio(
          pick,
          instrument,
          audioById.get(pick.getInstrumentAudioId()),
          pick.getStartAtSegmentMicros(),
          pick.getStartAtSegmentMicros() + pick.getLengthMicros(),
          1.0f, 1.0f))
      .toList(), 1.0);

    // Write the demo file output
    audioFileWriter.open(outputFilePath);
    audioFileWriter.append(mixer.getBuffer().consume(mixer.getBuffer().getAvailableByteCount()));
    audioFileWriter.finish();
  }

  @SuppressWarnings("SameParameterValue")
  private static Library buildLibrary(Project project) {
    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setName("Demo Library");
    library.setProjectId(project.getId());
    return library;
  }

  @SuppressWarnings("SameParameterValue")
  private static Instrument buildInstrument(Library library) {
    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setVolume(1.0f);
    instrument.setLibraryId(library.getId());
    instrument.setType(InstrumentType.Drum);
    instrument.setMode(InstrumentMode.Event);
    instrument.setState(InstrumentState.Published);
    instrument.setName("Test Drums");
    return instrument;
  }

  /**
   get reference audio filename

   @param referenceName within this filename
   @return filename
   */
  public static String getReferenceAudioFilename(String referenceName) {
    return INTERNAL_RESOURCE_REFERENCE_AUDIO_FILE_PREFIX + referenceName;
  }

  /**
   create a pick

   @param audio    to pick
   @param position in beats
   @param duration in beats
   @return pick
   */
  private SegmentChoiceArrangementPick buildPick(InstrumentAudio audio, float position, float duration) {
    ProgramSequencePatternEvent event = FabricationContentOneFixtures.buildProgramSequencePatternEvent(
      pattern,
      track,
      position,
      duration,
      "X",
      1.0f
    );

    var microsPerBeat = ValueUtils.MICROS_PER_SECOND * ValueUtils.SECONDS_PER_MINUTE / segment.getTempo();
    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(DemoIT.arrangement.getSegmentId());
    pick.setSegmentChoiceArrangementId(DemoIT.arrangement.getId());
    pick.setProgramSequencePatternEventId(event.getId());
    pick.setInstrumentAudioId(audio.getId());
    pick.setStartAtSegmentMicros((long) (event.getPosition() * microsPerBeat));
    pick.setLengthMicros((long) (event.getDuration() * microsPerBeat));
    pick.setAmplitude(event.getVelocity());
    pick.setTones(event.getTones());
    pick.setEvent("X");
    return pick;
  }

  @Test
  public void demo_48000Hz_Signed_16bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 2.231404f, "44100Hz_Signed_16bit_2ch.wav");
  }

  @Test
  public void demo_48000Hz_Signed_8bit_1ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding.PCM_SIGNED, 22000, 8, 1, 2.231404f, "22000Hz_Signed_8bit_1ch.wav");
  }
}
