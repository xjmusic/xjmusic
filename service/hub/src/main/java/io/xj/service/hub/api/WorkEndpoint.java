// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Injector;
import io.xj.lib.rest_api.Payload;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.model.UserRoleType;
import io.xj.service.hub.model.Work;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Works
 */
@Path("works")
public class WorkEndpoint extends HubEndpoint {

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public WorkEndpoint(
    Injector injector
  ) {
    super(injector);
  }

  /**
   Get all works.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc) {
    try {
      Payload payload = new Payload();
      for (Work work : workManager.readAllWork()) payload.addData(payloadFactory.toPayloadObject(work));
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
