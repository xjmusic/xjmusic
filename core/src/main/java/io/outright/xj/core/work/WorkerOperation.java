// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.work;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.link.Link;

public interface WorkerOperation {
  void workOn(Link link) throws BusinessException;
}
