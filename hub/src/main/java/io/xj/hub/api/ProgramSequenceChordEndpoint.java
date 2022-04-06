// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ProgramSequenceChordManager;
import io.xj.hub.manager.ProgramSequenceChordVoicingManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.PayloadDataType;

import javax.annotation.Nullable;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 ProgramSequenceChord endpoint
 */
@Path("api/1/program-sequence-chords")
public class ProgramSequenceChordEndpoint extends HubJsonapiEndpoint<ProgramSequenceChord> {
  private final ProgramSequenceChordManager manager;
  private final ProgramSequenceChordVoicingManager voicingManager;

  /**
   Constructor
   */
  @Inject
  public ProgramSequenceChordEndpoint(
    ProgramSequenceChordManager manager,
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
   Create a program sequence chord, optionally cloning voicings of an existing chord
   <p>
   Chord Search while composing a main program
   https://www.pivotaltracker.com/story/show/178921705
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(
    @Context ContainerRequestContext crc,
    JsonapiPayload jsonapiPayload,
    @Nullable @QueryParam("cloneId") UUID cloneId
  ) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      ProgramSequenceChord programSequenceChord = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<ProgramSequenceChord> cloner = manager().clone(access, cloneId, programSequenceChord);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(access, programSequenceChord)));
      }

      return response.create(responseJsonapiPayload);

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Get one ProgramSequenceChord by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return readOne(crc, manager(), id);
  }

  /**
   Get Chords in program sequence (by specifying programSequenceId)
   or
   Chord Search while composing a main program (by specifying search chord name and libraryId)
   https://www.pivotaltracker.com/story/show/178921705

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(
    @Context ContainerRequestContext crc,
    @Nullable @QueryParam("programSequenceId") UUID programSequenceId, // all-chords-for-sequence mode
    @Nullable @QueryParam("libraryId") UUID libraryId, // chord-search mode
    @Nullable @QueryParam("search") String chordName // chord-search mode
  ) {
    if (Objects.nonNull(programSequenceId) && Objects.nonNull(libraryId))
      return response.failure(Response.Status.NOT_ACCEPTABLE, "Must specify either parent programSequenceId or libraryId, not both!");

    if (Objects.nonNull(programSequenceId))
      return readMany(crc, manager(), programSequenceId);

    try {
      HubAccess access = HubAccess.fromContext(crc);
      var chords = manager().search(access, libraryId, chordName);
      var voicings = voicingManager.readManyForChords(access, chords.stream().map(ProgramSequenceChord::getId).toList());

      var result = new JsonapiPayload().setDataType(PayloadDataType.Many);
      for (var chord : chords) result.addData(payloadFactory.toPayloadObject(chord));
      for (var voicing : voicings) result.addToIncluded(payloadFactory.toPayloadObject(voicing));
      return response.ok(result);

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Update one ProgramSequenceChord

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
   Delete one ProgramSequenceChord by programSequenceId and chordId

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
  private ProgramSequenceChordManager manager() {
    return manager;
  }

}
