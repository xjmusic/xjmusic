// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.ship;

import io.xj.core.exception.BusinessException;

/**
 [#264] Link audio is compressed to MP3 and shipped to https://link.xj.io
 */
public interface ShipDub {

  /**
   perform delivery for the current link
   */
  void doWork() throws BusinessException;

}
