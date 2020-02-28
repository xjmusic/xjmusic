// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.work;

public interface Worker {
  Runnable getTaskRunnable() throws Exception;
}
