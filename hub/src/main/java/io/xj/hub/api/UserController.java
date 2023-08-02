// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.Manager;
import io.xj.hub.manager.UserManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.User;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Current user
 */
@RestController
@RequestMapping("/api/1")
public class UserController extends HubJsonapiEndpoint {
  final UserManager manager;

  /**
   * Constructor
   */
  public UserController(
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
  @GetMapping("users")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req) {
    return readMany(req, manager(), List.of());
  }

  /**
   * Get one user.
   *
   * @return application/json response.
   */
  @GetMapping("users/{id}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathVariable("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Update one User.
   *
   * @param jsonapiPayload with which to update User record.
   * @return ResponseEntity.
   */
  @PatchMapping("users/{id}")
  @RolesAllowed(ADMIN)
  public ResponseEntity<JsonapiPayload> update(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathVariable("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Get current authentication.
   *
   * @return application/json response.
   */
  @GetMapping("users/me")
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
  Manager<User> manager() {
    return manager;
  }
}
