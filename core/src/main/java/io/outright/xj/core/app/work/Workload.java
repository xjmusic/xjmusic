// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app.work;

import io.outright.xj.core.app.exception.ConfigException;

public interface Workload {
  void start() throws ConfigException;

  void stop();
}
