// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.background;

import io.xj.engine.FabricationException;

/**
 Background-type Instrument https://github.com/xjmusic/workstation/issues/256
 */
public interface BackgroundCraft {

  /**
   perform craft for the current segment
   */
  void doWork() throws FabricationException;

}
