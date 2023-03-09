// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.ProgramSequenceBindingManager;
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
 * ProgramSequenceBinding endpoint
 */
@Path("api/1/program-sequence-bindings")
public class ProgramSequenceBindingController extends HubJsonapiEndpoint {
  private final ProgramSequenceBindingManager manager;

  /**
   * Constructor
   */
  public ProgramSequenceBindingController(
    ProgramSequenceBindingManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Create new programSequence binding
   *
   * @param jsonapiPayload with which to of ProgramSequence Binding
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload, HttpServletRequest req) {
    return create(req, manager(), jsonapiPayload);
  }

  /**
   * Get one ProgramSequenceBinding by id
   *
   * @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, @PathParam("id") UUID id) {
    return readOne(req, manager(), id);
  }

  /**
   * Get Bindings in one programSequence.
   *
   * @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, @QueryParam("programSequenceId") UUID programSequenceId) {
    return readMany(req, manager(), programSequenceId);
  }

  /**
   * Update one ProgramSequenceBinding
   *
   * @param jsonapiPayload with which to update record.
   * @return ResponseEntity
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(JsonapiPayload jsonapiPayload, HttpServletRequest req, @PathParam("id") UUID id) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one ProgramSequenceBinding by programSequenceId and bindingId
   *
   * @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, @PathParam("id") UUID id) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private ProgramSequenceBindingManager manager() {
    return manager;
  }

}
