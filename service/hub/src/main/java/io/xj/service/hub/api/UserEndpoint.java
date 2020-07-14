// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessException;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.dao.UserDAO;
import io.xj.service.hub.entity.UserRoleType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 Current user
 */
@Path("users")
public class UserEndpoint extends HubEndpoint {
  private UserDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public UserEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(UserDAO.class);
  }

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
   Get one user.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one User.

   @param payload with which to update User record.
   @return Response.
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(UserRoleType.ADMIN)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Get current authentication.

   @return application/json response.
   */
  @GET
  @Path("me")
  @RolesAllowed({UserRoleType.USER})
  public Response getCurrentlyAuthenticatedUser(@Context ContainerRequestContext crc) {
    UUID userId;
    try {
      userId = HubAccess.fromContext(crc).getUserId();
    } catch (HubAccessException e) {
      return response.unauthorized();
    }

    return readOne(crc, dao(), userId.toString());
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private DAO dao() {
    return dao;
  }
}
