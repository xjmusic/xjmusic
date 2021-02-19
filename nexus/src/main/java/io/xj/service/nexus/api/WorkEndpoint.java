// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.nexus.NexusEndpoint;
import io.xj.service.nexus.work.NexusWork;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Works
 */
@Path("api/1/works")
public class WorkEndpoint extends NexusEndpoint {
  private final NexusWork nexusWork;

  /**
   Constructor
   */
  @Inject
  public WorkEndpoint(
    NexusWork nexusWork,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.nexusWork = nexusWork;
  }

  /**
   Get all works.

   @return application/json response.
   */
  @GET
  @RolesAllowed({ADMIN, ENGINEER})
  public Response readMany(@Context ContainerRequestContext crc) {
    try {
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      for (String id : nexusWork.getChainWorkingIds())
        jsonapiPayload.addData(payloadFactory.newPayloadObject()
          .setType(Chain.class)
          .setId(id.toString()));
      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
