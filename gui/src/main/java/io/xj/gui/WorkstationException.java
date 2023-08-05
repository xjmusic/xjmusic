// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.gui;

import io.xj.lib.util.StringUtils;
import org.jetbrains.annotations.Nullable;

public class WorkstationException extends Exception {

  public WorkstationException(String msg) {
    super(msg);
  }

  public WorkstationException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", StringUtils.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public WorkstationException(Throwable e) {
    super(null, e);
  }

  public WorkstationException() {
    super();
  }
}
