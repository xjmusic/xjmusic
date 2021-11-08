// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import com.google.api.client.util.Strings;

import javax.annotation.Nullable;

public class NexusException extends Exception {

  public NexusException(String msg) {
    super(msg);
  }

  public NexusException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", Strings.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public NexusException(Throwable e) {
    super(null, e);
  }

  public NexusException() {
    super();
  }
}
