// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.nexus.persistence.SegmentManager;

import java.util.Optional;

public interface WorkFactory {
  /**
   Start work
   */
  boolean start(
    WorkConfiguration configuration,
    Runnable onDone
  );

  /**
   Stop work
   */
  void finish();

  /**
   Get work state
   */
  WorkState getWorkState();

  /**
   Whether the factory is healthy
   */
  boolean isHealthy();

  /**
   Get the segment manager
   */
  SegmentManager getSegmentManager();

  /**
   Reset the factory including the segment manager and its store
   */
  void reset();

  /**
   Get the Hub content source material

   @return source material
   */
  HubContent getSourceMaterial();

  /**
   Return the current shipped-to chain micros

   @return chain micros, else empty
   */
  Optional<Long> getShippedToChainMicros();

  /**
   Return the current crafted-to chain micros

   @return chain micros, else empty
   */
  Optional<Long> getCraftedToChainMicros();

  /**
   Return the current dubbed-to sync chain micros

   @return chain micros, else empty
   */
  Optional<Long> getDubbedToChainMicros();
}
