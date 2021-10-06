// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer.demo;

import com.google.inject.Guice;
import com.google.inject.Module;
import io.xj.lib.mixer.*;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.time.Duration;

/**
 Demo of mix, Java-native sequence-based audio mixing for music apps.

 @author Charney Kaye */
public interface Main {
  long attackMicros = 10000;
  long releaseMicros = 50000;

  Duration preRoll = Duration.ofSeconds(1);

  String outputFilePath = "/tmp/OUTPUT-MY-FILE.wav";
  int outputFrameRate = 48000;
  int outputChannels = 2;
  int outputSampleBits = 32;
  AudioFormat.Encoding outputEncoding = AudioFormat.Encoding.PCM_SIGNED;

  long bpm = 121;
  Duration beat = Duration.ofMinutes(1).dividedBy(bpm);
  Duration step = beat.dividedBy(4);
  int loopTimes = 8;

  String filePrefix = "demo_audio/808/";
  String fileSuffix = ".wav";

  String kick1 = "kick1";
  String kick2 = "kick2";
  String marac = "maracas";
  String snare = "snare";
  String lotom = "tom1";
  String clhat = "cl_hihat";

  String[] sources = {
    kick1,
    kick2,
    marac,
    snare,
    lotom,
    clhat
  };

  String[] demoSequence = {
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
  static void main(String[] args) throws Exception {
    Module mod = new MixerModule();

    MixerFactory mixerFactory = Guice.createInjector(mod).getInstance(MixerFactory.class);

    Mixer demoMixer = mixerFactory.createMixer(new MixerConfig(
      new AudioFormat(outputEncoding, outputFrameRate, outputSampleBits, outputChannels,
        (outputChannels * outputSampleBits / 8), outputFrameRate, false)
    ));

    // setup the sources
    for (String sourceName : sources) {
      //Get file from resources folder
      InternalResource internalResource = new InternalResource(filePrefix + sourceName + fileSuffix);
      demoMixer.loadSource(sourceName, new BufferedInputStream(new FileInputStream(internalResource.getFile())));
    }

    // setup the music
    int iL = demoSequence.length;
    for (int n = 0; loopTimes > n; n++) {
      for (int i = 0; i < iL; i++) {
        demoMixer.put("Default", demoSequence[i], atMicros(n, i), atMicros(n, i + 3), 1.0);
      }
    }

    // mix it
    demoMixer.mixToFile(OutputEncoder.WAV, outputFilePath, 0.618f);
  }

  private static long atMicros(int loopNum, int stepNum) {
    return preRoll.plus(loopLength().multipliedBy(loopNum)).plus(step.multipliedBy(stepNum)).toNanos() / 1000;
  }

  private static Duration loopLength() {
    return step.multipliedBy(demoSequence.length);
  }

}
