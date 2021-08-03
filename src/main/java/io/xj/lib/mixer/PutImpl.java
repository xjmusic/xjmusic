// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.Objects;

/**
 Put to represent a single audio source playing at a specific time in the future.
 */
class PutImpl implements Put {
  private final int bus;
  private final String sourceId;
  private final long startAtMicros;
  private final long stopAtMicros;
  private final double velocity;
  private String state;

  @Inject
  public PutImpl(
    @Assisted("bus") int bus,
    @Assisted("sourceId") String sourceId,
    @Assisted("startAtMicros") long startAtMicros,
    @Assisted("stopAtMicros") long stopAtMicros,
    @Assisted("velocity") double velocity
  ) {
    this.bus = bus;
    this.sourceId = sourceId;
    this.startAtMicros = startAtMicros;
    this.stopAtMicros = stopAtMicros;
    this.velocity = velocity;

    state = READY;
  }

  public long sourceOffsetMicros(long atMixOffsetMicros) {
    switch (state) {

      case READY:
        if (atMixOffsetMicros > startAtMicros)
          state = PLAY;
        return 0;

      case PLAY:
        if (atMixOffsetMicros > stopAtMicros)
          state = DONE;
        return (long) (atMixOffsetMicros - (double) startAtMicros);

      case DONE:
      default:
        return 0;
    }
  }

  @Override
  public boolean isAlive() {
    return !Objects.equals(state, DONE);
  }

  @Override
  public boolean isPlaying() {
    return Objects.equals(state, PLAY);
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
  public int getBus() {
    return bus;
  }
}
