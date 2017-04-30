package io.outright.xj.core.app.server;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface HttpResponseProvider {
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
   Respond with a set-cookie in addition to unauthorized

   @param cookies to set before unauthorized.
   @return response.
   */
  Response unauthorizedWithCookie(NewCookie... cookies);

  /**
   Response with unauthorized

   @return response.
   */
  Response unauthorized();

  /**
   Response with entity (named) not found

   @param entityName not found
   @return response
   */
  Response notFound(String entityName);

  /**
   Return a response from an exception

   @param e Exception
   @return Response
   */
  Response failure(Exception e);

  /**
   Return a response from an exception

   @param e    Exception
   @param code HTTP Status Code
   @return Response
   */
  Response failure(Exception e, int code);

  /**
   Return a response from an exception during a Create operation

   @param e Exception
   @return Response
   */
  Response failureToCreate(Exception e);

  /**
   Return a response from an exception during an Update operation

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
}
