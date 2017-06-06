// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.exception;

import io.xj.core.util.Text;

public class NetworkException extends Exception {

  public NetworkException(String msg) {
    super(msg);
  }

  public NetworkException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

}
