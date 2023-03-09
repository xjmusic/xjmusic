// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ProgramSequenceManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramSequence;
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
 * ProgramSequence endpoint
 */
@Path("api/1/program-sequences")
public class ProgramSequenceController extends HubJsonapiEndpoint {
  private final ProgramSequenceManager manager;

  /**
   * Constructor
   */
  public ProgramSequenceController(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    ProgramSequenceManager manager
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
  public ResponseEntity<JsonapiPayload> create(
    JsonapiPayload jsonapiPayload,
    HttpServletRequest req, HttpServletResponse res,
    @Nullable @QueryParam("cloneId") UUID cloneId
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      var programSequence = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<ProgramSequence> cloner = manager().clone(access, cloneId, programSequence);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(access, programSequence)));
      }

      return responseProvider.create(responseJsonapiPayload);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one ProgramSequence by id
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
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, @QueryParam("programId") UUID programId) {
    return readMany(req, manager(), programId);
  }

  /**
   * Update one ProgramSequence
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
   * Delete one ProgramSequence by programSequenceId and bindingId
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
  private ProgramSequenceManager manager() {
    return manager;
  }

}
