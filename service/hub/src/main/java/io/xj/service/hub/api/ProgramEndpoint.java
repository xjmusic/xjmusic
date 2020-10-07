// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.PayloadObject;
import io.xj.lib.util.CSV;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.DAOCloner;
import io.xj.service.hub.dao.DAOException;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.dao.ProgramMemeDAO;
import io.xj.service.hub.dao.ProgramSequenceBindingMemeDAO;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.UserRoleType;

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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 Programs
 */
@Path("programs")
public class ProgramEndpoint extends HubEndpoint {
  private final ProgramSequenceBindingMemeDAO programSequenceBindingMemeDAO;
  private final ProgramDAO dao;
  private final ProgramMemeDAO programMemeDAO;

  /**
   Constructor
   */
  @Inject
  public ProgramEndpoint(
    ProgramDAO dao,
    ProgramSequenceBindingMemeDAO programSequenceBindingMemeDAO,
    ProgramMemeDAO programMemeDAO,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
    this.programSequenceBindingMemeDAO = programSequenceBindingMemeDAO;
    this.programMemeDAO = programMemeDAO;
  }

  /**
   Get all programs.

   @param accountId to get programs for
   @param libraryId to get programs for
   @param detailed  whether to include memes and bindings
   @return set of all programs
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readMany(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("libraryId") String libraryId,
    @QueryParam("detailed") Boolean detailed
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      Payload payload = new Payload().setDataType(PayloadDataType.Many);
      Collection<Program> programs;

      // how we source programs depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        programs = dao().readMany(hubAccess, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        programs = dao().readManyInAccount(hubAccess, UUID.fromString(accountId));
      else
        programs = dao().readMany(hubAccess);

      // add programs as plural data in payload
      for (Program program : programs) payload.addData(payloadFactory.toPayloadObject(program));
      Set<UUID> programIds = Entity.idsOf(programs);

      // if detailed, add Program Memes
      if (Objects.nonNull(detailed) && detailed)
        payload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programMemeDAO.readMany(hubAccess, programIds)));

      // if detailed, add Program Sequence Binding Memes
      if (Objects.nonNull(detailed) && detailed)
        payload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programSequenceBindingMemeDAO.readMany(hubAccess, programIds)));

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
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(
    Payload payload,
    @Context ContainerRequestContext crc,
    @QueryParam("cloneId") String cloneId
  ) {

    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      Program program = payloadFactory.consume(dao().newInstance(), payload);
      Payload responsePayload = new Payload();
      if (Objects.nonNull(cloneId)) {
        DAOCloner<Program> cloner = dao().clone(hubAccess, UUID.fromString(cloneId), program);
        responsePayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<PayloadObject> list = new ArrayList<>();
        for (Entity entity : cloner.getChildClones()) {
          PayloadObject payloadObject = payloadFactory.toPayloadObject(entity);
          list.add(payloadObject);
        }
        responsePayload.setIncluded(list);
      } else {
        responsePayload.setDataOne(payloadFactory.toPayloadObject(dao().create(hubAccess, program)));
      }

      return response.create(responsePayload);

    } catch (Exception e) {
      return response.notAcceptable(e);
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
      HubAccess hubAccess = HubAccess.fromContext(crc);
      UUID uuid = UUID.fromString(String.valueOf(id));

      Payload payload = new Payload().setDataOne(payloadFactory.toPayloadObject(dao().readOne(hubAccess, uuid)));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<PayloadObject> list = new ArrayList<>();
        for (Entity entity : dao().readChildEntities(hubAccess, ImmutableList.of(uuid), CSV.split(include))) {
          PayloadObject payloadObject = payloadFactory.toPayloadObject(entity);
          list.add(payloadObject);
        }
        payload.setIncluded(
          list);
      }

      return response.ok(payload);

    } catch (DAOException ignored) {
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
  @Consumes(MediaType.APPLICATION_JSONAPI)
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
      dao().destroy(HubAccess.fromContext(crc), UUID.fromString(id));
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
