// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.ship;

import io.xj.core.exception.BusinessException;

/**
 [#264] Segment audio is compressed to MP3 and shipped to https://segment.xj.io
 */
public interface ShipDub {

  /**
   perform delivery for the current segment
   */
  void doWork() throws BusinessException;

}
