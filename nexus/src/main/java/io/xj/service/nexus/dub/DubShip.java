// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.NexusException;

/**
 [#264] Segment audio is compressed to OGG and shipped to https://segment.xj.io
 */
public interface DubShip {

  /**
   perform delivery for the current segment
   */
  void doWork() throws NexusException, NexusException;

}
