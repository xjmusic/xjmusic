// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.dao.ProgramVoiceTrackDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 ProgramVoiceTrack endpoint
 */
@Path("api/1/program-voice-tracks")
public class ProgramVoiceTrackEndpoint extends HubJsonapiEndpoint {
  private final ProgramVoiceTrackDAO dao;

  /**
   Constructor
   */
  @Inject
  public ProgramVoiceTrackEndpoint(
    ProgramVoiceTrackDAO dao,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, payloadFactory);
    this.dao = dao;
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
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one ProgramVoiceTrack by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get Bindings in one programVoiceTrack.

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programVoiceId") String programVoiceId) {
    return readMany(crc, dao(), programVoiceId);
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
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one ProgramVoiceTrack by programVoiceTrackId and bindingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramVoiceTrackDAO dao() {
    return dao;
  }

}
