// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.ingest.HubContentPayload;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * HubIngest
 * <p>
 * Architect wants to ingest library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library. https://www.pivotaltracker.com/story/show/154234716
 */
@RestController
@RequestMapping("/api/1/ingest")
public class IngestController extends HubJsonapiEndpoint {
  static final Logger LOG = LoggerFactory.getLogger(IngestController.class);
  final HubIngestFactory ingestFactory;

  /**
   * Constructor
   */
  public IngestController(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    HubIngestFactory ingestFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.ingestFactory = ingestFactory;
  }

  /**
   * Perform any type of ingest
   *
   * @return application/json response.
   */
  @GetMapping("{templateId}")
  @RolesAllowed(INTERNAL)
  public ResponseEntity<HubContentPayload> ingest(
    HttpServletRequest req,
    @PathVariable("templateId") String templateId
  ) {
    try {
      return ResponseEntity.ok(ingestFactory.ingest(HubAccess.fromRequest(req), UUID.fromString(templateId)).toContentPayload());

    } catch (Exception e) {
      LOG.error("Failed to ingest", e);
      return ResponseEntity.internalServerError().body(new HubContentPayload());
    }
  }
}
