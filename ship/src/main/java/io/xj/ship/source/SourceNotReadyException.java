// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.api.client.util.Strings;

import javax.annotation.Nullable;

public class SourceNotReadyException extends Exception {

  public SourceNotReadyException(String msg) {
    super(msg);
  }

  public SourceNotReadyException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", Strings.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public SourceNotReadyException(Throwable e) {
    super(null, e);
  }

  public SourceNotReadyException() {
    super();
  }
}
