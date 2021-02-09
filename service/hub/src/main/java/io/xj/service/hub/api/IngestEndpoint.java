// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.ingest.HubIngest;
import io.xj.service.hub.ingest.HubIngestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 HubIngest
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@Path("ingest")
public class IngestEndpoint extends HubEndpoint {
  private final Logger log = LoggerFactory.getLogger(IngestEndpoint.class);
  private final HubIngestFactory ingestFactory;

  /**
   Constructor
   */
  @Inject
  public IngestEndpoint(
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory,
    HubIngestFactory ingestFactory
  ) {
    super(response, config, payloadFactory);
    this.ingestFactory = ingestFactory;
  }

  /**
   Perform any type of ingest

   @return application/json response.
   */
  @GET
  @RolesAllowed({INTERNAL})
  public Response ingest(
    @Context ContainerRequestContext crc,
    @QueryParam("libraryIds") Set<String> libraryIds,
    @QueryParam("programIds") Set<String> programIds,
    @QueryParam("instrumentIds") Set<String> instrumentIds
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = payloadFactory.newJsonapiPayload();
      HubIngest ingest = ingestFactory.ingest(hubAccess, libraryIds, programIds, instrumentIds);
      jsonapiPayload.setDataType(PayloadDataType.Many);
      ingest.getAllEntities().forEach(entity -> {
        try {
          jsonapiPayload.addData(payloadFactory.toPayloadObject(entity));
        } catch (JsonApiException e) {
          log.error("Failed to ingest entity: {}", entity, e);
          jsonapiPayload.addError(payloadFactory.newPayloadError()
            .setCode("IngestFailure").setTitle("Failed to ingest entity").setDetail(e.getMessage()));
        }
      });

      return response.ok(jsonapiPayload);
    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
