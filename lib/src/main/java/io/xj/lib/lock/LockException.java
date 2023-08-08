// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.lock;

import io.xj.hub.util.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * When Nexus reads a chain lock hash that does not match its own, that instance gracefully terminates itself (instead of writing anything)
 * https://www.pivotaltracker.com/story/show/185119448
 */
public class LockException extends Exception {

  public LockException(String msg) {
    super(msg);
  }

  public LockException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", StringUtils.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public LockException(Throwable e) {
    super(null, e);
  }

  public LockException() {
    super();
  }
}
