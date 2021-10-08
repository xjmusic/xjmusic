// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer.demo;

import com.google.inject.Guice;
import io.xj.lib.mixer.*;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;

import static io.xj.lib.util.Assertion.assertFileMatchesResourceFile;
import static io.xj.lib.util.Assertion.assertFileSizeToleranceFromResourceFile;

public class DemoIT {
  private static final long bpm = 121;
  private static final Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  private static final Duration step = beat.dividedBy(4);
  private static final String filePrefix = "demo_source_audio/808/";
  private static final String sourceFileSuffix = ".wav";
  private static final String kick1 = "kick1";
  private static final String kick2 = "kick2";
  private static final String marac = "maracas";
  private static final String snare = "snare";
  private static final String lotom = "tom1";
  private static final String clhat = "cl_hihat";
  private static final String[] sources = {
    kick1,
    kick2,
    marac,
    snare,
    lotom,
    clhat
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

  /**
   * assert mix output equals reference audio
   *
   * @param encoder       to output
   * @param encoding      encoding
   * @param frameRate     frame rate
   * @param sampleBits    sample bits
   * @param channels      channels
   * @param referenceName name
   * @throws Exception on failure
   */
  private static void assertMixOutputEqualsReferenceAudio(OutputEncoder encoder, AudioFormat.Encoding encoding, int frameRate, int sampleBits, int channels, String referenceName) throws Exception {
    String filename = io.xj.lib.util.Files.getUniqueTempFilename(referenceName);
    mixAndWriteOutput(encoder, encoding, frameRate, sampleBits, channels, filename);
    switch (encoder) {
      case WAV -> assertFileMatchesResourceFile(filename, getReferenceAudioFilename(referenceName));
      case OGG -> assertFileSizeToleranceFromResourceFile(filename, getReferenceAudioFilename(referenceName));
    }
  }

  /**
   * Execute a mix and write output to file
   *
   * @param outputEncoder    to encode pure floating point samples in channels to contained file output
   * @param outputEncoding   encoding
   * @param outputFrameRate  frame rate
   * @param outputSampleBits bits per sample
   * @param outputChannels   channels
   * @param outputFilePath   file path to write output
   * @throws Exception on failure
   */
  private static void mixAndWriteOutput(OutputEncoder outputEncoder, AudioFormat.Encoding outputEncoding, int outputFrameRate, int outputSampleBits, int outputChannels, String outputFilePath) throws Exception {
    Mixer demoMixer = mixerFactory.createMixer(new MixerConfig(
      new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
        (outputChannels * outputSampleBits / 8), outputFrameRate, false)
    ));

    // set up the sources
    for (String sourceName : sources)
      demoMixer.loadSource(sourceName, io.xj.lib.util.Files.inputFile(filePrefix + sourceName + sourceFileSuffix));

    // set up the music
    int iL = demoSequence.length;
    for (int i = 0; i < iL; i++)
      demoMixer.put("Default", demoSequence[i], atMicros(i), atMicros(i + 3), 1.0);

    // mix it
    demoMixer.mixToFile(outputEncoder, outputFilePath, 1.0f);
  }

  /**
   * get microseconds at a particular loop # and step #
   *
   * @param stepNum step
   * @return microseconds
   */
  private static long atMicros(int stepNum) {
    return step.multipliedBy(stepNum).toNanos() / 1000;
  }

  /**
   * get reference audio filename
   *
   * @param referenceName within this filename
   * @return filename
   */
  public static String getReferenceAudioFilename(String referenceName) {
    return referenceAudioFilePrefix + referenceName;
  }

  /**
   * FLOATING-POINT OUTPUT IS NOT SUPPORTED.
   * [#137] Support for floating-point output encoding.
   *
   * @throws FormatException to prevent confusion
   */
  @Test
  public void demo_48000Hz_Signed_32bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.WAV, AudioFormat.Encoding.PCM_SIGNED, 48000, 32, 2, "48000Hz_Signed_32bit_2ch.wav");
  }

  @Test
  public void demo_48000Hz_Signed_16bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.WAV, AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, "44100Hz_Signed_16bit_2ch.wav");
  }

  @Test
  public void demo_48000Hz_Signed_8bit_1ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.WAV, AudioFormat.Encoding.PCM_SIGNED, 22000, 8, 1, "22000Hz_Signed_8bit_1ch.wav");
  }

  @Test
  public void demo_48000Hz_2ch_OggVorbis() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.OGG, AudioFormat.Encoding.PCM_FLOAT, 48000, 32, 2, "48000Hz_Float_32bit_2ch.ogg");
  }

}
