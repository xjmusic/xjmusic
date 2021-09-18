// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.dao.InstrumentAuthorshipDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.MediaType;

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
 InstrumentAuthorship endpoint
 */
@Path("api/1/instrument-authorships")
public class InstrumentAuthorshipEndpoint extends HubJsonapiEndpoint {
  private final InstrumentAuthorshipDAO dao;

  /**
   Constructor
   */
  @Inject
  public InstrumentAuthorshipEndpoint(
    InstrumentAuthorshipDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Create new instrumentAuthorship binding

   @param jsonapiPayload with which to of InstrumentAuthorship Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one InstrumentAuthorship by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get Bindings in one instrumentAuthorship.

   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("instrumentId") String instrumentId) {
    return readMany(crc, dao(), instrumentId);
  }

  /**
   Update one instrumentAuthorship

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
   Delete one InstrumentAuthorship by instrumentAuthorshipId and bindingId

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
  private InstrumentAuthorshipDAO dao() {
    return dao;
  }

}
