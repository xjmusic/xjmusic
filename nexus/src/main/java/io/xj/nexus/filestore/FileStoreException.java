// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.filestore;

import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class FileStoreException extends Exception {

  public FileStoreException(String msg) {
    super(msg);
  }

  public FileStoreException(String msg, Exception e) {
    super(String.format("%s %s because %s", msg, e.toString(), e.getMessage()));
  }

  public FileStoreException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), formatStackTrace(targetException)));
  }

  /**
   Format a stack trace in carriage-return-separated lines

   @param e exception to format the stack trace of
   @return formatted stack trace
   */
  static String formatStackTrace(@Nullable Throwable e) {
    if (Objects.isNull(e)) return "";
    StackTraceElement[] stack = e.getStackTrace();
    String[] stackLines = Arrays.stream(stack).map(StackTraceElement::toString).toArray(String[]::new);
    return String.join(System.getProperty("line.separator"), stackLines);
  }
}
