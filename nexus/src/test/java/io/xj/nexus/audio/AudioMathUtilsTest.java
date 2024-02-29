package io.xj.nexus.audio;

import io.xj.hub.tables.pojos.InstrumentAudio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioMathUtilsTest {

  @Test
  public void computeIntensityAmplitude() {
    InstrumentAudio audio = new InstrumentAudio();
    audio.setIntensity(0.5f);
    int intensityLayers = 2;
    double intensityThreshold = 0.5;

    assertEquals(0.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, 0.000));
    assertEquals(0.5f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, 0.125));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, 0.250));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, 0.500));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, 0.750));
    assertEquals(0.5f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, 0.875));
    assertEquals(0.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, 1.000));
  }

  @Test
  public void computeIntensityAmplitude_singleIntensityLayerIgnoresEverything() {
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(null, 1, 0, 0));
  }

}