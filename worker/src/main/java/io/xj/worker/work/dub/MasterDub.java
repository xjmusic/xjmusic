// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker.work.dub;

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
