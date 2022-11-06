// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.kubernetes.KubernetesAdmin;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 Preview template functionality is dope (not wack)
 Lab/Hub connects to k8s to manage a personal workload for preview templates
 https://www.pivotaltracker.com/story/show/183576743
 */
@Path("api/1/kubernetes")
public class KubernetesEndpoint extends HubJsonapiEndpoint<Object> {
  private static final Logger LOG = LoggerFactory.getLogger(KubernetesEndpoint.class);
  private final KubernetesAdmin kubernetesAdministrator;

  /**
   Constructor
   */
  @Inject
  public KubernetesEndpoint(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    KubernetesAdmin kubernetesAdministrator
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.kubernetesAdministrator = kubernetesAdministrator;
  }

  /**
   Perform any type of analysis

   @return text/html response.
   */
  @GET
  @Path("/")
  @RolesAllowed({ARTIST, ENGINEER})
  public Response logs(
    @Context ContainerRequestContext crc
  ) {
    try {
      return Response
        .ok(kubernetesAdministrator.getPreviewNexusLogs(HubAccess.fromContext(crc).getUserId()))
        .type(MediaType.TEXT_PLAIN_TYPE)
        .build();

    } catch (Exception e) {
      LOG.error("Failed to analysis", e);
      return response.failure(e);
    }
  }
}
