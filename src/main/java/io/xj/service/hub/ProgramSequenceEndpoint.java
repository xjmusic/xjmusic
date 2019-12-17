// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.inject.Injector;
import io.xj.core.access.Access;
import io.xj.core.app.AppResource;
import io.xj.core.dao.DAOCloner;
import io.xj.core.dao.ProgramSequenceDAO;
import io.xj.core.entity.Entity;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.MediaType;
import io.xj.core.payload.Payload;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 ProgramSequence endpoint
 */
@Path("program-sequences")
public class ProgramSequenceEndpoint extends AppResource {
  private ProgramSequenceDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ProgramSequenceEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ProgramSequenceDAO.class);
  }

  /**
   Create new programSequence binding

   @param payload with which to of ProgramSequence Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ARTIST})
  public Response create(
    Payload payload,
    @Context ContainerRequestContext crc,
    @QueryParam("cloneId") String cloneId
  ) {

    try {
      Access access = Access.fromContext(crc);
      ProgramSequence programSequence = dao().newInstance().consume(payload);
      Payload responsePayload = new Payload();
      if (Objects.nonNull(cloneId)) {
        DAOCloner<ProgramSequence> cloner = dao().clone(access, UUID.fromString(cloneId), programSequence);
        responsePayload.setDataEntity(cloner.getClone());
        responsePayload.setIncluded(cloner.getChildClones()
          .stream().map(Entity::toPayloadObject)
          .collect(Collectors.toList()));
      } else {
        responsePayload.setDataEntity(dao().create(access, programSequence));
      }

      return response.create(responsePayload);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Get one ProgramSequence by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get Bindings in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("programId") String programId) {
    return readMany(crc, dao(), programId);
  }

  /**
   Update one ProgramSequence

   @param payload with which to update record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one ProgramSequence by programSequenceId and bindingId

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
  private ProgramSequenceDAO dao() {
    return dao;
  }

}
