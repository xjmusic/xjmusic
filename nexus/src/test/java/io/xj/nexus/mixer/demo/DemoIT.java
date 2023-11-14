// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer.demo;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.nexus.audio_cache.DubAudioCache;
import io.xj.nexus.audio_cache.DubAudioCacheImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.mixer.*;
import io.xj.nexus.model.*;
import io.xj.nexus.util.InternalResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static io.xj.nexus.NexusHubIntegrationTestingFixtures.*;
import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@ExtendWith(MockitoExtension.class)
public class DemoIT {
  static final long bpm = 121;
  static final Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  static final Duration step = beat.dividedBy(4);
  static final String filePrefix = "/demo_source_audio/";
  static final String sourceFileSuffix = ".wav";
  static final Account account = buildAccount();
  static final Template template = buildTemplate(account, "Demo");
  static final Chain chain = buildChain(template);
  static final Program program = buildProgram(ProgramType.Beat, "C", 120, 1);
  static final ProgramSequence sequence = buildProgramSequence(program, 4, "Demo", 1.0f, "C");
  static final ProgramVoice voice = buildProgramVoice(program, InstrumentType.Drum, "Demo Beat");
  static final ProgramVoiceTrack track = buildProgramVoiceTrack(voice, "Demo Beat");
  static final ProgramSequencePattern pattern = buildProgramSequencePattern(sequence, voice, 4, "Demo Beat");
  static final Segment segment = buildSegment(chain, 0, "C", 4, 1, 120);
  static final SegmentChoice choice = buildSegmentChoice(segment, program);
  static final SegmentChoiceArrangement arrangement = buildSegmentChoiceArrangement(choice);
  static final Instrument instrument = buildInstrument(InstrumentType.Drum, InstrumentMode.Event, false, false);
  static final InstrumentAudio kick1 = buildAudio(instrument, "kick1", "808/kick1", 0, .701f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio kick2 = buildAudio(instrument, "kick2", "808/kick2", 0, .865f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio marac = buildAudio(instrument, "marac", "808/maracas", 0, .025f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio snare = buildAudio(instrument, "snare", "808/snare", 0, .092f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio lotom = buildAudio(instrument, "lotom", "808/tom1", 0, .360f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio clhat = buildAudio(instrument, "clhat", "808/cl_hihat", 0, .052f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio ding = buildAudio(instrument, "ding", "instrument7-audio9-24bit-88200hz", 0, 3.733f, 120, 1.0f, "X", "C5", 1.0f);
  static final InstrumentAudio[] sources = {
    kick1,
    kick2,
    marac,
    snare,
    lotom,
    clhat,
    ding
  };
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
  final MixerFactory mixerFactory;
  static final String referenceAudioFilePrefix = "/demo_reference_outputs/";
  static final int DEFAULT_BUS = 0;

  @Mock
  HttpClientProvider httpClientProvider;

  public DemoIT() {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    DubAudioCache dubAudioCache = new DubAudioCacheImpl(httpClientProvider);
    this.mixerFactory = new MixerFactoryImpl(envelopeProvider, dubAudioCache);
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
    assertArrayEquals(testFileBytes, generatedFileBytes);
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
    AudioFormat audioFormat = new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
      (outputChannels * outputSampleBits / 8), outputFrameRate, false);
    AudioFileWriter audioFileWriter = new AudioFileWriterImpl(audioFormat);
    MixerConfig mixerConfig = new MixerConfig(audioFormat);
    mixerConfig.setTotalSeconds(outputSeconds);
    Mixer mixer = mixerFactory.createMixer(mixerConfig);


    List<SegmentChoiceArrangementPick> picks = new ArrayList<>();

    /*
    // set up the sources
    TODO: pass this path in as an active audio source
      for (InstrumentAudio source : sources) {
        // mixer.loadSource(source.id(), getResourceFile(filePrefix + source.key() + sourceFileSuffix).getAbsolutePath(), "test audio");
      }
     */

    // set up the music
    int iL = demoSequence.length;
    for (int i = 0; i < iL; i++) {
      // TODO new version of:  mixer.put(UUID.randomUUID(), demoSequence[i].id(), DEFAULT_BUS, atMicros(i), atMicros(i + 3), 1.0f, 1, 5);
      ProgramSequencePatternEvent event = buildProgramSequencePatternEvent(
        pattern,
        track,
        (float) i / 4,
        0.25f,
        "X",
        1.0f
      );
      picks.add(buildSegmentChoiceArrangementPick(
        segment,
        arrangement,
        event,
        demoSequence[i],
        demoSequence[i].getName()
      ));
    }

/*
 TODO: add to active audios array
    // To also test high rate inputs being added to the mix
    mixer.put(UUID.randomUUID(), ding.id(), DEFAULT_BUS, atMicros(0), atMicros(4), 1.0f, 1, 5);
*/

    // mix it
    mixer.mix(picks.stream()
      .map((SegmentChoiceArrangementPick pick) ->
        new ActiveAudio(pick, instrument, audio, startAtMixerMicros, stopAtMixerMicros))
      .toList());

    // Write the demo file output
    audioFileWriter.open(outputFilePath);
    audioFileWriter.append(mixer.getBuffer().consume(mixer.getBuffer().getAvailableByteCount()));
    audioFileWriter.finish();
  }

  /**
   get microseconds at a particular loop # and step #

   @param stepNum step
   @return microseconds
   */
  static long atMicros(int stepNum) {
    return step.multipliedBy(stepNum).toNanos() / 1000;
  }

  /**
   get reference audio filename

   @param referenceName within this filename
   @return filename
   */
  public static String getReferenceAudioFilename(String referenceName) {
    return referenceAudioFilePrefix + referenceName;
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
