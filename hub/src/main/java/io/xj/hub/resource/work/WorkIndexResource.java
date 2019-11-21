// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.work;

import io.xj.core.payload.Payload;
import io.xj.core.model.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Works
 */
@Path("works")
public class WorkIndexResource extends HubResource {

  /**
   Get all works.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc) {
    try {
      return response.ok(
        new Payload().setDataEntities(
          workManager.readAllWork()));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
