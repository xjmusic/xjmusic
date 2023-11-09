// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;

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
