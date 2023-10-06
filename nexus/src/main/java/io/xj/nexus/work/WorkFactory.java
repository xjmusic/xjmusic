// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.nexus.persistence.SegmentManager;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface WorkFactory {
  /**
   Start work
   */
  boolean start(
    WorkConfiguration workConfig,
    HubConfiguration hubConfig,
    Callable<HubContent> hubContentProvider,
    Consumer<Double> progressUpdateCallback,
    Runnable onDone
  );

  /**
   Get a dub work instance

   @param hubConfig  hub config
   @param workConfig work config
   @return dub work instance
   */
  DubWork dub(HubConfiguration hubConfig, WorkConfiguration workConfig);

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
   Return the current dubbed-to sync chain micros

   @return chain micros, else empty
   */
  Optional<Long> getDubbedToChainMicros();

  /**
   Return the current crafted-to chain micros

   @return chain micros, else empty
   */
  Optional<Long> getCraftedToChainMicros();

  /**
   If the current work is realtime, e.g. playback or HLS, return the target chain micros

   @return chain micros if realtime, else empty
   */
  Optional<Long> getShipTargetChainMicros();
}
