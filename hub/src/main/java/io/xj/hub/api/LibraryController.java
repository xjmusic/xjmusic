// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.LibraryManager;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Library;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.jetbrains.annotations.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Libraries
 */
@RestController
@RequestMapping("/api/1/libraries")
public class LibraryController extends HubJsonapiEndpoint {
  final LibraryManager manager;

  /**
   * Constructor
   */
  public LibraryController(
    LibraryManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Get all libraries.
   *
   * @return application/json response.
   */
  @GetMapping
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req,
    @Nullable @RequestParam("accountId") UUID accountId
  ) {
    if (Objects.nonNull(accountId))
      return readMany(req, manager(), accountId);
    else
      return readMany(req, manager(), HubAccess.fromRequest(req).getAccountIds());
  }

  /**
   * Create new library, potentially cloning an existing library
   *
   * @param jsonapiPayload with which to update Library record.
   * @return ResponseEntity
   */
  @PostMapping
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    @RequestBody JsonapiPayload jsonapiPayload,
    HttpServletRequest req,
    @Nullable @RequestParam("cloneId") UUID cloneId
  ) {

    try {
      HubAccess access = HubAccess.fromRequest(req);
      Library library = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<Library> cloner = manager().clone(access, cloneId, library);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(access, library)));
      }

      return responseProvider.create(responseJsonapiPayload);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one library.
   *
   * @return application/json response.
   */
  @GetMapping("{id}")
  @RolesAllowed(USER)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathVariable("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Update one library
   *
   * @param jsonapiPayload with which to update Library record.
   * @return ResponseEntity
   */
  @PatchMapping("{id}")
  @RolesAllowed({ADMIN, ENGINEER})
  public ResponseEntity<JsonapiPayload> update(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathVariable("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one library
   *
   * @return ResponseEntity
   */
  @DeleteMapping("{id}")
  @RolesAllowed({ADMIN, ENGINEER})
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathVariable("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  LibraryManager manager() {
    return manager;
  }
}
