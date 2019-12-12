// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.inject.Injector;
import io.xj.core.app.AppResource;
import io.xj.core.dao.ProgramSequencePatternEventDAO;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.MediaType;
import io.xj.core.payload.Payload;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 ProgramSequencePatternEvent endpoint
 */
@Path("program-sequence-pattern-events")
public class ProgramSequencePatternEventEndpoint extends AppResource {
  private ProgramSequencePatternEventDAO dao;

  /**
   The constructor's @javax.inject.Inject patternEvent is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ProgramSequencePatternEventEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ProgramSequencePatternEventDAO.class);
  }

  /**
   Get PatternEvents in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("programSequenceId") String programSequenceId) {
    return readMany(crc, dao(), programSequenceId);
  }

  /**
   Create new programSequence patternEvent

   @param payload with which to of ProgramSequence PatternEvent
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), payload);
  }

  /**
   Get one ProgramSequencePatternEvent by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Delete one ProgramSequencePatternEvent by programSequenceId and patternEventId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramSequencePatternEventDAO dao() {
    return dao;
  }

}
