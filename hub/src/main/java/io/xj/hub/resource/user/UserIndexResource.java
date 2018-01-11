// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.user;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.UserDAO;
import io.xj.core.model.user_role.UserRoleType;
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
@Path("users")
public class UserIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final UserDAO DAO = injector.getInstance(UserDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  /**
   Get all users.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readMany(
        User.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }


}
