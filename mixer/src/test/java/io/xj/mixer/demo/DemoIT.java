// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.demo;

import com.google.common.io.Files;
import com.google.inject.Guice;
import io.xj.mixer.Mixer;
import io.xj.mixer.MixerConfig;
import io.xj.mixer.MixerFactory;
import io.xj.mixer.MixerModule;
import io.xj.mixer.OutputEncoder;
import io.xj.mixer.impl.exception.FormatException;
import io.xj.mixer.util.InternalResource;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Duration;

import static org.junit.Assert.assertTrue;

public class DemoIT {
  private static final long attackMicros = 1000;
  private static final long releaseMicros = 5000;
  private static final long bpm = 121;
  private static final Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  private static final Duration step = beat.dividedBy(4);
  private static final String filePrefix = "demo_source_audio/808/";
  private static final String referenceAudioFilePrefix = "demo_reference_outputs/";
  private static final String tempFilePrefix = "/tmp/";
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
  private static void assertMixOutputEqualsReferenceAudio(OutputEncoder encoder, AudioFormat.Encoding encoding, int frameRate, int sampleBits, int channels, String referenceName) throws Exception {
    String filename = getUniqueTempFilename(referenceName);
    mixAndWriteOutput(encoder, encoding, frameRate, sampleBits, channels, filename);
    switch (encoder) {
      case WAV:
        assertTrue("Demo output does not match reference audio for " + referenceName + "!",
          Files.equal(new File(filename), resourceFile(getReferenceAudioFilename(referenceName))));
        break;
      case OGG:
      case AAC:
        assertTrue("Demo output does not match file size +/-2% of reference audio for " + referenceName + "!",
          isFileSizeWithin(new File(filename), resourceFile(getReferenceAudioFilename(referenceName)), 0.02f)
        );
        break;
    }
  }

  /**
   Assert size of two different files is within a tolerated threshold

   @param f1        to compare
   @param f2        to compare
   @param threshold ratio +/- to tolerate, where 0.0 is perfect equality, 0.1 is 10 percent deviation.
   @return true if within tolerance
   */
  private static boolean isFileSizeWithin(File f1, File f2, float threshold) {
    Float deviance = (float) f1.getTotalSpace() / f2.getTotalSpace();
    return (1 - threshold) < deviance && (1 + threshold) > deviance;
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
  private static void mixAndWriteOutput(OutputEncoder outputEncoder, AudioFormat.Encoding outputEncoding, int outputFrameRate, int outputSampleBits, int outputChannels, String outputFilePath) throws Exception {
    Mixer demoMixer = mixerFactory.createMixer(new MixerConfig(
      new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
        (outputChannels * outputSampleBits / 8), outputFrameRate, false),
      totalLength()
    ));

    // setup the sources
    for (String sourceName : sources) {
      demoMixer.loadSource(sourceName, inputFile(filePrefix + sourceName + sourceFileSuffix));
    }

    // setup the music
    int iL = demoSequence.length;
    for (int i = 0; i < iL; i++) {
      demoMixer.put(demoSequence[i], atMicros(i), atMicros(i + 3), attackMicros, releaseMicros, 1.0, 1.0, 0);
    }

    // mix it
    demoMixer.mixToFile(outputEncoder, outputFilePath, 1.0f);
  }

  /**
   Fetch new buffered input stream from file

   @param filePath to fetch
   @return buffered stream of file
   @throws FileNotFoundException on failure
   */
  private static BufferedInputStream inputFile(String filePath) throws FileNotFoundException {
    return new BufferedInputStream(new FileInputStream(resourceFile(filePath)));
  }

  /**
   get a file from java resources

   @param filePath to get
   @return File
   */
  private static File resourceFile(String filePath) {
    InternalResource internalResource = new InternalResource(filePath);
    return internalResource.getFile();
  }

  /**
   get unique temp filename

   @param subFilename filename within this filename
   @return filename
   */
  private static String getUniqueTempFilename(String subFilename) {
    return tempFilePrefix + System.nanoTime() + "-" + subFilename;
  }

  /**
   get reference audio filename

   @param referenceName within this filename
   @return filename
   */
  private static String getReferenceAudioFilename(String referenceName) {
    return referenceAudioFilePrefix + referenceName;
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
   total length

   @return duration
   */
  private static Duration totalLength() {
    return loopLength();
  }

  /**
   loop length

   @return duration
   */
  private static Duration loopLength() {
    return step.multipliedBy(demoSequence.length);
  }

  /**
   FLOATING-POINT OUTPUT IS NOT SUPPORTED.
   [#137] Support for floating-point output encoding.

   @throws FormatException to prevent confusion
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

  /**
   [#162361712] AAC output
   */
  @Test
  public void demo_48000Hz_2ch_AAC() throws Exception {
    assertMixOutputEqualsReferenceAudio(OutputEncoder.AAC, AudioFormat.Encoding.PCM_SIGNED, 48000, 16, 2, "48000Hz_Signed_16bit_2ch.aac");
  }


}
