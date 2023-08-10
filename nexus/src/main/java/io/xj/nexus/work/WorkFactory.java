// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;

public interface WorkFactory {
  /**
   * Start work
   */
  void start(
    InputMode inputMode,
    String inputTemplateKey,
    OutputFileMode outputFileMode,
    OutputMode outputMode,
    String outputPathPrefix,
    int outputSeconds,
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
