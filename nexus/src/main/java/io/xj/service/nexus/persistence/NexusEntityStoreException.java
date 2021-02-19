// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.persistence;

/**
 Exception for Nexus Entity Store
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public class NexusEntityStoreException extends Exception {

  /**
   Construct Nexus Entity Store Exception

   @param message throwable to wrap in exception
   */
  public NexusEntityStoreException(String message) {
    super(message);
  }

  /**
   Construct Nexus Entity Store Exception

   @param cause throwable to wrap in exception
   */
  public NexusEntityStoreException(Throwable cause) {
    super(cause);
  }

  /**
   Construct Nexus Entity Store Exception

   @param message for exception
   @param cause   throwable to wrap in exception
   */
  public NexusEntityStoreException(String message, Throwable cause) {
    super(String.format("%s because %s", message, cause.getMessage()));
  }
}
