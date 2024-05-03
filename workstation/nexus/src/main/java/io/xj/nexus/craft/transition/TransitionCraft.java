// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.transition;

import io.xj.nexus.NexusException;

/**
 Transition-type Instrument https://github.com/xjmusic/workstation/issues/262
 */
public interface TransitionCraft {

  /**
   perform craft for the current segment
   */
  void doWork() throws NexusException;

}
