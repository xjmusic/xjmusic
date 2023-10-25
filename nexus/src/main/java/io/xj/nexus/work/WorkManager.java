// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.persistence.SegmentManager;

import java.util.Optional;

public interface WorkManager {
  /**
   Start work.
   <p>
   This assigns the work configuration, hub configuration, and hub access.
   However, in order to advance the state machine and perform work, the parent framework must repeatedly call
   {@link #runCycle()} from its preferred work thread.
   */
  void start(
    WorkConfiguration workConfig,
    HubConfiguration hubConfig,
    HubClientAccess hubAccess
  );

  /**
   Stop work
   */
  void finish();

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

  /**
   Run the current work cycle
   */
  void runCycle();

  WorkState getWorkState();

  /**
   @return true if the current work is healthy
   */
  boolean isHealthy();

  /**
   @return true if the current work is finished
   */
  boolean isFinished();
}
