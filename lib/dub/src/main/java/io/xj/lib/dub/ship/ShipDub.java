// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.dub.ship;

import io.xj.lib.core.exception.CoreException;
import io.xj.lib.craft.exception.CraftException;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public interface ShipDub {

  /**
   perform delivery for the current segment
   */
  void doWork() throws CoreException, CraftException;

}
