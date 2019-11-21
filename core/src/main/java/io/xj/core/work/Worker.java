// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work;

public interface Worker {
  Runnable getTaskRunnable() throws Exception;
}
