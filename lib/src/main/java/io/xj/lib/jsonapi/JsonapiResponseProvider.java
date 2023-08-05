// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.jsonapi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface JsonapiResponseProvider {

  /**
   * Return a simple responseEntity<JsonapiPayload> that a record was accepted
   *
   * @return accepted responseEntity<JsonapiPayload>
   */
  ResponseEntity<String> noContent();

  /**
   * Return a simple responseEntity<String> that a record was deleted OK
   *
   * @return accepted responseEntity<String>
   */
  ResponseEntity<JsonapiPayload> deletedOk();

  /**
   * Return a responseEntity<JsonapiPayload> having read successfully
   *
   * @param jsonapiPayload of content that was read
   * @return responseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload);

  /**
   * ResponseEntity<JsonapiPayload> with unauthorized
   *
   * @return responseEntity<JsonapiPayload>.
   */
  ResponseEntity<JsonapiPayload> unauthorized();

  /**
   * ResponseEntity<JsonapiPayload> with entity (named) not authorized
   *
   * @param type       not found
   * @param identifier not found
   * @param cause      to include in detail
   * @return responseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> unauthorized(Class<?> type, Object identifier, Throwable cause);

  /**
   * ResponseEntity<JsonapiPayload> with entity (named) not found
   *
   * @param type       not found
   * @param identifier not found
   * @return responseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> notFound(String type, Object identifier);

  /**
   * Get a not found responseEntity<JsonapiPayload> from an entity type and id
   *
   * @param type       of entity not found
   * @param identifier of entity not found
   * @return not found responseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> notFound(Class<?> type, Object identifier);

  /**
   * ResponseEntity<JsonapiPayload> with entity not found
   *
   * @param resource that was not found, really just has id
   * @return responseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> notFound(Object resource);

  /**
   * Return a responseEntity<String> of an exception
   *
   * @param e Exception
   * @return ResponseEntity<String>
   */
  ResponseEntity<String> failureText(Exception e);

  /**
   * Return a responseEntity<JsonapiPayload> of an exception
   *
   * @param e Exception
   * @return ResponseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> failure(Exception e);

  /**
   * Return a failure responseEntity<JsonapiPayload> of an exception with a code
   *
   * @param status HTTP Status Code
   * @param e      Exception
   * @return ResponseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> failure(HttpStatus status, Exception e);

  /**
   * Return a failure responseEntity<JsonapiPayload> of a message and code
   *
   * @param status  HTTP Status Code
   * @param message to include
   * @return ResponseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> failure(HttpStatus status, String message);

  /**
   * Return a failure responseEntity<JsonapiPayload> of a message and PayloadError
   *
   * @param status HTTP Status Code
   * @param error  to include
   * @return ResponseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> failure(HttpStatus status, PayloadError error);

  /**
   * Return a responseEntity<JsonapiPayload> of an exception during a Create operation
   * <p>
   * 406 not-acceptable errors surface underlying causes https://www.pivotaltracker.com/story/show/175985762
   *
   * @param e Exception
   * @return ResponseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> notAcceptable(Exception e);

  /**
   * Return a responseEntity<JsonapiPayload> that the request is not acceptable, with a JSON error payload
   *
   * @param message in error payload
   * @return responseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> notAcceptable(String message);

  /**
   * Return a responseEntity<JsonapiPayload> having read successfully
   *
   * @param jsonapiPayload of content that was read
   * @return responseEntity<JsonapiPayload>
   */
  ResponseEntity<JsonapiPayload> ok(JsonapiPayload jsonapiPayload);

  /**
   * Return a responseEntity<JsonapiPayload> having read successfully
   *
   * @param content that was read
   * @return responseEntity<JsonapiPayload>
   */
  ResponseEntity<String> ok(String content);
}
