// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.app.work;

public interface Worker {
  Runnable getTaskRunnable() throws Exception;
}
