// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.mixer.demo;

import io.outright.xj.mixer.MixerFactory;
import io.outright.xj.mixer.MixerModule;
import io.outright.xj.mixer.Mixer;
import io.outright.xj.mixer.OutputContainer;
import io.outright.xj.mixer.impl.resource.InternalResource;

import com.google.inject.Guice;
import com.google.inject.Module;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.time.Duration;

/**
 Demo of mix, Java-native sequence-based audio mixing for music apps.

 @author Charney Kaye */
public class Main {

  private static final Duration preRoll = Duration.ofSeconds(1);
  private static final Duration postRoll = Duration.ofSeconds(2);

  private static final String outputFilePath = "/tmp/OUTPUT-MY-FILE.wav";
  private static final int outputFrameRate = 48000;
  private static final int outputChannels = 2;
  private static final int outputSampleBits = 32;
  private static final AudioFormat.Encoding outputEncoding = AudioFormat.Encoding.PCM_SIGNED;

  private static final long bpm = 121;
  private static final Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  private static final Duration step = beat.dividedBy(4);
  private static final int loopTimes = 8;

  private static final String filePrefix = "demo_audio/808/";
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
   Main method.

   @param args arguments
   @throws Exception if execution fails
   */
  public static void main(String[] args) throws Exception {
    Module mod = new MixerModule();

    MixerFactory mixerFactory = Guice.createInjector(mod).getInstance(MixerFactory.class);

    Mixer demoMixer = mixerFactory.createMixer(
      OutputContainer.WAV,
      new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
        (outputChannels * outputSampleBits / 8), outputFrameRate, false),
      totalLength()
    );

    // setup the sources
    for (String sourceName : sources) {
      //Get file from resources folder
      InternalResource internalResource = new InternalResource(filePrefix + sourceName + fileSuffix);
      demoMixer.loadSource(sourceName, new BufferedInputStream(new FileInputStream(internalResource.getFile())));
    }

    // setup the music
    for (int n = 0; n < loopTimes; n++) {
      for (int s = 0; s < demoPattern.length; s++) {
        demoMixer.put(demoPattern[s], atMicros(n, s), atMicros(n, s + 3), 1.0, 1.0, 0);
      }
    }

    // mix it
    demoMixer.mixToFile(outputFilePath);
  }

  private static long atMicros(int loopNum, int stepNum) {
    return preRoll.plus(loopLength().multipliedBy(loopNum)).plus(step.multipliedBy(stepNum)).toNanos() / 1000;
  }

  private static Duration totalLength() {
    return preRoll.plus(loopLength().multipliedBy(loopTimes)).plus(postRoll);
  }

  private static Duration loopLength() {
    return step.multipliedBy(demoPattern.length);
  }

}
