// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.exception;

import io.xj.core.util.Text;

public class AccessException extends Exception {

  public AccessException(String msg) {
    super(msg);
  }

  public AccessException(DatabaseException e) {
    super(e);
  }

  public AccessException(Exception e) {
    super(e);
  }

  public AccessException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }
}
