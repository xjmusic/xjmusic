// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft;

import io.xj.core.exception.BusinessException;

/**
 Voice craft for the current link includes events, arrangements, instruments, and audio
 */
public interface VoiceCraft {

  /**
   perform craft for the current link
   */
  void doWork() throws BusinessException;

}
