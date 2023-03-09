// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * ProgramSequenceChordVoicing endpoint
 */
@Path("api/1/program-sequence-chord-voicings")
public class ProgramSequenceChordVoicingController extends HubJsonapiEndpoint {
  private final ProgramSequenceChordVoicingManager manager;

  /**
   * Constructor
   */
  public ProgramSequenceChordVoicingController(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    ProgramSequenceChordVoicingManager manager
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   * Create new programSequence chordVoicing
   *
   * @param jsonapiPayload with which to of ProgramSequence ChordVoicing
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(
    JsonapiPayload jsonapiPayload,
    HttpServletRequest req
  ) {
    if (PayloadDataType.Many == jsonapiPayload.getDataType())
      return updateMany(req, manager(), jsonapiPayload);
    return create(req, manager(), jsonapiPayload);
  }

  /**
   * Get one ProgramSequenceChordVoicing by id
   *
   * @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readOne(
    HttpServletRequest req,
    @PathParam("id") UUID id
  ) {
    return readOne(req, manager(), id);
  }

  /**
   * Get ChordVoicings in one programSequence.
   *
   * @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(
    HttpServletRequest req,
    @QueryParam("programSequenceChordId") UUID programSequenceChordId
  ) {
    return readMany(req, manager(), programSequenceChordId);
  }

  /**
   * Update one ProgramSequenceChordVoicing
   *
   * @param jsonapiPayload with which to update record.
   * @return ResponseEntity
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> update(
    JsonapiPayload jsonapiPayload,
    HttpServletRequest req,
    @PathParam("id") UUID id
  ) {
    return update(req, manager(), id, jsonapiPayload);
  }

  /**
   * Delete one ProgramSequenceChordVoicing by programSequenceId and chordVoicingId
   *
   * @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> delete(
    HttpServletRequest req,
    @PathParam("id") UUID id
  ) {
    return delete(req, manager(), id);
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private ProgramSequenceChordVoicingManager manager() {
    return manager;
  }

}
