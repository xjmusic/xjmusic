// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ShipWork {
  /**
   This method just does work until failure, blocks until interrupted
   */
  void work();

  /**
   Stop work
   */
  void stop();

  /**
   Whether the next cycle nanos is above threshold, compared to System.nanoTime();

   @return next cycle nanos
   */
  boolean isHealthy();
}
