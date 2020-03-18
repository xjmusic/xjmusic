// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.dub;

import io.xj.lib.core.util.Text;

public class DubException extends Exception {

  public DubException(String msg) {
    super(msg);
  }

  public DubException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), Text.formatStackTrace(e)));
  }

}
