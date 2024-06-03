// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.entity;

/**
 Exception for Entity Store
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public class EntityStoreException extends Exception {

  /**
   Construct Entity Store Exception

   @param message throwable to wrap in exception
   */
  public EntityStoreException(String message) {
    super(message);
  }

  /**
   Construct Entity Store Exception

   @param cause throwable to wrap in exception
   */
  public EntityStoreException(Throwable cause) {
    super(cause);
  }

  /**
   Construct Entity Store Exception

   @param message for exception
   @param cause   throwable to wrap in exception
   */
  public EntityStoreException(String message, Throwable cause) {
    super(String.format("%s because %s", message, cause.getMessage()));
  }
}
