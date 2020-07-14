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

   @param payload of content that was read
   @return response
   */
  Response create(Payload payload);

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
   @return response
   */
  Response unauthorized(Class<?> type, Object identifier);

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
   Return a response of an exception

   @param e    Exception
   @param code HTTP Status Code
   @return Response
   */
  Response failure(Exception e, int code);

  /**
   Return a response of an exception during a Create operation

   @param e Exception
   @return Response
   */
  Response failureToCreate(Exception e);

  /**
   Return a response of an exception during an Update operation

   @param e Exception
   @return Response
   */
  Response failureToUpdate(Exception e);

  /**
   Return a response that the request is not acceptable, with a JSON error payload

   @param message in error payload
   @return response
   */
  Response notAcceptable(String message);

  /**
   Return a response having read successfully

   @param payload of content that was read
   @return response
   */
  Response ok(Payload payload);

  /**
   Return a response having read successfully

   @param content that was read
   @return response
   */
  Response ok(String content);
}
