// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.persistence.SegmentManager;
import jakarta.annotation.Nullable;

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
   Get the craft work
   */
  @Nullable
  CraftWork getCraftWork();

  /**
   Get the dub work
   */
  @Nullable
  DubWork getDubWork();

  /**
   Get the ship work
   */
  @Nullable
  ShipWork getShipWork();
}
