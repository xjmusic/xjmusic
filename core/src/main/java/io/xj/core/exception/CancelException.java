// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.exception;

import io.xj.core.util.Text;

public class CancelException extends Exception {

  public CancelException(String msg) {
    super(msg);
  }

  public CancelException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

}
