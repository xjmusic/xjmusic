// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppResource;
import io.xj.lib.core.dao.DAO;
import io.xj.lib.core.dao.DAOCloner;
import io.xj.lib.core.dao.ProgramDAO;
import io.xj.lib.core.dao.ProgramMemeDAO;
import io.xj.lib.core.dao.ProgramSequenceBindingMemeDAO;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.model.Program;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.payload.MediaType;
import io.xj.lib.core.payload.Payload;
import io.xj.lib.core.util.CSV;

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
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Programs
 */
@Path("programs")
public class ProgramEndpoint extends AppResource {
  private final ProgramSequenceBindingMemeDAO programSequenceBindingMemeDAO;
  private ProgramDAO dao;
  private ProgramMemeDAO programMemeDAO;

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
    programMemeDAO = injector.getInstance(ProgramMemeDAO.class);
    programSequenceBindingMemeDAO = injector.getInstance(ProgramSequenceBindingMemeDAO.class);
  }

  /**
   Get all programs.

   @param accountId to get programs for
   @param libraryId to get programs for
   @param include   (optional) "memes" or null
   @return set of all programs
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("libraryId") String libraryId,
    @QueryParam("include") String include
  ) {
    try {
      Access access = Access.fromContext(crc);
      Payload payload = new Payload();
      Collection<Program> programs;

      // how we source programs depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        programs = dao().readMany(access, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        programs = dao().readAllInAccount(access, UUID.fromString(accountId));
      else
        programs = dao().readAll(access);

      // add programs as plural data in payload
      payload.setDataEntities(programs);
      Set<UUID> programIds = DAO.idsFrom(programs);

      // if included, seek and add events to payload
      if (Objects.nonNull(include) && include.contains("memes")) {
        programMemeDAO.readMany(access, programIds)
          .forEach(programMeme -> payload.addIncluded(programMeme.toPayloadObject()));
        programSequenceBindingMemeDAO.readAllForPrograms(access, programIds)
          .forEach(programMeme -> payload.addIncluded(programMeme.toPayloadObject()));
      }

      return response.ok(payload);

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
      Payload responsePayload = new Payload();
      if (Objects.nonNull(cloneId)) {
        DAOCloner<Program> cloner = dao().clone(access, UUID.fromString(cloneId), program);
        responsePayload.setDataEntity(cloner.getClone());
        responsePayload.setIncluded(cloner.getChildClones()
          .stream().map(Entity::toPayloadObject)
          .collect(Collectors.toList()));
      } else {
        responsePayload.setDataEntity(dao().create(access, program));
      }

      return response.create(responsePayload);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Get one program with included child entities

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id, @QueryParam("include") String include) {
    try {
      Access access = Access.fromContext(crc);
      UUID uuid = UUID.fromString(String.valueOf(id));

      Payload payload = new Payload().setDataEntity(dao().readOne(access, uuid));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) payload.setIncluded(
        dao().readChildEntities(access, ImmutableList.of(uuid), CSV.split(include))
          .stream().map(Entity::toPayloadObject).collect(Collectors.toList()));

      return response.ok(payload);

    } catch (CoreException ignored) {
      return response.notFound(dao.newInstance().setId(UUID.fromString(String.valueOf(id))));

    } catch (Exception e) {
      return response.failure(e);
    }
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
