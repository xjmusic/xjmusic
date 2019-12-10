// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

import io.xj.mixer.FrequencyRangeLimiter;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;

public class FrequencyRangeLimiterTest {
  float pi = 3.14159265359F;

  @Test
  public void filter() throws Exception {
    int FRAMES = 400;
    int CHANNELS = 2;
    int SAMPLE_RATE = 48000;
    int WAVELENGTH_A = SAMPLE_RATE / 400;
    int WAVELENGTH_B = SAMPLE_RATE / 2200;
    int AUDIO_BUFFER_SIZE = 16;
    double[][] bufferTest = testWave(FRAMES, CHANNELS, WAVELENGTH_A, WAVELENGTH_B);
    double[][] bufferControl = testWave(FRAMES, CHANNELS, WAVELENGTH_A, WAVELENGTH_B);
    FrequencyRangeLimiter.filter(bufferTest, SAMPLE_RATE, AUDIO_BUFFER_SIZE, 500.0F, 2000.0F);
    boolean identical = true;
    for (int i = 0; i < FRAMES; i++)
      if (!Arrays.equals(bufferTest[i], bufferControl[i]))
        identical = false;
    assertFalse("buffer is modified after filter operation", identical);
  }

  /**
   Create a test wave

   @param FRAMES       total
   @param CHANNELS     breadth
   @param WAVELENGTH_A frames per cycle, wave A
   @param WAVELENGTH_B frames per cycle, wave B
   @return buffer of test audio data
   */
  private double[][] testWave(int FRAMES, int CHANNELS, int WAVELENGTH_A, int WAVELENGTH_B) {
    double[][] out = new double[FRAMES][CHANNELS];
    for (int i = 0; i < FRAMES; i++) {
      out[i][0] = StrictMath.sin(2 * pi * i / WAVELENGTH_A) + StrictMath.sin(2 * pi * i / WAVELENGTH_B);
      out[i][1] = StrictMath.cos(2 * pi * i / WAVELENGTH_A) + StrictMath.cos(2 * pi * i / WAVELENGTH_B);
    }
    return out;
  }


}
