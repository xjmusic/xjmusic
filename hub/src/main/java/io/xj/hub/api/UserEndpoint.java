// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAO;
import io.xj.hub.dao.UserDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.HubEndpoint;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.UUID;

/**
 Current user
 */
@Path("api/1")
public class UserEndpoint extends HubEndpoint {
  private final UserDAO dao;

  /**
   Constructor
   */
  @Inject
  public UserEndpoint(
    UserDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Get all users.

   @return application/json response.
   */
  @GET
  @Path("users")
  @RolesAllowed(USER)
  public Response readMany(@Context ContainerRequestContext crc) {
    return readMany(crc, dao(), ImmutableList.of());
  }

  /**
   Get one user.

   @return application/json response.
   */
  @GET
  @Path("users/{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one User.

   @param jsonapiPayload with which to update User record.
   @return Response.
   */
  @PATCH
  @Path("users/{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ADMIN)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Get current authentication.

   @return application/json response.
   */
  @GET
  @Path("users/me")
  @RolesAllowed({USER})
  public Response getCurrentlyAuthenticatedUser(@Context ContainerRequestContext crc) {
    UUID userId;
    userId = HubAccess.fromContext(crc).getUserId();

    return readOne(crc, dao(), Objects.requireNonNull(userId).toString());
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private DAO<User> dao() {
    return dao;
  }
}
