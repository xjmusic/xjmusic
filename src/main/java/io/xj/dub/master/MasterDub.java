// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import io.xj.core.exception.CoreException;
import io.xj.craft.exception.CraftException;
import io.xj.dub.exception.DubException;

/**
 [#141] Dubworker Segment mix final output of instrument-audio-arrangements
 */
public interface MasterDub {

  /**
   perform master dub for the current segment
   */
  void doWork() throws CoreException, CraftException, DubException;

}
