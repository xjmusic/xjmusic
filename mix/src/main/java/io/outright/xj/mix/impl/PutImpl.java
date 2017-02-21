// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.mix.impl;

import io.outright.xj.mix.Put;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Put to represent a single audio source playing at a specific time in the future.
 */
public class PutImpl implements Put {
  private final String sourceId;
  private String state;

  private final long startAtMicros;
  private final long stopAtMicros;
  private final double velocity;
  private final double pitchRatio;
  private final double pan;

  @Inject
  public PutImpl(
    @Assisted("sourceId") String sourceId,
    @Assisted("startAtMicros") long startAtMicros,
    @Assisted("stopAtMicros") long stopAtMicros,
    @Assisted("velocity") double velocity,
    @Assisted("pitchRatio") double pitchRatio,
    @Assisted("pan") double pan
  ) {
    this.sourceId = sourceId;
    this.startAtMicros = startAtMicros;
    this.stopAtMicros = stopAtMicros;
    this.velocity = velocity;
    this.pitchRatio = pitchRatio;
    this.pan = pan;

    this.state = READY;
  }

  public long sourceOffsetMicros(long atMixOffsetMicros) {
    switch (state) {

      case READY:
        if (atMixOffsetMicros > startAtMicros) {
          state = PLAY;
        }
        return 0;

      case PLAY:
        if (atMixOffsetMicros > stopAtMicros) {
          state = DONE;
        }
//        return (long) Math.floor(((double) atMixOffsetMicros - startAtMicros) / pitchRatio);
        return atMixOffsetMicros - startAtMicros;

      case DONE:
        return 0;

      default:
        return 0;
    }
  }


  @Override
  public boolean isAlive() {
    return !state.equals(DONE);
  }

  @Override
  public boolean isPlaying() {
    return state.equals(PLAY);
  }

  @Override
  public String getSourceId() {
    return sourceId;
  }

  @Override
  public String getState() {
    return state;
  }

  @Override
  public long getStartAtMicros() {
    return startAtMicros;
  }

  @Override
  public long getStopAtMicros() {
    return stopAtMicros;
  }

  @Override
  public double getVelocity() {
    return velocity;
  }

  @Override
  public double getPitchRatio() {
    return pitchRatio;
  }

  @Override
  public double getPan() {
    return pan;
  }

}
