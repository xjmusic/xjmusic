// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.AccountUserManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Account record
 */
@RestController
@RequestMapping("/api/1/account-users")
public class AccountUserController extends HubJsonapiEndpoint {
  private final AccountUserManager manager;

  /**
   * Constructor
   */
  public AccountUserController(
    AccountUserManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Get Users in one account.
   *
   * @return application/json response.
   */
  @GetMapping
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, @RequestParam("accountId") UUID accountId) {
    return readMany(req, manager(), accountId);
  }

  /**
   * Create new account user
   *
   * @param jsonapiPayload with which to of Account User
   * @return ResponseEntity
   */
  @PostMapping
  @RolesAllowed(ADMIN)
  public ResponseEntity<JsonapiPayload> create(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req) {
    return create(req, manager(), jsonapiPayload);
  }

  /**
   * Get one AccountUser by id
   *
   * @return application/json response.
   */
  @GetMapping("{id}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathVariable("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Delete one AccountUser by accountId and userId
   *
   * @return application/json response.
   */
  @DeleteMapping("{id}")
  @RolesAllowed(ADMIN)
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathVariable("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private AccountUserManager manager() {
    return manager;
  }

}
