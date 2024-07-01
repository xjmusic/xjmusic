// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/work/ActiveAudio.h"

XJ::ActiveAudio::ActiveAudio(
    SegmentChoiceArrangementPick pick,
    Instrument instrument,
    InstrumentAudio audio,
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
  this->fromAmplitude = fromIntensityAmplitude * pick.amplitude * instrument.volume * audio.volume;
  this->toAmplitude = toIntensityAmplitude * pick.amplitude * instrument.volume * audio.volume;
  this->instrumentConfig = InstrumentConfig(instrument);
}

XJ::UUID XJ::ActiveAudio::getId() {
  return pick.id;
}

XJ::SegmentChoiceArrangementPick XJ::ActiveAudio::getPick() {
  return pick;
}

XJ::Instrument XJ::ActiveAudio::getInstrument() {
  return instrument;
}

unsigned long long XJ::ActiveAudio::getStartAtMixerMicros() const {
  return startAtMixerMicros;
}

std::optional<unsigned long long> XJ::ActiveAudio::getStopAtMixerMicros() const {
  return stopAtMixerMicros;
}

XJ::InstrumentAudio XJ::ActiveAudio::getAudio() {
  return audio;
}

int XJ::ActiveAudio::getReleaseMillis() const {
  return instrumentConfig.releaseMillis;
}

float XJ::ActiveAudio::getAmplitude(const float ap) const {
  if (fromAmplitude == toAmplitude) return fromAmplitude;
  return fromAmplitude + (toAmplitude - fromAmplitude) * ap;
}