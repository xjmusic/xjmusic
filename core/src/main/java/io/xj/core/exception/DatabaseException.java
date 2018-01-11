// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.exception;

import io.xj.core.util.Text;

public class DatabaseException extends Exception {

  public DatabaseException(String msg) {
    super(msg);
  }

  public DatabaseException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

}
