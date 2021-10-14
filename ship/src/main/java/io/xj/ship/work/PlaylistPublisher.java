// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

/**
 * This process is run directly in the hard loop (not in a Fork/Join pool)
 * <p>
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public interface PlaylistPublisher {
  /**
   * @param nowMillis at which to publish
   */
  void publish(long nowMillis);
}
