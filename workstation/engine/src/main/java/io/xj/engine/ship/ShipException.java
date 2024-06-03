// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.ship;

import io.xj.model.util.StringUtils;
import jakarta.annotation.Nullable;

public class ShipException extends Exception {

  public ShipException(String msg) {
    super(msg);
  }

  public ShipException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", StringUtils.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }
}
