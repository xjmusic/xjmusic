// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.platform_message;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.platform_message.PlatformMessageWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 PlatformMessages
 */
@Path("platform-messages")
public class PlatformMessageIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PlatformMessageDAO platformMessageDAO = injector.getInstance(PlatformMessageDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("previousDays")
  Integer previousDays;

  /**
   Get all platformMessages.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (null == previousDays || 0 == previousDays) {
      previousDays = Config.platformMessageReadPreviousDays();
    }

    try {
      return response.readMany(
        PlatformMessage.KEY_MANY,
        platformMessageDAO.readAllPreviousDays(
          Access.fromContext(crc),
          previousDays));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new platform message

   @param data with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response create(PlatformMessageWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PlatformMessage.KEY_MANY,
        PlatformMessage.KEY_ONE,
        platformMessageDAO.create(
          Access.fromContext(crc),
          data.getPlatformMessage()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
