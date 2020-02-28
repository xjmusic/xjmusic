// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.exception;

import io.xj.lib.core.util.Text;

public class CraftException extends Exception {

  public CraftException(String msg) {
    super(msg);
  }

  public CraftException(String msg, Exception e) {
    super(String.format("%s %s at %s", msg, e.toString(), Text.formatSimpleTrace(e)));
  }

}
