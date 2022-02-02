// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.*;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;
import io.xj.lib.util.CSV;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 Programs
 */
@Path("api/1/programs")
public class ProgramEndpoint extends HubJsonapiEndpoint<Program> {
  private final ProgramSequenceBindingMemeManager programSequenceBindingMemeManager;
  private final ProgramManager manager;
  private final ProgramMemeManager programMemeManager;

  /**
   Constructor
   */
  @Inject
  public ProgramEndpoint(
    ProgramManager manager,
    ProgramSequenceBindingMemeManager programSequenceBindingMemeManager,
    ProgramMemeManager programMemeManager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.programSequenceBindingMemeManager = programSequenceBindingMemeManager;
    this.programMemeManager = programMemeManager;
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
        programs = manager().readMany(hubAccess, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        programs = manager().readManyInAccount(hubAccess, accountId);
      else
        programs = manager().readMany(hubAccess);

      // add programs as plural data in payload
      for (Program program : programs) jsonapiPayload.addData(payloadFactory.toPayloadObject(program));
      Set<UUID> programIds = Entities.idsOf(programs);

      // if detailed, add Program Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programMemeManager.readMany(hubAccess, programIds)));

      // if detailed, add Program Sequence Binding Memes
      if (Objects.nonNull(detailed) && detailed)
        jsonapiPayload.addAllToIncluded(payloadFactory.toPayloadObjects(
          programSequenceBindingMemeManager.readMany(hubAccess, programIds)));

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
      Program program = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        ManagerCloner<Program> cloner = manager().clone(hubAccess, UUID.fromString(cloneId), program);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(manager().create(hubAccess, program)));
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

      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(manager().readOne(hubAccess, uuid)));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : manager().readChildEntities(hubAccess, ImmutableList.of(uuid), CSV.split(include))) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        jsonapiPayload.setIncluded(
          list);
      }

      return response.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return response.notFound(manager.newInstance().getClass(), id);

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
    return update(crc, manager(), id, jsonapiPayload);
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
      manager().destroy(HubAccess.fromContext(crc), UUID.fromString(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private ProgramManager manager() {
    return manager;
  }
}
