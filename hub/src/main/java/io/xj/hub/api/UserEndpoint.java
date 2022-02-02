// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.Manager;
import io.xj.hub.manager.UserManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.User;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.UUID;

/**
 Current user
 */
@Path("api/1")
public class UserEndpoint extends HubJsonapiEndpoint<User> {
  private final UserManager manager;

  /**
   Constructor
   */
  @Inject
  public UserEndpoint(
    UserManager manager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   Get all users.

   @return application/json response.
   */
  @GET
  @Path("users")
  @RolesAllowed(USER)
  public Response readMany(@Context ContainerRequestContext crc) {
    return readMany(crc, manager(), ImmutableList.of());
  }

  /**
   Get one user.

   @return application/json response.
   */
  @GET
  @Path("users/{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, manager(), id);
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
    return update(crc, manager(), id, jsonapiPayload);
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

    return readOne(crc, manager(), Objects.requireNonNull(userId).toString());
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private Manager<User> manager() {
    return manager;
  }
}
