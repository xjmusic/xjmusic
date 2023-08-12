// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

public interface WorkFactory {
  /**
   * Start work
   */
  boolean start(
    WorkConfiguration configuration,
    Runnable onDone
  );

  /**
   * Stop work
   */
  void finish();

  /**
   * Get work state
   */
  WorkState getWorkState();

  /**
   * Whether the factory is healthy
   */
  boolean isHealthy();
}
