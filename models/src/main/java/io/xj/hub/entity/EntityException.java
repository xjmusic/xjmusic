// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.entity;

/**
 * Exception for Entity
 * <p>
 * Created by Charney Kaye on 2020/03/05
 */
public class EntityException extends Exception {

  /**
   * Construct Entity Exception
   *
   * @param message for exception
   */
  public EntityException(String message) {
    super(message);
  }

  /**
   * Construct Entity Exception
   *
   * @param e exception
   */
  public EntityException(Exception e) {
    super(e);
  }

  /**
   * Construct Entity Exception
   *
   * @param message for exception
   * @param cause   throwable to wrap in exception
   */
  public EntityException(String message, Throwable cause) {
    super(String.format("%s because %s", message, cause.getMessage()), cause);
  }
}
