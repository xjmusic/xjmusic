// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker.work.craft;

import io.xj.core.app.exception.BusinessException;

/**
 Voice craft for the current link includes events, arrangements, instruments, and audio
 */
public interface VoiceCraft {

  /**
   perform craft for the current link
   */
  void doWork() throws BusinessException;

}
