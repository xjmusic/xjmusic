// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/audio/AudioInMemory.h"

using namespace XJ;

UUID AudioInMemory::getId() {
  return audio.id;
}

std::string AudioInMemory::getWaveformKey() {
  return audio.waveformKey;
}

bool AudioInMemory::isDifferent(InstrumentAudio other) {
  return audio.id != other.id || audio.waveformKey != other.waveformKey;
}
