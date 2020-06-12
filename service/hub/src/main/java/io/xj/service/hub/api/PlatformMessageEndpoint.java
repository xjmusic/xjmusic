// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.rest_api.MediaType;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.PayloadDataType;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.PlatformMessageDAO;
import io.xj.service.hub.model.PlatformMessage;
import io.xj.service.hub.model.UserRoleType;

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

/**
 PlatformMessages
 */
@Path("platform-messages")
public class PlatformMessageEndpoint extends HubEndpoint {
  private PlatformMessageDAO dao;
  private Config config;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public PlatformMessageEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(PlatformMessageDAO.class);
    config = injector.getInstance(Config.class);
  }

  /**
   Get all platformMessages.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("previousDays") Integer previousDays) {

    if (null == previousDays || 0 == previousDays)
      previousDays = config.getInt("platform.messageReadPreviousDays");

    try {
      Payload payload = new Payload().setDataType(PayloadDataType.HasMany);
      for (PlatformMessage message : dao().readAllPreviousDays(
        Access.fromContext(crc),
        previousDays))
        payload.addData(payloadFactory.toPayloadObject(message));
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new platform message

   @param payload with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), payload);
  }

  /**
   Get one library.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one library

   @param payload with which to update Library record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one library

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }


  /**
   Get DAO of injector

   @return DAO
   */
  private PlatformMessageDAO dao() {
    return dao;
  }
}
