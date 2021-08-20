// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

/**
 Exception for Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public class JsonapiException extends Exception {

  /**
   Construct XJ Music service REST API exception with message

   @param message for exception
   */
  public JsonapiException(String message) {
    super(message);
  }

  /**
   Construct XJ Music service REST API exception with message and throwable

   @param message   for exception
   @param throwable throwable to wrap in exception
   */
  public JsonapiException(String message, Throwable throwable) {
    super(message, throwable);
  }

  /**
   Create a REST API exception caused by another exception

   @param cause exception from which to create a REST API exception
   */
  public JsonapiException(Throwable cause) {
    super(cause);
  }
}
