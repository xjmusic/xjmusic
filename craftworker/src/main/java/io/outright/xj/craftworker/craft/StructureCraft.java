// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker.craft;

import io.outright.xj.core.app.exception.BusinessException;

/**
 Structure craft for the current link includes rhythm and support
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public interface StructureCraft {

  /**
   perform craft for the current link
   */
  void doWork() throws BusinessException;

}
