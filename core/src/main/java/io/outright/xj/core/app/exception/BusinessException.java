// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.app.exception;

import io.outright.xj.core.util.Text;

public class BusinessException extends Exception {

  public BusinessException(String msg) {
    super(msg);
  }

  public BusinessException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

}
