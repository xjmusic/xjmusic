package io.outright.xj.core.app.server;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public interface HttpResponseProvider {
  /**
   * Respond with a temporary redirect.
   *
   * @param path of redirection target.
   * @return response.
   */
  Response internalRedirect(String path);

  /**
   * Respond with a set-cookie in addition to a temporary redirect.
   *
   * @param path of redirection target.
   * @param cookies to set before redirection.
   * @return response.
   */
  Response internalRedirectWithCookie(String path, NewCookie... cookies);

  /**
   * Respond with a set-cookie in addition to unauthorized
   *
   * @param cookies to set before unauthorized.
   * @return response.
   */
  Response unauthorizedWithCookie(NewCookie... cookies);

  /**
   * Response with unauthorized
   * @return response.
   */
  Response unauthorized();

  /**
   * Response with entity (named) not found
   * @param entityName not found
   * @return response
   */
  Response notFound(String entityName);
}
