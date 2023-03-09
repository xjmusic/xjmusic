// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.AccountManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Accounts
 */
@Path("api/1/accounts")
public class AccountController extends HubJsonapiEndpoint {
  private final AccountManager manager;

  /**
   * Constructor
   */
  public AccountController(
    AccountManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Get all accounts.
   *
   * @return application/json response.
   */
  @GET
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req) {
    return readMany(req, manager(), ImmutableList.of());
  }

  /**
   * Create new account
   *
   * @param jsonapiPayload comprising new entities
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ADMIN)
  public ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload, HttpServletRequest req) {
    return create(req, manager(), jsonapiPayload);
  }

  /**
   * Get one account.
   *
   * @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathParam("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Update one account
   *
   * @param jsonapiPayload comprising updated entities
   * @return ResponseEntity
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ADMIN)
  public ResponseEntity<JsonapiPayload> update(JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathParam("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one account
   *
   * @return ResponseEntity
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ADMIN)
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathParam("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private AccountManager manager() {
    return manager;
  }
}
