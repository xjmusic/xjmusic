// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_BPM_H
#define XJMUSIC_MUSIC_BPM_H

#include "xjmusic/util/ValueUtils.h"

namespace XJ {

  /**
   * Beats-Per-Minute
   * (utilities)
   */
  class BPM {
  public:

    /**
     * Nanoseconds length of a given number of beats at a given Beats Per Minute.
     *
     * @param beats total length
     * @param bpm   beats per minute
     */
    static long beatsNanos(long beats, float bpm);

    /**
     * Velocity in seconds-ber-beat for any BPM
     *
     * @param bpm to get seconds-per-beat for
     * @return velocity in seconds-per-beat
     */
    static float velocity(float bpm);

  };

}// namespace XJ

#endif// XJMUSIC_MUSIC_BPM_H