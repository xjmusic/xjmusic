// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Injector;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.hub.ingest.HubIngest;
import io.xj.service.hub.ingest.HubIngestCacheProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;

/**
 HubIngest
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@Path("ingest")
public class IngestEndpoint extends HubEndpoint {
  private final Logger log = LoggerFactory.getLogger(IngestEndpoint.class);
  private HubIngestCacheProvider ingestProvider;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public IngestEndpoint(
    Injector injector
  ) {
    super(injector);
    ingestProvider = injector.getInstance(HubIngestCacheProvider.class);
  }

  /**
   Perform any type of ingest

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.INTERNAL})
  public Response ingest(
    @Context ContainerRequestContext crc,
    @QueryParam("libraryIds") Set<UUID> libraryIds,
    @QueryParam("programIds") Set<UUID> programIds,
    @QueryParam("instrumentIds") Set<UUID> instrumentIds
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      Payload payload = payloadFactory.newPayload();
      HubIngest ingest = ingestProvider.ingest(hubAccess, libraryIds, programIds, instrumentIds);
      payload.setDataType(PayloadDataType.Many);
      ingest.getAllEntities().forEach(entity -> {
        try {
          payload.addData(payloadFactory.toPayloadObject(entity));
        } catch (JsonApiException e) {
          log.error("Failed to ingest entity: {}", entity, e);
          payload.addError(payloadFactory.newPayloadError()
            .setCode("IngestFailure").setTitle("Failed to ingest entity").setDetail(e.getMessage()));
        }
      });

      return response.ok(payload);
    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
