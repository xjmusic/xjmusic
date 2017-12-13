// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.demo;

import io.xj.mixer.Mixer;
import io.xj.mixer.MixerFactory;
import io.xj.mixer.MixerModule;
import io.xj.mixer.OutputContainer;
import io.xj.mixer.impl.exception.FormatException;
import io.xj.mixer.util.InternalResource;

import com.google.common.io.Files;
import com.google.inject.Guice;

import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;

import static org.junit.Assert.assertTrue;

public class DemoIT {
  private static MixerFactory mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);

  private static final Duration preRoll = Duration.ofMillis(500);
  private static final Duration postRoll = Duration.ofMillis(500);

  private static final long bpm = 121;
  private static final Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  private static final Duration step = beat.dividedBy(4);

  private static final String filePrefix = "demo_source_audio/808/";
  private static final String referenceAudioFilePrefix = "demo_reference_outputs/";
  private static final String tempFilePrefex = "/tmp/";
  private static final String fileSuffix = ".wav";

  private static final String kick1 = "kick1";
  private static final String kick2 = "kick2";
  private static final String marac = "maracas";
  private static final String snare = "snare";
  private static final String lotom = "tom1";
  private static final String clhat = "cl_hihat";

  private static final String[] sources = new String[]{
    kick1,
    kick2,
    marac,
    snare,
    lotom,
    clhat
  };

  private static final String[] demoPattern = new String[]{
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

  /**
   FLOATING-POINT OUTPUT IS NOT SUPPORTED.
   [#137] Support for floating-point output encoding.

   @throws FormatException to prevent confusion
   */
  @Test(expected = FormatException.class)
  public void demo_48000Hz_Float_32bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding.PCM_FLOAT, 48000, 32, 2, "48000Hz_Float_32bit_2ch");
  }

  @Test
  public void demo_48000Hz_Signed_32bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding.PCM_SIGNED, 48000, 32, 2, "48000Hz_Signed_32bit_2ch");
  }

  @Test
  public void demo_48000Hz_Signed_16bit_2ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, "44100Hz_Signed_16bit_2ch");
  }

  @Test
  public void demo_48000Hz_Signed_8bit_1ch() throws Exception {
    assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding.PCM_SIGNED, 22000, 8, 1, "22000Hz_Signed_8bit_1ch");
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
  private void assertMixOutputEqualsReferenceAudio(AudioFormat.Encoding encoding, int frameRate, int sampleBits, int channels, String referenceName) throws Exception {
    String filename = getUniqueTempFilename(referenceName);
    mixAndWriteOutput(encoding, frameRate, sampleBits, channels, filename);
    assertTrue("Demo output does not match reference audio for " + referenceName + "!",
      Files.equal(new File(filename), resourceFile(getReferenceAudioFilename(referenceName))));
  }

  /**
   Execute a mix and write output to file

   @param outputEncoding   encoding
   @param outputFrameRate  frame rate
   @param outputSampleBits bits per sample
   @param outputChannels   channels
   @param outputFilePath   file path to write output
   @throws Exception on failure
   */
  private void mixAndWriteOutput(AudioFormat.Encoding outputEncoding, int outputFrameRate, int outputSampleBits, int outputChannels, String outputFilePath) throws Exception {
    Mixer demoMixer = mixerFactory.createMixer(
      OutputContainer.WAV,
      new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
        (outputChannels * outputSampleBits / 8), outputFrameRate, false),
      totalLength()
    );

    // setup the sources
    for (String sourceName : sources) {
      demoMixer.loadSource(sourceName, new BufferedInputStream(new FileInputStream(resourceFile(filePrefix + sourceName + fileSuffix))));
    }

    // setup the music
    for (int s = 0; s < demoPattern.length; s++) {
      demoMixer.put(demoPattern[s], atMicros(s), atMicros(s + 3), 1.0, 1.0, 0);
    }

    // mix it
    demoMixer.mixToFile(outputFilePath);
  }

  /**
   get a file from java resources

   @param filePath to get
   @return File
   */
  private File resourceFile(String filePath) {
    InternalResource internalResource = new InternalResource(filePath);
    return internalResource.getFile();
  }

  /**
   get unique temp filename

   @param subFilename filename within this filename
   @return filename
   */
  private String getUniqueTempFilename(String subFilename) {
    return tempFilePrefex + System.nanoTime() + "-" + subFilename + fileSuffix;
  }


  /**
   get reference audio filename

   @param referenceName within this filename
   @return filename
   */
  private String getReferenceAudioFilename(String referenceName) {
    return referenceAudioFilePrefix + referenceName + fileSuffix;
  }

  /**
   get microseconds at a particular loop # and step #

   @param stepNum step
   @return microseconds
   */
  private long atMicros(int stepNum) {
    return preRoll.plus(step.multipliedBy(stepNum)).toNanos() / 1000;
  }

  /**
   total length

   @return duration
   */
  private Duration totalLength() {
    return preRoll.plus(loopLength()).plus(postRoll);
  }

  /**
   loop length

   @return duration
   */
  private Duration loopLength() {
    return step.multipliedBy(demoPattern.length);
  }

}
