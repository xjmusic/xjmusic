// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;


public class FormatException extends Exception {

  public FormatException(String msg) {
    super(msg);
  }

  public FormatException(Exception e) {
    super(e);
  }

  public FormatException(String msg, Exception e) {
    super(msg, e);
  }
}
