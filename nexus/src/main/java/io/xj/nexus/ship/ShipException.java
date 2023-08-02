// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.ship;

import io.xj.lib.util.StringUtils;

import javax.annotation.Nullable;

public class ShipException extends Exception {

  public ShipException(String msg) {
    super(msg);
  }

  public ShipException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", StringUtils.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }
}
