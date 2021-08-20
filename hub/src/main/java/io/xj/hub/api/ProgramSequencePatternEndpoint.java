// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.api.ProgramSequencePattern;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAOCloner;
import io.xj.hub.dao.ProgramSequencePatternDAO;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.hub.HubEndpoint;

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
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 ProgramSequencePattern endpoint
 */
@Path("api/1/program-sequence-patterns")
public class ProgramSequencePatternEndpoint extends HubEndpoint {
  private final ProgramSequencePatternDAO dao;

  /**
   Constructor
   */
  @Inject
  public ProgramSequencePatternEndpoint(
    ProgramSequencePatternDAO dao,
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Create new programSequencePattern binding

   @param jsonapiPayload with which to of ProgramSequencePattern Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({ARTIST})
  public Response create(
    JsonapiPayload jsonapiPayload,
    @Context ContainerRequestContext crc,
    @QueryParam("cloneId") String cloneId
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      var programSequencePattern = payloadFactory.consume(dao().newInstance(), jsonapiPayload);
      JsonapiPayload responseJsonapiPayload = new JsonapiPayload();
      if (Objects.nonNull(cloneId)) {
        DAOCloner<ProgramSequencePattern> cloner = dao().clone(hubAccess, UUID.fromString(cloneId), programSequencePattern);
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(cloner.getClone()));
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object entity : cloner.getChildClones()) {
          JsonapiPayloadObject jsonapiPayloadObject = payloadFactory.toPayloadObject(entity);
          list.add(jsonapiPayloadObject);
        }
        responseJsonapiPayload.setIncluded(list);
      } else {
        responseJsonapiPayload.setDataOne(payloadFactory.toPayloadObject(dao().create(hubAccess, programSequencePattern)));
      }

      return response.create(responseJsonapiPayload);

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Get one ProgramSequencePattern by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({ARTIST})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get Patterns in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed({ARTIST})
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("programSequenceId") String programSequenceId) {
    return readMany(crc, dao(), programSequenceId);
  }

  /**
   Update one ProgramSequencePattern

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
   Delete one ProgramSequencePattern by programSequenceId and patternId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({ARTIST})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramSequencePatternDAO dao() {
    return dao;
  }

}
