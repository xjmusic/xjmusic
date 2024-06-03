package io.xj.engine.audio;

import io.xj.model.pojos.InstrumentAudio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioMathUtilsTest {

  /**
   Compute the actual amplitude given the target intensity, with a plateau at 1.0 in the middle of the intensity range
   E.g., for source intensity 0.5 and intensity threshold 0.5, with fadeOutLowerIntensities = true;
   - target intensity 0.000 -> amplitude 0.0
   - target intensity 0.125 -> amplitude 0.5
   - target intensity 0.250 -> amplitude 1.0
   - target intensity 0.500 -> amplitude 1.0
   - target intensity 0.750 -> amplitude 1.0
   - target intensity 0.875 -> amplitude 0.5
   - target intensity 1.000 -> amplitude 0.0
   E.g., for source intensity 0.5 and intensity threshold 0.5, with fadeOutLowerIntensities = false;
   - target intensity 0.000 -> amplitude 0.0
   - target intensity 0.125 -> amplitude 0.5
   - target intensity 0.250 -> amplitude 1.0
   - target intensity 0.500 -> amplitude 1.0
   - target intensity 0.750 -> amplitude 1.0
   - target intensity 0.875 -> amplitude 1.0
   - target intensity 1.000 -> amplitude 1.0
   */
  @Test
  public void computeIntensityAmplitude() {
    InstrumentAudio audio = new InstrumentAudio();
    audio.setIntensity(0.5f);
    int intensityLayers = 2;
    double intensityThreshold = 0.5;

    assertEquals(0.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, true, 0.000));
    assertEquals(0.5f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, true, 0.125));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, true, 0.250));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, true, 0.500));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, true, 0.750));
    assertEquals(0.5f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, true, 0.875));
    assertEquals(0.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, true, 1.000));
    assertEquals(0.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, false, 0.000));
    assertEquals(0.5f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, false, 0.125));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, false, 0.250));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, false, 0.500));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, false, 0.750));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, false, 0.875));
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(audio, intensityLayers, intensityThreshold, false, 1.000));
  }

  @Test
  public void computeIntensityAmplitude_singleIntensityLayerIgnoresEverything() {
    assertEquals(1.0f, AudioMathUtils.computeIntensityAmplitude(null, 1, 0, false, 0));
  }

}