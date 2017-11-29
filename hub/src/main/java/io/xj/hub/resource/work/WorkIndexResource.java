// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.work;

import io.xj.core.CoreModule;
import io.xj.core.app.server.HttpResponseProvider;
import io.xj.core.model.role.Role;
import io.xj.core.model.work.Work;
import io.xj.core.work.WorkManager;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
public class WorkIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final WorkManager workManager = injector.getInstance(WorkManager.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  /**
   Get all works.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ADMIN})
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
