// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.ManagerCloner;
import io.xj.hub.manager.ProgramSequenceManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 ProgramSequence endpoint
 */
@Path("api/1/program-sequences")
public class ProgramSequenceEndpoint extends HubJsonapiEndpoint<ProgramSequence> {
  private final ProgramSequenceManager manager;

  /**
   Constructor
   */
  @Inject
  public ProgramSequenceEndpoint(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    ProgramSequenceManager manager
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   Create new programSequence binding

   @param jsonapiPayload with which to of ProgramSequence Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(
    JsonapiPayload jsonapiPayload,
    @Context ContainerRequestContext crc,
    @QueryParam("cloneId") String cloneId
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      var programSequence = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<ProgramSequence> cloner = manager().clone(hubAccess, UUID.fromString(cloneId), programSequence);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(hubAccess, programSequence)));
      }

      return response.create(responseJsonapiPayload);

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Get one ProgramSequence by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, manager(), id);
  }

  /**
   Get Bindings in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programId") String programId) {
    return readMany(crc, manager(), programId);
  }

  /**
   Update one ProgramSequence

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
   Delete one ProgramSequence by programSequenceId and bindingId

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
  private ProgramSequenceManager manager() {
    return manager;
  }

}
