// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.user;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.UserDAO;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.User;

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
 Current user
 */
@Path("users/me")
public class MeResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final UserDAO DAO = injector.getInstance(UserDAO.class);

  /**
   Get current authentication.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentlyAuthenticatedUser(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        User.KEY_ONE,
        DAO.readOne(
          Access.fromContext(crc),
          Access.fromContext(crc).getUserId()));

    } catch (Exception e) {
      return response.unauthorized();
    }
  }
}
