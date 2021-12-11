// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import io.xj.ship.ShipException;

/**
 This process is run directly in the hard loop (not in a Fork/Join pool)
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ChunkMixer {
  /**
   Invoke the recursive action

   @param chunk to mix
   @return mixed samples
   */
  double[][] mix(Chunk chunk) throws ShipException;
}
