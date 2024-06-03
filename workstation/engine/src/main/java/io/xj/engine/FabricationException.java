// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine;

import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;

public class FabricationException extends Exception {

  public FabricationException(String msg) {
    super(msg);
  }

  public FabricationException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", StringUtils.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public FabricationException(Throwable e) {
    super(null, e);
  }

  public FabricationException() {
    super();
  }
}
