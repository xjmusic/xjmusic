// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface HttpResponseProvider {

  /**
   Return a simple response that a record was accepted

   @return accepted response
   */
  Response noContent();

  /**
   Return a response having read successfully

   @param jsonapiPayload of content that was read
   @return response
   */
  Response create(JsonapiPayload jsonapiPayload);

  /**
   Respond with a temporary redirect.

   @param path of redirection target.
   @return response.
   */
  Response internalRedirect(String path);

  /**
   Respond with a set-cookie in addition to a temporary redirect.

   @param path    of redirection target.
   @param cookies to set before redirection.
   @return response.
   */
  Response internalRedirectWithCookie(String path, NewCookie... cookies);

  /**
   Response with unauthorized

   @return response.
   */
  Response unauthorized();

  /**
   Response with entity (named) not authorized

   @param type       not found
   @param identifier not found
   @param cause      to include in detail
   @return response
   */
  Response unauthorized(Class<?> type, Object identifier, Throwable cause);

  /**
   Response with entity (named) not found

   @param type       not found
   @param identifier not found
   @return response
   */
  Response notFound(String type, Object identifier);

  /**
   Get a not found response from an entity type and id

   @param type       of entity not found
   @param identifier of entity not found
   @return not found response
   */
  Response notFound(Class<?> type, Object identifier);

  /**
   Response with entity not found

   @param resource that was not found, really just has id
   @return response
   */
  Response notFound(Object resource);

  /**
   Return a response of an exception

   @param e Exception
   @return Response
   */
  Response failure(Exception e);

  /**
   Return a failure response of an exception with a code

   @param status HTTP Status Code
   @param e      Exception
   @return Response
   */
  Response failure(Response.Status status, Exception e);

  /**
   Return a failure response of a message and code

   @param status  HTTP Status Code
   @param message to include
   @return Response
   */
  Response failure(Response.Status status, String message);

  /**
   Return a failure response of a message and PayloadError

   @param status HTTP Status Code
   @param error  to include
   @return Response
   */
  Response failure(Response.Status status, PayloadError error);

  /**
   Return a response of an exception during a Create operation
   <p>
   [#175985762] 406 not-acceptable errors surface underlying causes

   @param e Exception
   @return Response
   */
  Response notAcceptable(Exception e);

  /**
   Return a response that the request is not acceptable, with a JSON error payload

   @param message in error payload
   @return response
   */
  Response notAcceptable(String message);

  /**
   Return a response having read successfully

   @param jsonapiPayload of content that was read
   @return response
   */
  Response ok(JsonapiPayload jsonapiPayload);

  /**
   Return a response having read successfully

   @param content that was read
   @return response
   */
  Response ok(String content);
}
