// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.user;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.DAO;
import io.xj.core.dao.UserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.math.BigInteger;

/**
 Current user
 */
@Path("users/me")
public class MeResource extends HubResource {
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);

  /**
   Get current authentication.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.USER})
  public Response getCurrentlyAuthenticatedUser(@Context ContainerRequestContext crc) {
    BigInteger userId;
    try {
      userId = Access.fromContext(crc).getUserId();
    } catch (CoreException e) {
      return response.unauthorized();
    }

    return readOne(crc, dao(), userId.toString());
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private DAO dao() {
    return injector.getInstance(UserDAO.class);
  }
}
