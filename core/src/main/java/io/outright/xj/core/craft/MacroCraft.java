// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.app.exception.BusinessException;

/**
 [#138] Macro-Choice for Initial Link of a Chain
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public interface MacroCraft {

  /**
   perform macro craft for the current link
   */
  void craft() throws BusinessException;

}
