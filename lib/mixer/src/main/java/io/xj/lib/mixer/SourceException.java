// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

public class SourceException extends Exception {

  public SourceException(String msg) {
    super(msg);
  }

  public SourceException(Exception e) {
    super(e);
  }

  public SourceException(String msg, Exception e) {
    super(msg, e);
  }

}
