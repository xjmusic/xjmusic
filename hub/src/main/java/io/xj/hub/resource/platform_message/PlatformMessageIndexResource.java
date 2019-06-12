// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.platform_message;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.model.payload.MediaType;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 PlatformMessages
 */
@Path("platform-messages")
public class PlatformMessageIndexResource extends HubResource {

  @QueryParam("previousDays")
  Integer previousDays;

  /**
   Get all platformMessages.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc) {

    if (null == previousDays || 0 == previousDays)
      previousDays = Config.getPlatformMessageReadPreviousDays();

    try {
      return response.ok(
        new Payload().setDataEntities(
          dao().readAllPreviousDays(
            Access.fromContext(crc),
            previousDays), false));

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
   Get DAO from injector

   @return DAO
   */
  private PlatformMessageDAO dao() {
    return injector.getInstance(PlatformMessageDAO.class);
  }
}
