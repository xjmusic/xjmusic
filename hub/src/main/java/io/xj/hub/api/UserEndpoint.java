// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.Manager;
import io.xj.hub.manager.UserManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.User;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.UUID;

/**
 * Current user
 */
@Path("api/1")
public class UserEndpoint extends HubJsonapiEndpoint {
  private final UserManager manager;

  /**
   * Constructor
   */
  public UserEndpoint(
    UserManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Get all users.
   *
   * @return application/json response.
   */
  @GET
  @Path("users")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req) {
    return readMany(req, manager(), ImmutableList.of());
  }

  /**
   * Get one user.
   *
   * @return application/json response.
   */
  @GET
  @Path("users/{id}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathParam("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Update one User.
   *
   * @param jsonapiPayload with which to update User record.
   * @return ResponseEntity.
   */
  @PATCH
  @Path("users/{id}")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ADMIN)
  public ResponseEntity<JsonapiPayload> update(JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathParam("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Get current authentication.
   *
   * @return application/json response.
   */
  @GET
  @Path("users/me")
  @RolesAllowed({USER})
  public ResponseEntity<JsonapiPayload> getCurrentlyAuthenticatedUser(HttpServletRequest req) {
    UUID userId;
    userId = HubAccess.fromRequest(req).getUserId();

    return readOne(req, manager(), Objects.requireNonNull(userId).toString());
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private Manager<User> manager() {
    return manager;
  }
}
