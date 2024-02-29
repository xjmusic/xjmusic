package io.xj.nexus.audio;

import io.xj.hub.tables.pojos.InstrumentAudio;

public enum AudioMathUtils {
  ;

  /**
   Compute the actual amplitude given the target intensity, with a plateau at 1.0 in the middle of the intensity range
   E.g., for source intensity 0.5 and intensity threshold 0.5
   - target intensity 0.000 -> amplitude 0.0
   - target intensity 0.125 -> amplitude 0.5
   - target intensity 0.250 -> amplitude 1.0
   - target intensity 0.500 -> amplitude 1.0
   - target intensity 0.750 -> amplitude 1.0
   - target intensity 0.875 -> amplitude 0.5
   - target intensity 1.000 -> amplitude 0.0

   @param audio              instrument audio from which to get source intensity
   @param intensityLayers    if the audio only has one layer, intensity is always 1.0
   @param intensityThreshold distance between source and target intensity at which the amplitude fades down to 0
   @param intensity          target intensity
   @return amplitude
   */
  public static float computeIntensityAmplitude(InstrumentAudio audio, int intensityLayers, double intensityThreshold, double intensity) {
    if (intensityLayers == 1) return 1.0f;
    return (float) Math.min(1, 2 * Math.max(0, (1 - Math.abs(intensity - audio.getIntensity()) / intensityThreshold)));
  }
}
