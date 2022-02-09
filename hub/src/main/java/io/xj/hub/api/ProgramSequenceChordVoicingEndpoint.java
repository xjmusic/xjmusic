// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 ProgramSequenceChordVoicing endpoint
 */
@Path("api/1/program-sequence-chord-voicings")
public class ProgramSequenceChordVoicingEndpoint extends HubJsonapiEndpoint<ProgramSequenceChordVoicing> {
  private final ProgramSequenceChordVoicingManager manager;

  /**
   Constructor
   */
  @Inject
  public ProgramSequenceChordVoicingEndpoint(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    ProgramSequenceChordVoicingManager manager
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
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
      return updateMany(crc, manager(), jsonapiPayload);
    return create(crc, manager(), jsonapiPayload);
  }

  /**
   Get one ProgramSequenceChordVoicing by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, manager(), id);
  }

  /**
   Get ChordVoicings in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programSequenceChordId") String programSequenceChordId) {
    return readMany(crc, manager(), programSequenceChordId);
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
    return update(crc, manager(), id, jsonapiPayload);
  }

  /**
   Delete one ProgramSequenceChordVoicing by programSequenceId and chordVoicingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, manager(), id);
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private ProgramSequenceChordVoicingManager manager() {
    return manager;
  }

}
