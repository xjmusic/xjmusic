// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.core.access.Access;
import io.xj.core.app.AppResource;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.model.Program;
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

/**
 Programs
 */
@Path("programs")
public class ProgramEndpoint extends AppResource {
  private ProgramDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ProgramEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ProgramDAO.class);
  }

  /**
   Get all programs.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("libraryId") String libraryId
  ) {
    Access access = Access.fromContext(crc);
    try {

      if (Objects.nonNull(libraryId) && !libraryId.isEmpty()) {
        // read all in library
        return response.ok(
          new Payload().setDataEntities(
            dao().readMany(
              access,
              ImmutableList.of(UUID.fromString(libraryId)))));

      } else if (Objects.nonNull(accountId) && !accountId.isEmpty()) {
        // read all in account
        return response.ok(
          new Payload().setDataEntities(
            dao().readAllInAccount(
              access,
              UUID.fromString(accountId))));

      } else {
        // read all we have access to
        return response.ok(
          new Payload().setDataEntities(
            dao().readAll(
              access)));
      }

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new program

   @param payload with which to update Program record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(
    Payload payload,
    @Context ContainerRequestContext crc,
    @QueryParam("cloneId") String cloneId
  ) {

    try {
      Access access = Access.fromContext(crc);
      Program program = dao().newInstance().consume(payload);
      Program created;
      if (Objects.nonNull(cloneId))
        created = dao().clone(access, UUID.fromString(cloneId), program);
      else
        created = dao().create(access, program);

      return response.create(new Payload().setDataEntity(created));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Get one program.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one program

   @param payload with which to update Program record.
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
   Delete one program

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(UserRoleType.ADMIN)
  public Response destroy(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    try {
      dao().destroy(Access.fromContext(crc), UUID.fromString(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramDAO dao() {
    return dao;
  }
}
