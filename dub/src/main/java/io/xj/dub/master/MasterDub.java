// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import io.xj.core.exception.BusinessException;

/**
 [#141] Dubworker Link mix final output from instrument-audio-arrangements
 */
public interface MasterDub {

  /**
   perform master dub for the current link
   */
  void doWork() throws BusinessException;

}
