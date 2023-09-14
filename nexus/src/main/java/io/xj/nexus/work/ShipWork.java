// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import java.util.Optional;

/**
 THERE IS NO SPOON
 <p>
 Ground-up rewrite of the XJ work logic. First we instantiate the dub cycle, which depends on the craft cycle
 operating on a separate thread. All Segment craft is persisted in memory, and the dub cycle is responsible for
 requesting crafted segments and specifically the picked audio, and then dubbing each output audio chunk.
 <p>
 Output audio chunks are dynamically sized. There is a default size, but the size is also determined by the
 duration of the segment. E.g., during gapless album output, the chunk will cut short if necessary to begin the next
 chunk at exactly the top of the following segment.
 <p>
 https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 */
public interface ShipWork {

  /**
   This blocks for as long as the work is alive
   */
  void start();

  /**
   Stop work
   */
  void finish();

  /**
   Test whether all expected chains are healthy, depending on chain manager mode
   Whether the next cycle nanos is above threshold, compared to System.nanoTime();

   @return next cycle nanos
   */
  boolean isHealthy();

  /**
   If the current work is realtime, e.g. playback or HLS, return the current chain micros

   @return chain micros if realtime, else empty
   */
  Optional<Long> getShippedToChainMicros();

  /**
   If the current work is realtime, e.g. playback or HLS, return the target chain micros

   @return chain micros if realtime, else empty
   */
  Optional<Long> getShipTargetChainMicros();
}
