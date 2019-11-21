// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.access.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.model.Program;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.MediaType;
import io.xj.core.payload.Payload;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
public class ProgramIndexResource extends HubResource {

  @QueryParam("accountId")
  String accountId;

  @QueryParam("libraryId")
  String libraryId;

  @QueryParam("cloneId")
  String cloneId;

  /**
   Get all programs.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) {
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
  public Response create(Payload payload, @Context ContainerRequestContext crc) {

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
   Get DAO of injector

   @return DAO
   */
  private ProgramDAO dao() {
    return injector.getInstance(ProgramDAO.class);
  }
}
