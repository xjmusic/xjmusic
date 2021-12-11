// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import io.xj.ship.ShipException;

/**
 Ship competent HTTP Live Stream #180419462
 */
public interface StreamPlayer {

  /**
   Publish a media segment to the output

   @param samples of audio to append
   */
  double[][] append(double[][] samples) throws ShipException;

  /**
   Close the player and release resources
   */
  void close();
}
