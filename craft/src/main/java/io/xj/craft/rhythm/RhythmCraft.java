// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import io.xj.core.exception.BusinessException;

/**
 Structure craft for the current link includes rhythm and harmonicDetail
 [#214] If a Chain has Patterns associated with it directly, prefer those choices to any in the Library
 */
public interface RhythmCraft {

  /**
   perform craft for the current link
   */
  void doWork() throws BusinessException;

}
