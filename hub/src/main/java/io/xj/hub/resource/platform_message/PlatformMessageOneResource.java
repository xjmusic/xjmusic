// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.platform_message;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 PlatformMessage record
 */
@Path("platform-messages/{id}")
public class PlatformMessageOneResource extends HubResource {
  private final PlatformMessageDAO platformMessageDAO = injector.getInstance(PlatformMessageDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one platformMessage.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        PlatformMessage.KEY_ONE,
        platformMessageDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (CoreException ignored) {
      return response.notFound("Platform Message");

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
