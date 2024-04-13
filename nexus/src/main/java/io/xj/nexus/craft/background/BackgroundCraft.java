// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.background;

import io.xj.nexus.NexusException;

/**
 Background-type Instrument https://github.com/xjmusic/workstation/issues/256
 */
public interface BackgroundCraft {

  /**
   perform craft for the current segment
   */
  void doWork() throws NexusException;

}
