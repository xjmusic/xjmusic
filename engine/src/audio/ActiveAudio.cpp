// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/audio/ActiveAudio.h"

using namespace XJ;

ActiveAudio::ActiveAudio(
    const SegmentChoiceArrangementPick* pick,
    const Instrument *instrument,
    const InstrumentAudio* audio,
    unsigned long long startAtMixerMicros,
    std::optional<unsigned long long> stopAtMixerMicros,
    float fromIntensityAmplitude,
    float toIntensityAmplitude) {
  this->pick = pick;
  this->audio = audio;
  this->startAtMixerMicros = startAtMixerMicros;
  this->stopAtMixerMicros = stopAtMixerMicros;
  this->instrument = instrument;

  // computed
  this->fromAmplitude = fromIntensityAmplitude * pick->amplitude * instrument->volume * audio->volume;
  this->toAmplitude = toIntensityAmplitude * pick->amplitude * instrument->volume * audio->volume;
  this->instrumentConfig = InstrumentConfig(instrument);
}

UUID ActiveAudio::getId() {
  return pick->id;
}

const SegmentChoiceArrangementPick * ActiveAudio::getPick() {
  return pick;
}

const Instrument * ActiveAudio::getInstrument() {
  return instrument;
}

unsigned long long ActiveAudio::getStartAtMixerMicros() const {
  return startAtMixerMicros;
}

std::optional<unsigned long long> ActiveAudio::getStopAtMixerMicros() const {
  return stopAtMixerMicros;
}

const InstrumentAudio * ActiveAudio::getAudio() {
  return audio;
}

int ActiveAudio::getReleaseMillis() const {
  return instrumentConfig.releaseMillis;
}

float ActiveAudio::getAmplitude(const float ap) const {
  if (fromAmplitude == toAmplitude) return fromAmplitude;
  return fromAmplitude + (toAmplitude - fromAmplitude) * ap;
}
