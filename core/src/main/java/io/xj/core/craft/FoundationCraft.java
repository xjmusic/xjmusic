// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.craft;

import io.xj.core.exception.BusinessException;

/**
 [#138] Foundation craft for Initial Link of a Chain
 [#214] If a Chain has Patterns associated with it directly, prefer those choices to any in the Library
 */
public interface FoundationCraft {

  /**
   perform macro craft for the current link
   */
  void doWork() throws BusinessException;

}
