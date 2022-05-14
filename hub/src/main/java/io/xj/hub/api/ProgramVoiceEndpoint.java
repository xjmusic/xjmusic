// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.manager.ProgramVoiceManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 ProgramVoice endpoint
 */
@Path("api/1/program-voices")
public class ProgramVoiceEndpoint extends HubJsonapiEndpoint<ProgramVoice> {
  private final ProgramVoiceManager manager;
  private final ProgramSequenceChordVoicingManager voicingManager;

  /**
   Constructor
   */
  @Inject
  public ProgramVoiceEndpoint(
    ProgramVoiceManager manager,
    ProgramSequenceChordVoicingManager voicingManager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.voicingManager = voicingManager;
  }

  /**
   Create new programVoice binding

   @param jsonapiPayload with which to of ProgramVoice Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      JsonapiPayload responseData = new JsonapiPayload();

      ProgramVoice created = manager.create(access, payloadFactory.consume(manager.newInstance(), jsonapiPayload));
      responseData.setDataOne(payloadFactory.toPayloadObject(created));
      responseData.addAllToIncluded(payloadFactory.toPayloadObjects(voicingManager.createEmptyVoicings(access, created)));
      return response.create(responseData);

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Get one ProgramVoice by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return readOne(crc, manager(), id);
  }

  /**
   Get many program voices

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programId") UUID programId) {
    return readMany(crc, manager(), programId);
  }

  /**
   Update one ProgramVoice

   @param jsonapiPayload with which to update record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return update(crc, manager(), id, jsonapiPayload);
  }

  /**
   Delete one ProgramVoice by programVoiceId and bindingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return delete(crc, manager(), id);
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private ProgramVoiceManager manager() {
    return manager;
  }

}
