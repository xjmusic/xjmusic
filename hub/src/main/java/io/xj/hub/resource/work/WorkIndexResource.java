// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.work;

import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.work.Work;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.work.WorkManager;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Works
 */
@Path("works")
public class WorkIndexResource extends HubResource {
  private final WorkManager workManager = injector.getInstance(WorkManager.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  /**
   Get all works.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readMany(
        Work.KEY_MANY,
        workManager.readAllWork());

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
