// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.exception;

import io.xj.core.util.Text;

public class BusinessException extends Exception {

  public BusinessException(String msg) {
    super(msg);
  }

  public BusinessException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

}
