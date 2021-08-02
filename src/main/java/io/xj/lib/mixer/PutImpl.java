// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.Objects;

/**
 Put to represent a single audio source playing at a specific time in the future.
 */
class PutImpl implements Put {
  private final String busId;
  private final String sourceId;
  private final long startAtMicros;
  private final long stopAtMicros;
  private final long attackMicros;
  private final long releaseMicros;
  private final double velocity;
  private final double pan;
  private String state;

  @Inject
  public PutImpl(
    @Assisted("busId") String busId,
    @Assisted("sourceId") String sourceId,
    @Assisted("startAtMicros") long startAtMicros,
    @Assisted("stopAtMicros") long stopAtMicros,
    @Assisted("attackMicros") long attackMicros,
    @Assisted("releaseMicros") long releaseMicros,
    @Assisted("velocity") double velocity,
    @Assisted("pan") double pan
  ) {
    this.busId = busId;
    this.sourceId = sourceId;
    this.startAtMicros = startAtMicros;
    this.stopAtMicros = stopAtMicros;
    this.velocity = velocity;
    this.pan = pan;
    this.attackMicros = attackMicros;
    this.releaseMicros = releaseMicros;

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

  public Double envelope(long atMixOffsetMicros) {
    if (!Objects.equals(PLAY, state)) return 0.0;
    double envIn = (atMixOffsetMicros - (double) startAtMicros) / attackMicros;
    double envOut = ((double) stopAtMicros - atMixOffsetMicros) / releaseMicros;
    return Math.max(0, Math.min(1.0, Math.min(envIn, envOut)));
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
  public double getPan() {
    return pan;
  }

  @Override
  public long getAttackMicros() {
    return attackMicros;
  }

  @Override
  public long getReleaseMicros() {
    return releaseMicros;
  }

  @Override
  public String getBusId() {
    return busId;
  }
}
