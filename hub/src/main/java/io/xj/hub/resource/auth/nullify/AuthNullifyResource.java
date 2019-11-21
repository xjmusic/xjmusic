// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.auth.nullify;

import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.Access;
import io.xj.core.dao.UserDAO;
import io.xj.hub.HubResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Nullify current authentication
 */
@Path("auth/no")
public class AuthNullifyResource extends HubResource {
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);
  private final AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);

  /**
   Get current authentication, destroy all known access_tokens for that user, and delete the access token browser cookie.

   @return JSONObject that will be returned as an application/json response.
   */
  @GET
  @PermitAll
  public Response nullifyAuthentication(@Context ContainerRequestContext crc) {
    Access access = Access.fromContext(crc);

    if (access.isValid()) {
      try {
        userDAO.destroyAllTokens(access.getUserId());
        return response.internalRedirectWithCookie("", accessControlProvider.newExpiredCookie());

      } catch (Exception e) {
        return response.failure(e);
      }

    } else
      return response.internalRedirectWithCookie("", accessControlProvider.newExpiredCookie());
  }

}
