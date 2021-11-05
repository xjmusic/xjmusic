// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.LibraryDAO;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Library;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 Libraries
 */
@Path("api/1/libraries")
public class LibraryEndpoint extends HubJsonapiEndpoint<Library> {
  private final LibraryDAO dao;

  /**
   Constructor
   */
  @Inject
  public LibraryEndpoint(
    LibraryDAO dao,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.dao = dao;
  }

  /**
   Get all libraries.

   @return application/json response.
   */
  @GET
  @RolesAllowed(USER)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("accountId") String accountId) {
    if (Objects.nonNull(accountId))
      return readMany(crc, dao(), accountId);
    else
      return readMany(crc, dao(), HubAccess.fromContext(crc).getAccountIds());
  }

  /**
   Create new library

   @param jsonapiPayload with which to update Library record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({ADMIN, ENGINEER})
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one library.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one library

   @param jsonapiPayload with which to update Library record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({ADMIN, ENGINEER})
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one library

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({ADMIN, ENGINEER})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private LibraryDAO dao() {
    return dao;
  }
}
