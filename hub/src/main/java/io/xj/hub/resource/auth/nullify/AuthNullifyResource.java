// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.auth.nullify;

import io.xj.core.CoreModule;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.UserDAO;
import io.xj.core.model.role.Role;

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
 Nullify current authentication
 */
@Path("auth/no")
public class AuthNullifyResource {
  private Injector injector = Guice.createInjector(new CoreModule());
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);

  /**
   Get current authentication, destroy all known access_tokens for that user, and delete the access token browser cookie.

   @return JSONObject that will be returned as an application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) throws IOException {
    Access access = Access.fromContext(crc);
    try {
      userDAO.destroyAllTokens(access.getUserId());
      return response.internalRedirectWithCookie("", accessControlProvider.newExpiredCookie());

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
