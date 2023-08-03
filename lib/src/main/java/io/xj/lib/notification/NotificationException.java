// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.notification;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class NotificationException extends Exception {

  public NotificationException(String msg) {
    super(msg);
  }

  public NotificationException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), formatStackTrace(e)));
  }

  public NotificationException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), formatStackTrace(targetException)));
  }

  /**
   * Format a stack trace in carriage-return-separated lines
   *
   * @param e exception to format the stack trace of
   * @return formatted stack trace
   */
  static String formatStackTrace(@Nullable Throwable e) {
    if (Objects.isNull(e)) return "";
    StackTraceElement[] stack = e.getStackTrace();
    String[] stackLines = Arrays.stream(stack).map(StackTraceElement::toString).toArray(String[]::new);
    return String.join(System.getProperty("line.separator"), stackLines);
  }
}
