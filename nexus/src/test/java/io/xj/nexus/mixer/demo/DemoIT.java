// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer.demo;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.audio_cache.AudioCacheImpl;
import io.xj.nexus.mixer.ActiveAudio;
import io.xj.nexus.mixer.AudioFileWriter;
import io.xj.nexus.mixer.AudioFileWriterImpl;
import io.xj.nexus.mixer.EnvelopeProvider;
import io.xj.nexus.mixer.EnvelopeProviderImpl;
import io.xj.nexus.mixer.FormatException;
import io.xj.nexus.mixer.Mixer;
import io.xj.nexus.mixer.MixerConfig;
import io.xj.nexus.mixer.MixerFactory;
import io.xj.nexus.mixer.MixerFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectManagerImpl;
import io.xj.nexus.util.InternalResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildAudio;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgram;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgramSequence;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgramSequencePattern;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgramSequencePatternEvent;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgramVoice;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgramVoiceTrack;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProject;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoiceArrangement;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@ExtendWith(MockitoExtension.class)
public class DemoIT {
  static final long TEMPO = 121;
  static final String INTERNAL_RESOURCE_REFERENCE_AUDIO_FILE_PREFIX = "/demo_reference_outputs/";
  static final String INTERNAL_RESOURCE_INSTRUMENT_AUDIO_PREFIX = "/demo_source_audio/";
  static final String SOURCE_FILE_SUFFIX = ".wav";
  static final Project project = buildProject();
  static final Template template = buildTemplate(project, "Demo");
  static final Chain chain = buildChain(template);
  static final Program program = buildProgram(ProgramType.Beat, "C", TEMPO, 1);
  static final ProgramSequence sequence = buildProgramSequence(program, 4, "Demo", 1.0f, "C");
  static final ProgramVoice voice = buildProgramVoice(program, InstrumentType.Drum, "Demo Beat");
  static final ProgramVoiceTrack track = buildProgramVoiceTrack(voice, "Demo Beat");
  static final ProgramSequencePattern pattern = buildProgramSequencePattern(sequence, voice, 4, "Demo Beat");
  static final Segment segment = buildSegment(chain, 0, "C", 4, 1, TEMPO);
  static final SegmentChoice choice = buildSegmentChoice(segment, program);
  static final SegmentChoiceArrangement arrangement = buildSegmentChoiceArrangement(choice);
  static final Instrument instrument = buildInstrument();
  static final InstrumentAudio kick1 = buildAudio(instrument, "kick1", "kick1" + SOURCE_FILE_SUFFIX, 0, .701f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio kick2 = buildAudio(instrument, "kick2", "kick2" + SOURCE_FILE_SUFFIX, 0, .865f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio marac = buildAudio(instrument, "marac", "maracas" + SOURCE_FILE_SUFFIX, 0, .025f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio snare = buildAudio(instrument, "snare", "snare" + SOURCE_FILE_SUFFIX, 0, .092f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio lotom = buildAudio(instrument, "lotom", "tom1" + SOURCE_FILE_SUFFIX, 0, .360f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio clhat = buildAudio(instrument, "clhat", "cl_hihat" + SOURCE_FILE_SUFFIX, 0, .052f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio ding = buildAudio(instrument, "ding", "ding" + SOURCE_FILE_SUFFIX, 0, 3.733f, 120, 1.0f, "X", "C5", 1.0f);
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

  public DemoIT() throws IOException {
    String contentStoragePathPrefix = Files.createTempDirectory("xj_demo").toFile().getAbsolutePath() + File.separator;
    Files.createDirectory(Paths.get(contentStoragePathPrefix, "instrument"));
    projectManager = ProjectManagerImpl.createInstance();
    projectManager.setPathPrefix(contentStoragePathPrefix);
    instrumentPathPrefix = Files.createDirectory(Paths.get(contentStoragePathPrefix, "instrument", instrument.getId().toString())).toAbsolutePath().toString();
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
    audioCache = new AudioCacheImpl(projectManager);
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
          pick.getStartAtSegmentMicros() + pick.getLengthMicros()
        ))
      .toList());

    // Write the demo file output
    audioFileWriter.open(outputFilePath);
    audioFileWriter.append(mixer.getBuffer().consume(mixer.getBuffer().getAvailableByteCount()));
    audioFileWriter.finish();
  }

  private static Instrument buildInstrument() {
    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setLibraryId(UUID.randomUUID());
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
    ProgramSequencePatternEvent event = buildProgramSequencePatternEvent(
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

  /**
   FLOATING-POINT OUTPUT IS NOT SUPPORTED.
   [#137] Support for floating-point output encoding.

   @throws FormatException to prevent confusion
   */
  @Test
  public void demo_48000Hz_Signed_32bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding.PCM_SIGNED, 48000, 32, 2, 2.231404f, "48000Hz_Signed_32bit_2ch.wav");
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
