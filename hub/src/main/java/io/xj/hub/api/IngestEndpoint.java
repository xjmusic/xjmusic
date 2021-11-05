// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 HubIngest
 <p>
 [#154234716] Architect wants to ingest library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@Path("api/1/ingest")
public class IngestEndpoint extends HubJsonapiEndpoint<Object> {
  private static final Logger LOG = LoggerFactory.getLogger(IngestEndpoint.class);
  private final HubIngestFactory ingestFactory;

  /**
   Constructor
   */
  @Inject
  public IngestEndpoint(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    HubIngestFactory ingestFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.ingestFactory = ingestFactory;
  }

  /**
   Perform any type of ingest

   @return application/json response.
   */
  @GET
  @Path("{templateId}")
  @RolesAllowed(INTERNAL)
  public Response ingest(
    @Context ContainerRequestContext crc,
    @PathParam("templateId") String templateId
  ) {
    try {
      return response.ok(ingestFactory.ingest(HubAccess.fromContext(crc), UUID.fromString(templateId)).toJSON());

    } catch (Exception e) {
      LOG.error("Failed to ingest", e);
      return response.failure(e);
    }
  }
}
