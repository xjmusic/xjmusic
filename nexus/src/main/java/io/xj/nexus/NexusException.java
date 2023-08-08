// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus;

import io.xj.hub.util.StringUtils;
import org.jetbrains.annotations.Nullable;

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
