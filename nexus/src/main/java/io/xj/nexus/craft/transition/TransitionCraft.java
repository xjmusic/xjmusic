// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.transition;

import io.xj.nexus.NexusException;

/**
 * Transition-type Instrument https://www.pivotaltracker.com/story/show/180059746
 */
public interface TransitionCraft {

  /**
   * perform craft for the current segment
   */
  void doWork() throws NexusException;

}
