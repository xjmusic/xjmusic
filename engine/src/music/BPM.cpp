// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/music/BPM.h"

namespace Music {

  static double NANOS_PER_SECOND = 1000000000;

  long BPM::beatsNanos(long beats, float bpm) {
    return (long) (NANOS_PER_SECOND * beats * 60 / bpm);
  }

  float BPM::velocity(float bpm) {
    return 60 / bpm;
  }

}// namespace Music