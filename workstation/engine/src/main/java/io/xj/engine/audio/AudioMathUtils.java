package io.xj.engine.audio;

import io.xj.hub.pojos.InstrumentAudio;

public enum AudioMathUtils {
  ;

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

   @param audio                   instrument audio from which to get source intensity
   @param intensityLayers         if the audio only has one layer, intensity is always 1.0
   @param intensityThreshold      distance between source and target intensity at which the amplitude fades down to 0
   @param fadeOutLowerIntensities whether to fade out lower intensities when the target is higher than the source
   @param intensity               target intensity
   @return amplitude
   */
  public static float computeIntensityAmplitude(InstrumentAudio audio, int intensityLayers, double intensityThreshold, boolean fadeOutLowerIntensities, double intensity) {
    if (intensityLayers == 1) return 1.0f;
    if (fadeOutLowerIntensities)
      return (float) Math.min(1, 2 * Math.max(0, (1 - Math.abs(audio.getIntensity() - intensity) / intensityThreshold)));
    else
      return (float) Math.min(1, 2 * Math.max(0, (1 - (audio.getIntensity() - intensity) / intensityThreshold)));
  }
}
