// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.chain_gang;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.model.link.Link;

public interface ChainGangOperation {
  /**
   As long as this operation succeeds (without throwing an exception)
   the link will be updated to its next state

   @param link to work on
   @throws BusinessException on failure
   */
  void workOn(Link link) throws BusinessException, ConfigException;
}
