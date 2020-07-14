// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

/**
 XJ Music service App Exception
 */
public class AppException extends Exception {

  /**
   Construct XJ Music service App exception with message

   @param message for exception
   */
  public AppException(String message) {
    super(message);
  }

  /**
   Construct XJ Music service App exception with message and throwable

   @param message   for exception
   @param throwable throwable to wrap in exception
   */
  public AppException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
