// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/music/BPM.h"

using namespace XJ;


static double NANOS_PER_SECOND = 1000000000;


long long BPM::beatsNanos(const float beats, const float bpm) {
  return static_cast<long long>(NANOS_PER_SECOND * beats * 60 / bpm);
}


float BPM::velocity(const float bpm) {
  return 60 / bpm;
}
