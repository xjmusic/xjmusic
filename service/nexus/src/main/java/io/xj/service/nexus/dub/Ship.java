// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import io.xj.service.hub.HubException;
import io.xj.service.nexus.craft.exception.CraftException;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public interface Ship {

  /**
   perform delivery for the current segment
   */
  void doWork() throws HubException, CraftException;

}
