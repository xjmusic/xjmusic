// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

/**
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public interface ShipWork {
  /**
   * This method just does work until failure, blocks until interrupted
   */
  void start();

  /**
   * Stop work
   */
  void finish();

  /**
   * Whether the next cycle nanos is above threshold, compared to System.nanoTime();
   * <p>
   * Ship health check tests playlist length and ffmpeg liveness https://www.pivotaltracker.com/story/show/180746583
   *
   * @return next cycle nanos
   */
  boolean isHealthy();

}
