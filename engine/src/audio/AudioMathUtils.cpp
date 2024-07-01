// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/audio/AudioMathUtils.h"

float XJ::AudioMathUtils::computeIntensityAmplitude(const InstrumentAudio *audio, const int intensityLayers, const float intensityThreshold, const bool fadeOutLowerIntensities, const float intensity) {
  if (intensityLayers == 1) return 1.0f;

  if (fadeOutLowerIntensities)
    return std::min(1.0f, 2.0f * std::max(0.0f, 1.0f - std::abs(audio->intensity - intensity) / intensityThreshold));

  return std::min(1.0f, 2.0f * std::max(0.0f, 1.0f - (audio->intensity - intensity) / intensityThreshold));
}