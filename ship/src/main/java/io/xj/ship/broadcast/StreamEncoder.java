// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import io.xj.ship.ShipException;

/**
 Ship competent HTTP Live Stream #180419462
 */
public interface StreamEncoder {

  /**
   Append audio to the encoder process

   @param samples of audio to append
   @throws ShipException on failure
   */
  double[][] append(double[][] samples) throws ShipException;

  /**
   Publish any new generated media segments

   @throws ShipException on failure
   @param atMillis
   */
  void publish(long atMillis) throws ShipException;

  /**
   Close the encoder and release resources
   */
  void close();
}
