// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.HubEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAOCloner;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.ProgramDAO;
import io.xj.hub.dao.ProgramMemeDAO;
import io.xj.hub.dao.ProgramSequenceBindingMemeDAO;
import io.xj.lib.entity.Entities;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.util.CSV;

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
@Path("api/1/programs")
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
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
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
  @RolesAllowed(USER)
  public Response readMany(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("libraryId") String libraryId,
    @QueryParam("detailed") Boolean detailed
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<Program> programs;

      // how we source programs depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        programs = dao().readMany(hubAccess, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        programs = dao().readManyInAccount(hubAccess, accountId);
      else
        programs = dao().readMany(hubAccess);

      // add programs as plural data in payload
      for (Program program : programs) jsonapiPayload.addData(payloadFactory.toPayloadObject(program));
      Set<UUID> programIds = Entities.idsOf(programs);

      // if detailed, add Program Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programMemeDAO.readMany(hubAccess, programIds)));

      // if detailed, add Program Sequence Binding Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programSequenceBindingMemeDAO.readMany(hubAccess, programIds)));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new program

   @param jsonapiPayload with which to update Program record.
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
      Program program = payloadFactory.consume(dao().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        DAOCloner<Program> cloner = dao().clone(hubAccess, UUID.fromString(cloneId), program);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(dao().create(hubAccess, program)));
      }

      return response.create(responseJsonapiPayload);

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
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id, @QueryParam("include") String include) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      var uuid = UUID.fromString(id);

      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(dao().readOne(hubAccess, uuid)));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : dao().readChildEntities(hubAccess, ImmutableList.of(uuid), CSV.split(include))) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        jsonapiPayload.setIncluded(
          list);
      }

      return response.ok(jsonapiPayload);

    } catch (DAOException ignored) {
      return response.notFound(dao.newInstance().getClass(), id);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one program

   @param jsonapiPayload with which to update Program record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one program

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
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
