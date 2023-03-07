// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.manager.ProgramVoiceManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * ProgramVoice endpoint
 */
@Path("api/1/program-voices")
public class ProgramVoiceEndpoint extends HubJsonapiEndpoint {
  private final ProgramVoiceManager manager;
  private final ProgramSequenceChordVoicingManager voicingManager;

  /**
   * Constructor
   */
  public ProgramVoiceEndpoint(
    ProgramVoiceManager manager,
    ProgramSequenceChordVoicingManager voicingManager,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.voicingManager = voicingManager;
  }

  /**
   * Create new programVoice binding
   *
   * @param jsonapiPayload with which to of ProgramVoice Binding
   * @return ResponseEntity
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload, HttpServletRequest req, HttpServletResponse res) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      JsonapiPayload responseData = new JsonapiPayload();

      ProgramVoice created = manager.create(access, payloadFactory.consume(manager.newInstance(), jsonapiPayload));
      responseData.setDataOne(payloadFactory.toPayloadObject(created));
      responseData.addAllToIncluded(payloadFactory.toPayloadObjects(voicingManager.createEmptyVoicings(access, created)));
      return responseProvider.create(responseData);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get one ProgramVoice by id
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
   * Get many program voices
   *
   * @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, @QueryParam("programId") UUID programId) {
    return readMany(req, manager(), programId);
  }

  /**
   * Update one ProgramVoice
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
   * Delete one ProgramVoice by programVoiceId and bindingId
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
  private ProgramVoiceManager manager() {
    return manager;
  }

}
