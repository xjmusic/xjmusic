// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJNEXUS_MUSIC_BPM_H
#define XJNEXUS_MUSIC_BPM_H

#include "xjnexus/util/ValueUtils.h"

namespace Music {

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

}// namespace Music

#endif// XJNEXUS_MUSIC_BPM_H