// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.user;

import com.google.common.collect.ImmutableList;
import io.xj.core.dao.DAO;
import io.xj.core.dao.UserDAO;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Current user
 */
@Path("users")
public class UserIndexResource extends HubResource {

  /**
   Get all users.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) {
    return readMany(crc, dao(), ImmutableList.of());
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private DAO dao() {
    return injector.getInstance(UserDAO.class);
  }
}
