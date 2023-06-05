// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.yard;

import com.google.api.client.util.Strings;

import javax.annotation.Nullable;

public class YardException extends Exception {

  public YardException(String msg) {
    super(msg);
  }

  public YardException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", Strings.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public YardException(Throwable e) {
    super(null, e);
  }

  public YardException() {
    super();
  }
}
