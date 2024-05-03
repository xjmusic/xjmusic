// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.telemetry;

import static io.xj.hub.util.StringUtils.formatStackTrace;

public class TelemetryException extends Exception {

  public TelemetryException(String msg) {
    super(msg);
  }

  public TelemetryException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), formatStackTrace(e)));
  }

  public TelemetryException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), formatStackTrace(targetException)));
  }
}
