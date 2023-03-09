// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ProgramSequencePatternManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * ProgramSequencePattern endpoint
 */
@Path("api/1/program-sequence-patterns")
public class ProgramSequencePatternController extends HubJsonapiEndpoint {
  private final ProgramSequencePatternManager manager;

  /**
   * Constructor
   */
  public ProgramSequencePatternController(
    ProgramSequencePatternManager manager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Create new programSequencePattern binding
   *
   * @param jsonapiPayload with which to of ProgramSequencePattern Binding
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    JsonapiPayload jsonapiPayload,
    HttpServletRequest req, HttpServletResponse res,
    @Nullable @QueryParam("cloneId") UUID cloneId
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      var programSequencePattern = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<ProgramSequencePattern> cloner = manager().clone(access, cloneId, programSequencePattern);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(access, programSequencePattern)));
      }

      return responseProvider.create(responseJsonapiPayload);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one ProgramSequencePattern by id
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
   * Get Patterns in one programSequence.
   *
   * @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, @QueryParam("programSequenceId") UUID programSequenceId) {
    return readMany(req, manager(), programSequenceId);
  }

  /**
   * Update one ProgramSequencePattern
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
   * Delete one ProgramSequencePattern by programSequenceId and patternId
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
  private ProgramSequencePatternManager manager() {
    return manager;
  }

}
