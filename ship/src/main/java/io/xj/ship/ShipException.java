// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship;

import com.google.api.client.util.Strings;

import javax.annotation.Nullable;

public class ShipException extends Exception {

  public ShipException(String msg) {
    super(msg);
  }

  public ShipException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", Strings.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public ShipException(Throwable e) {
    super(null, e);
  }

  public ShipException() {
    super();
  }
}
