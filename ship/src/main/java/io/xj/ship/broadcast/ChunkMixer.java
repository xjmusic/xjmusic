// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import io.xj.ship.ShipException;

/**
 This process is run directly in the hard loop (not in a Fork/Join pool)
 <p>
 Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public interface ChunkMixer {

  /**
   Whether the Chunk is ready to mix
   <p>
   Ship should not upload empty media segment files! https://www.pivotaltracker.com/story/show/180745353

   @return true if ready
   @throws ShipException on failure
   */
  boolean isReadyToMix() throws ShipException;

  /**
   Invoke the recursive action

   @return mixed samples
   */
  double[][] mix() throws ShipException;
}
