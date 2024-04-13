// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

/**
 Ship competent HTTP Live Stream https://github.com/xjmusic/workstation/issues/279
 */
public interface StreamPlayer {

  /**
   Publish PCM data bytes to the output

   @param samples of audio to append
   */
  void write(byte[] samples);

  /**
   Close the player and release resources
   */
  void finish();

  /**
   The time at which the last sample was heard at the chain in microseconds.
   The actual playback chain micros calculated from the audio playback system.
   */
  long getHeardAtChainMicros();
}
