// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.platform_message;

import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 PlatformMessage record
 */
@Path("platform-messages/{id}")
public class PlatformMessageOneResource extends HubResource {

  @PathParam("id")
  String id;

  /**
   Get one platformMessage.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readOne(@Context ContainerRequestContext crc) {
    return readOne(crc, dao(), id);
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private PlatformMessageDAO dao() {
    return injector.getInstance(PlatformMessageDAO.class);
  }
}
