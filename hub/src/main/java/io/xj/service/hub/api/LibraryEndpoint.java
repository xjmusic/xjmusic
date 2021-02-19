// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.LibraryDAO;

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
import java.util.Objects;

/**
 Libraries
 */
@Path("api/1/libraries")
public class LibraryEndpoint extends HubEndpoint {
  private final LibraryDAO dao;

  /**
   Constructor
   */
  @Inject
  public LibraryEndpoint(
    LibraryDAO dao,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
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
