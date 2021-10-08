// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import java.util.concurrent.RecursiveAction;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public abstract class ChunksPublisher extends RecursiveAction {
  /**
   * Invoke the recursive action
   */
  public abstract void compute();
}
