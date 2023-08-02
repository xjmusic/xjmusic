// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import io.xj.lib.util.StringUtils;

import javax.annotation.Nullable;

public class NexusException extends Exception {

  public NexusException(String msg) {
    super(msg);
  }

  public NexusException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", StringUtils.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public NexusException(Throwable e) {
    super(null, e);
  }

  public NexusException() {
    super();
  }
}
