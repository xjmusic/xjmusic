// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.rest_api;

/**
 Exception for Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public class RestApiException extends Exception {

    /**
     Construct XJ Music service REST API exception with message

     @param message for exception
     */
    public RestApiException(String message) {
        super(message);
    }

    /**
     Construct XJ Music service REST API exception with message and throwable

     @param message   for exception
     @param throwable throwable to wrap in exception
     */
    public RestApiException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
