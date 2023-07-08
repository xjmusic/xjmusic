// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.work;

public interface WorkFactory {
  /**
   * Start work
   */
  void start(Runnable onDone);

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
