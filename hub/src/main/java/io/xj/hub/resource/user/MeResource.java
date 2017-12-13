// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.user;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.UserDAO;
import io.xj.core.model.role.Role;
import io.xj.core.model.user.User;

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
