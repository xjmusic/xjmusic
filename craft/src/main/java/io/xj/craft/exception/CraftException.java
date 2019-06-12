//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.exception;

import io.xj.core.util.Text;

public class CraftException extends Exception {

  public CraftException(String msg) {
    super(msg);
  }

  public CraftException(String msg, Exception e) {
    super(String.format("%s %s at %s", msg, e.toString(), Text.formatSimpleTrace(e)));
  }

}
