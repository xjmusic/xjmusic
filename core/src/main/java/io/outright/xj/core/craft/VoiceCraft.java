// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.app.exception.BusinessException;

/**
 Voice craft for the current link includes events, arrangements, instruments, and audio
 */
public interface VoiceCraft {

  /**
   perform craft for the current link
   */
  void craft() throws BusinessException;

}
