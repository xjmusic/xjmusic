// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.ProgramVoiceTrackManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
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
 ProgramVoiceTrack endpoint
 */
@Path("api/1/program-voice-tracks")
public class ProgramVoiceTrackEndpoint extends HubJsonapiEndpoint<ProgramVoiceTrack> {
  private final ProgramVoiceTrackManager manager;

  /**
   Constructor
   */
  @Inject
  public ProgramVoiceTrackEndpoint(
    ProgramVoiceTrackManager manager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   Create new programVoiceTrack binding

   @param jsonapiPayload with which to of ProgramVoiceTrack Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    return create(crc, manager(), jsonapiPayload);
  }

  /**
   Get one ProgramVoiceTrack by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return readOne(crc, manager(), id);
  }

  /**
   Get Bindings in one programVoiceTrack.

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programVoiceId") UUID programVoiceId) {
    return readMany(crc, manager(), programVoiceId);
  }

  /**
   Update one ProgramVoiceTrack

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
   Delete one ProgramVoiceTrack by programVoiceTrackId and bindingId

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
  private ProgramVoiceTrackManager manager() {
    return manager;
  }

}
