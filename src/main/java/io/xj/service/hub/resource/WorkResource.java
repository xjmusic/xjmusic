// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.resource;

import com.google.inject.Injector;
import io.xj.core.app.AppResource;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.Payload;

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
public class WorkResource extends AppResource {

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public WorkResource(
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
      return response.ok(
        new Payload().setDataEntities(
          workManager.readAllWork()));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
