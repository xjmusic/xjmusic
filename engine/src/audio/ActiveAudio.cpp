// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/audio/ActiveAudio.h"

using namespace XJ;

ActiveAudio::ActiveAudio(
    const SegmentChoiceArrangementPick *pick,
    const Instrument *instrument,
    const InstrumentAudio *audio,
    const unsigned long long startAtChainMicros,
    const unsigned long long stopAtChainMicros,
    const float fromIntensity,
    const float toIntensity) {
  this->pick = pick;
  this->audio = audio;
  this->startAtChainMicros = startAtChainMicros;
  this->stopAtChainMicros = stopAtChainMicros;
  this->instrument = instrument;

  // computed
  this->fromVolume = fromIntensity * pick->amplitude * instrument->volume * audio->volume;
  this->toVolume = toIntensity * pick->amplitude * instrument->volume * audio->volume;
}

UUID ActiveAudio::getId() const {
  return pick->id;
}

const SegmentChoiceArrangementPick *ActiveAudio::getPick() const {
  return pick;
}

const Instrument *ActiveAudio::getInstrument() const {
  return instrument;
}

unsigned long long ActiveAudio::getStartAtChainMicros() const {
  return startAtChainMicros;
}

unsigned long long ActiveAudio::getStopAtChainMicros() const {
  return stopAtChainMicros;
}

const InstrumentAudio *ActiveAudio::getAudio() const {
  return audio;
}

int ActiveAudio::getReleaseMillis() const {
  return instrument->config.releaseMillis;
}

float ActiveAudio::getAmplitude(const float ap) const {
  if (fromVolume == toVolume) return fromVolume;
  return fromVolume + (toVolume - fromVolume) * ap;
}

float ActiveAudio::getFromVolume() const {
  return fromVolume;
}

float ActiveAudio::getToVolume() const {
  return toVolume;
}
