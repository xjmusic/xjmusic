// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Injector;
import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadObject;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.DAOCloner;
import io.xj.service.hub.dao.ProgramSequencePatternDAO;
import io.xj.service.hub.entity.ProgramSequence;
import io.xj.service.hub.entity.ProgramSequencePattern;
import io.xj.service.hub.entity.UserRoleType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 ProgramSequencePattern endpoint
 */
@Path("program-sequence-patterns")
public class ProgramSequencePatternEndpoint extends HubEndpoint {
  private ProgramSequencePatternDAO dao;

  /**
   The constructor's @javax.inject.Inject pattern is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ProgramSequencePatternEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ProgramSequencePatternDAO.class);
  }

  /**
   Create new programSequencePattern binding

   @param payload with which to of ProgramSequencePattern Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({UserRoleType.ARTIST})
  public Response create(
    Payload payload,
    @Context ContainerRequestContext crc,
    @QueryParam("cloneId") String cloneId
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      ProgramSequencePattern programSequencePattern = payloadFactory.consume(dao().newInstance(), payload);
      Payload responsePayload = new Payload();
      if (Objects.nonNull(cloneId)) {
        DAOCloner<ProgramSequencePattern> cloner = dao().clone(hubAccess, UUID.fromString(cloneId), programSequencePattern);
        responsePayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<PayloadObject> list = new ArrayList<>();
        for (Entity entity : cloner.getChildClones()) {
          PayloadObject payloadObject = payloadFactory.toPayloadObject(entity);
          list.add(payloadObject);
        }
        responsePayload.setIncluded(list);
      } else {
        responsePayload.setDataOne(payloadFactory.toPayloadObject(dao().create(hubAccess, programSequencePattern)));
      }

      return response.create(responsePayload);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Get one ProgramSequencePattern by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get Patterns in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programSequenceId") String programSequenceId) {
    return readMany(crc, dao(), programSequenceId);
  }

  /**
   Update one ProgramSequencePattern

   @param payload with which to update record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one ProgramSequencePattern by programSequenceId and patternId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ARTIST})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramSequencePatternDAO dao() {
    return dao;
  }

}
