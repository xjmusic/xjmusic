// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer.demo;

import com.google.inject.Guice;
import io.xj.lib.mixer.*;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;

import static io.xj.lib.util.Assertion.assertFileMatchesResourceFile;
import static io.xj.lib.util.Assertion.assertFileSizeToleranceFromResourceFile;
import static io.xj.lib.util.Files.getResourceFile;
import static org.junit.Assert.assertEquals;

public class DemoIT {
  private static final long bpm = 121;
  private static final Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  private static final Duration step = beat.dividedBy(4);
  private static final String filePrefix = "demo_source_audio/";
  private static final String sourceFileSuffix = ".wav";
  private static final String kick1 = "808/kick1";
  private static final String kick2 = "808/kick2";
  private static final String marac = "808/maracas";
  private static final String snare = "808/snare";
  private static final String lotom = "808/tom1";
  private static final String clhat = "808/cl_hihat";
  private static final String ding = "instrument7-audio9-24bit-88200hz";
  private static final String[] sources = {
    kick1,
    kick2,
    marac,
    snare,
    lotom,
    clhat,
    ding
  };
  private static final String[] demoSequence = {
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
  private static final MixerFactory mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);
  private static final String referenceAudioFilePrefix = "demo_reference_outputs/";
  private static final String DEFAULT_BUS = "Default";

  /**
   assert mix output equals reference audio

   @param encoder       to output
   @param encoding      encoding
   @param frameRate     frame rate
   @param sampleBits    sample bits
   @param channels      channels
   @param referenceName name
   @throws Exception on failure
   */
  @SuppressWarnings("SameParameterValue")
  private static void assertMixOutputEqualsReferenceAudio(OutputEncoder encoder, AudioFormat.Encoding encoding, int frameRate, int sampleBits, int channels, double seconds, String referenceName) throws Exception {
    String filename = io.xj.lib.util.Files.getUniqueTempFilename(referenceName);
    assertEquals("Mixed output length in seconds", seconds, mixAndWriteOutput(encoder, encoding, frameRate, sampleBits, channels, filename), 0.01);
    switch (encoder) {
      case WAV -> assertFileMatchesResourceFile(getReferenceAudioFilename(referenceName), filename);
      case OGG -> assertFileSizeToleranceFromResourceFile(filename, getReferenceAudioFilename(referenceName));
    }
  }

  /**
   Execute a mix and write output to file

   @param outputEncoder    to encode pure floating point samples in channels to contained file output
   @param outputEncoding   encoding
   @param outputFrameRate  frame rate
   @param outputSampleBits bits per sample
   @param outputChannels   channels
   @param outputFilePath   file path to write output
   @throws Exception on failure
   */
  private static double mixAndWriteOutput(OutputEncoder outputEncoder, AudioFormat.Encoding outputEncoding, int outputFrameRate, int outputSampleBits, int outputChannels, String outputFilePath) throws Exception {
    Mixer demoMixer = mixerFactory.createMixer(new MixerConfig(
      new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
        (outputChannels * outputSampleBits / 8), outputFrameRate, false)
    ));

    // set up the sources
    for (String sourceName : sources)
      demoMixer.loadSource(sourceName, getResourceFile(filePrefix + sourceName + sourceFileSuffix).getAbsolutePath());

    // set up the music
    int iL = demoSequence.length;
    for (int i = 0; i < iL; i++)
      demoMixer.put(DEFAULT_BUS, demoSequence[i], atMicros(i), atMicros(i + 3), 1.0);

    // To also test high rate inputs being added to the mix
    demoMixer.put(DEFAULT_BUS, ding, atMicros(0), atMicros(4), 1.0);

    // mix it
    return demoMixer.mixToFile(outputEncoder, outputFilePath, 1.0f);
  }

  /**
   get microseconds at a particular loop # and step #

   @param stepNum step
   @return microseconds
   */
  private static long atMicros(int stepNum) {
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
    assertMixOutputEqualsReferenceAudio(OutputEncoder.WAV, AudioFormat.Encoding.PCM_SIGNED, 48000, 32, 2, 2.231404, "48000Hz_Signed_32bit_2ch.wav");
  }

  @Test
  public void demo_48000Hz_Signed_16bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.WAV, AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 2.231404, "44100Hz_Signed_16bit_2ch.wav");
  }

  @Test
  public void demo_48000Hz_Signed_8bit_1ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.WAV, AudioFormat.Encoding.PCM_SIGNED, 22000, 8, 1, 2.231404, "22000Hz_Signed_8bit_1ch.wav");
  }

  @Test
  public void demo_48000Hz_2ch_OggVorbis() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.OGG, AudioFormat.Encoding.PCM_FLOAT, 48000, 32, 2, 2.231404, "48000Hz_Float_32bit_2ch.ogg");
  }

}
