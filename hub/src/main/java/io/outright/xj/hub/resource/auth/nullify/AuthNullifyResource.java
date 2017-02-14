// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.auth.nullify;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.access.AccessControlProvider;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.UserDAO;
import io.outright.xj.core.model.role.Role;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Nullify current authentication
 */
@Path("auth/no")
public class AuthNullifyResource {
  private Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(AuthNullifyResource.class);
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);
  private final AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);

  /**
   * Get current authentication, destroy all known access_tokens for that user, and delete the access token browser cookie.
   *
   * @return JSONObject that will be returned as an application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) throws IOException {
    AccessControl accessControl = AccessControl.fromContext(crc);
    try {
      userDAO.destroyAllTokens(accessControl.getUserId());
      return httpResponseProvider.internalRedirectWithCookie("", accessControlProvider.newExpiredCookie());

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }
}
