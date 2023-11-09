// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer.demo;

import io.xj.nexus.audio_cache.DubAudioCache;
import io.xj.nexus.audio_cache.DubAudioCacheImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.mixer.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static io.xj.hub.util.Assertion.assertFileMatchesResourceFile;

@ExtendWith(MockitoExtension.class)
public class DemoIT {
  static final long bpm = 121;
  static final Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  static final Duration step = beat.dividedBy(4);
  static final String filePrefix = "demo_source_audio/";
  static final String sourceFileSuffix = ".wav";
  static final DemoSource kick1 = new DemoSource(UUID.randomUUID(), "808/kick1");
  static final DemoSource kick2 = new DemoSource(UUID.randomUUID(), "808/kick2");
  static final DemoSource marac = new DemoSource(UUID.randomUUID(), "808/maracas");
  static final DemoSource snare = new DemoSource(UUID.randomUUID(), "808/snare");
  static final DemoSource lotom = new DemoSource(UUID.randomUUID(), "808/tom1");
  static final DemoSource clhat = new DemoSource(UUID.randomUUID(), "808/cl_hihat");
  static final DemoSource ding = new DemoSource(UUID.randomUUID(), "instrument7-audio9-24bit-88200hz");
  static final DemoSource[] sources = {
    kick1,
    kick2,
    marac,
    snare,
    lotom,
    clhat,
    ding
  };
  static final DemoSource[] demoSequence = {
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
  static final String referenceAudioFilePrefix = "demo_reference_outputs/";
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

    // set up the sources
    for (DemoSource source : sources) {
      // TODO: pass this path in as an active audio source mixer.loadSource(source.id(), getResourceFile(filePrefix + source.key() + sourceFileSuffix).getAbsolutePath(), "test audio");
    }

    // set up the music
    int iL = demoSequence.length;
/*
 TODO: add to active audios array
    for (int i = 0; i < iL; i++)
      mixer.put(UUID.randomUUID(), demoSequence[i].id(), DEFAULT_BUS, atMicros(i), atMicros(i + 3), 1.0f, 1, 5);
*/

/*
 TODO: add to active audios array
    // To also test high rate inputs being added to the mix
    mixer.put(UUID.randomUUID(), ding.id(), DEFAULT_BUS, atMicros(0), atMicros(4), 1.0f, 1, 5);
*/

    // mix it
    List<ActiveAudio> activeAudios = List.of(); // TODO: actual list of active audios
    mixer.mix(activeAudios);

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
