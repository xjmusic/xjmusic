// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.hub.dao.ProgramSequenceChordVoicingDAO;
import io.xj.lib.jsonapi.*;
import io.xj.hub.HubJsonapiEndpoint;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 ProgramSequenceChordVoicing endpoint
 */
@Path("api/1/program-sequence-chord-voicings")
public class ProgramSequenceChordVoicingEndpoint extends HubJsonapiEndpoint {
  private final ProgramSequenceChordVoicingDAO dao;

  /**
   Constructor
   */
  @Inject
  public ProgramSequenceChordVoicingEndpoint(
    ProgramSequenceChordVoicingDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Create new programSequence chordVoicing

   @param jsonapiPayload with which to of ProgramSequence ChordVoicing
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    if (PayloadDataType.Many == jsonapiPayload.getDataType())
      return updateMany(crc, dao(), jsonapiPayload);
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one ProgramSequenceChordVoicing by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get ChordVoicings in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programSequenceChordId") String programSequenceChordId) {
    return readMany(crc, dao(), programSequenceChordId);
  }

  /**
   Update one ProgramSequenceChordVoicing

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
   Delete one ProgramSequenceChordVoicing by programSequenceId and chordVoicingId

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
  private ProgramSequenceChordVoicingDAO dao() {
    return dao;
  }

}
