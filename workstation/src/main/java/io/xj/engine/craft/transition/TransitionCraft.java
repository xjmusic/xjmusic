// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.transition;

import io.xj.engine.FabricationException;

/**
 Transition-type Instrument https://github.com/xjmusic/xjmusic/issues/262
 */
public interface TransitionCraft {

  /**
   perform craft for the current segment
   */
  void doWork() throws FabricationException;

}
