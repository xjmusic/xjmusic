// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.hub.dao.ProgramAuthorshipDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.HubJsonapiEndpoint;

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

/**
 ProgramAuthorship endpoint
 */
@Path("api/1/program-authorships")
public class ProgramAuthorshipEndpoint extends HubJsonapiEndpoint {
  private final ProgramAuthorshipDAO dao;

  /**
   Constructor
   */
  @Inject
  public ProgramAuthorshipEndpoint(
    ProgramAuthorshipDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Create new programAuthorship binding

   @param jsonapiPayload with which to of ProgramAuthorship Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one ProgramAuthorship by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get Bindings in one programAuthorship.

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programId") String programId) {
    return readMany(crc, dao(), programId);
  }

  /**
   Update one ProgramAuthorship

   @param jsonapiPayload with which to update record.
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
   Delete one ProgramAuthorship by programAuthorshipId and bindingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramAuthorshipDAO dao() {
    return dao;
  }

}
