// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.ship;

import io.xj.hub.util.StringUtils;

import org.jetbrains.annotations.Nullable;

public class ShipException extends Exception {

  public ShipException(String msg) {
    super(msg);
  }

  public ShipException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", StringUtils.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }
}
