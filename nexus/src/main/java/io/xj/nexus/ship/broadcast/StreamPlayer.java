// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import io.xj.nexus.ship.ShipException;

/**
 * Ship competent HTTP Live Stream https://www.pivotaltracker.com/story/show/180419462
 */
public interface StreamPlayer {

  /**
   * Publish PCM data bytes to the output
   *
   * @param samples of audio to append
   */
  byte[] append(byte[] samples) throws ShipException;

  /**
   * Close the player and release resources
   */
  void close();
}
