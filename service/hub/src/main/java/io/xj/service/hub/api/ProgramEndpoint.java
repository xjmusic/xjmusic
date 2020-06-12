// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.rest_api.MediaType;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.PayloadDataType;
import io.xj.lib.rest_api.PayloadObject;
import io.xj.lib.util.CSV;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.*;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.UserRoleType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 Programs
 */
@Path("programs")
public class ProgramEndpoint extends HubEndpoint {
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
      Payload payload = new Payload().setDataType(PayloadDataType.HasMany);
      Collection<Program> programs;

      // how we source programs depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        programs = dao().readMany(access, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        programs = dao().readAllInAccount(access, UUID.fromString(accountId));
      else
        programs = dao().readAll(access);

      // add programs as plural data in payload
      for (Program program : programs) payload.addData(payloadFactory.toPayloadObject(program));
      Set<UUID> programIds = DAO.idsFrom(programs);

      // if included, seek and add events to payload
      if (Objects.nonNull(include) && include.contains("memes")) {
        for (ProgramMeme meme : programMemeDAO.readMany(access, programIds))
          payload.getIncluded().add(payloadFactory.toPayloadObject(meme));
        for (ProgramSequenceBindingMeme programMeme : programSequenceBindingMemeDAO.readAllForPrograms(access, programIds))
          payload.getIncluded().add(payloadFactory.toPayloadObject(programMeme));
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
      Program program = payloadFactory.consume(dao().newInstance(), payload);
      Payload responsePayload = new Payload();
      if (Objects.nonNull(cloneId)) {
        DAOCloner<Program> cloner = dao().clone(access, UUID.fromString(cloneId), program);
        responsePayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<PayloadObject> list = new ArrayList<>();
        for (Entity entity : cloner.getChildClones()) {
          PayloadObject payloadObject = payloadFactory.toPayloadObject(entity);
          list.add(payloadObject);
        }
        responsePayload.setIncluded(list);
      } else {
        responsePayload.setDataOne(payloadFactory.toPayloadObject(dao().create(access, program)));
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

      Payload payload = new Payload().setDataOne(payloadFactory.toPayloadObject(dao().readOne(access, uuid)));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<PayloadObject> list = new ArrayList<>();
        for (Entity entity : dao().readChildEntities(access, ImmutableList.of(uuid), CSV.split(include))) {
          PayloadObject payloadObject = payloadFactory.toPayloadObject(entity);
          list.add(payloadObject);
        }
        payload.setIncluded(
          list);
      }

      return response.ok(payload);

    } catch (HubException ignored) {
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
