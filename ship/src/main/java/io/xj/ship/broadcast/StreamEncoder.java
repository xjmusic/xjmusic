// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import io.xj.ship.ShipException;

/**
 Ship competent HTTP Live Stream https://www.pivotaltracker.com/story/show/180419462
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
   @param atMillis time at which to publish (now, unless testing)
   */
  void publish(long atMillis) throws ShipException;

  /**
   Close the encoder and release resources
   */
  void close();

  /**
   Check if the ship encoder process is healthy
   <p>
   Ship health check tests playlist length and ffmpeg liveness https://www.pivotaltracker.com/story/show/180746583

   @return true if healthy
   */
  boolean isHealthy();
}
