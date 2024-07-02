// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_AUDIO_MATH_UTILS_H
#define XJMUSIC_AUDIO_MATH_UTILS_H

#include "xjmusic/content/InstrumentAudio.h"

namespace XJ {
  class InstrumentAudio;

  class AudioMathUtils {
  public:
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
    static float computeIntensityAmplitude(
        const InstrumentAudio *audio,
        const int intensityLayers,
        const float intensityThreshold,
        const bool fadeOutLowerIntensities,
        const float intensity);
  };

}// namespace XJ

#endif//XJMUSIC_AUDIO_MATH_UTILS_H
