// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.inject.Injector;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.nexus.NexusEndpoint;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.work.NexusWork;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 Works
 */
@Path("works")
public class WorkEndpoint extends NexusEndpoint {
  private final NexusWork nexusWork;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public WorkEndpoint(
    Injector injector
  ) {
    super(injector);
    nexusWork = injector.getInstance(NexusWork.class);
  }

  /**
   Get all works.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response readMany(@Context ContainerRequestContext crc) {
    try {
      Payload payload = new Payload().setDataType(PayloadDataType.Many);
      for (UUID id : nexusWork.getChainWorkingIds())
        payload.addData(payloadFactory.newPayloadObject()
          .setType(Chain.class)
          .setId(id.toString()));
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
