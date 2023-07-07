// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import io.xj.nexus.ship.ShipException;

/**
 * Ship competent HTTP Live Stream https://www.pivotaltracker.com/story/show/180419462
 */
public interface StreamEncoder {

  /**
   * Append audio to the encoder process
   *
   * @param samples of audio to append
   * @throws ShipException on failure
   */
  void append(byte[] samples) throws ShipException;

  /**
   * Publish any new generated media segments
   *
   * @param atMillis time at which to publish (now, unless testing)
   * @throws ShipException on failure
   */
  void publish(long atMillis) throws ShipException;

  /**
   * Close the encoder and release resources
   */
  void close();

  /**
   * Check if the ship encoder process is healthy
   * <p>
   * Ship health check tests playlist length and ffmpeg liveness https://www.pivotaltracker.com/story/show/180746583
   *
   * @return true if healthy
   */
  boolean isHealthy();

  /**
   * Set the target time in chain micros
   *
   * @param atChainMicros time in chain micros
   */
  void setPlaylistAtChainMicros(long atChainMicros);
}
