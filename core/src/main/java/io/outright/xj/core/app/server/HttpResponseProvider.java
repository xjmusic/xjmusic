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
  Response temporaryRedirect(String path);

  /**
   * Respond with a set-cookie in addition to a temporary redirect.
   *
   * @param path of redirection target.
   * @param cookies to set before redirection.
   * @return response.
   */
  Response temporaryRedirectWithCookie(String path, NewCookie... cookies);

  /**
   * Respond with Not Authorized.
   *
   * @return reponse.
   */
  Response unauthorized();
}
