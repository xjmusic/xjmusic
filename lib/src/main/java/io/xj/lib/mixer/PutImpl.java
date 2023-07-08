// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;


import java.util.Objects;
import java.util.UUID;

/**
 * Put to represent a single audio source playing at a specific time in the future.
 */
class PutImpl implements Put {
  String state;
  final UUID id;
  final UUID audioId;
  final double velocity;
  final int bus;
  final int attackMillis;
  final int releaseMillis;
  final long startAtMicros;
  final long stopAtMicros;

  public PutImpl(
    UUID id,
    UUID audioId,
    int bus,
    int attackMillis,
    int releaseMillis,
    long startAtMicros,
    long stopAtMicros,
    double velocity
  ) {
    this.id = id;
    this.audioId = audioId;
    this.bus = bus;
    this.attackMillis = attackMillis;
    this.releaseMillis = releaseMillis;
    this.startAtMicros = startAtMicros;
    this.stopAtMicros = stopAtMicros;
    this.velocity = velocity;

    state = READY;
  }

  public long sourceOffsetMicros(long atMixOffsetMicros) {
    switch (state) {
      case READY -> {
        if (atMixOffsetMicros > startAtMicros)
          state = PLAY;
        return 0;
      }
      case PLAY -> {
        if (atMixOffsetMicros > stopAtMicros)
          state = DONE;
        return (long) (atMixOffsetMicros - (double) startAtMicros);
      }
      default -> {
        return 0;
      }
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
  public UUID getId() {
    return id;
  }

  @Override
  public UUID getAudioId() {
    return audioId;
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
  public int getAttackMillis() {
    return attackMillis;
  }

  @Override
  public int getReleaseMillis() {
    return releaseMillis;
  }

  @Override
  public int getBus() {
    return bus;
  }
}
