// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

/**
 Ship competent HTTP Live Stream https://www.pivotaltracker.com/story/show/180419462
 */
public interface StreamPlayer {

  /**
   Publish PCM data bytes to the output

   @param samples of audio to append
   */
  void append(byte[] samples);

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
