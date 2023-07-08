// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.analysis.HubAnalysisFactory;
import io.xj.hub.analysis.Report;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
@RestController
@RequestMapping("/api/1/analysis")
public class AnalysisController extends HubJsonapiEndpoint {
  static final Logger LOG = LoggerFactory.getLogger(AnalysisController.class);
  final HubAnalysisFactory analysisFactory;

  /**
   * Constructor
   */
  public AnalysisController(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    HubAnalysisFactory analysisFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.analysisFactory = analysisFactory;
  }

  /**
   * Perform any type of analysis
   *
   * @return text/html response.
   */
  @GetMapping("{templateId}/{type}")
  @RolesAllowed(ARTIST)
  public ResponseEntity<String> analysis(
    HttpServletRequest req, HttpServletResponse res,
    @PathVariable("templateId") String templateId,
    @PathVariable("type") String type
  ) {
    try {
      return ResponseEntity
        .ok()
        .contentType(MediaType.TEXT_HTML)
        .body(analysisFactory.report(
          HubAccess.fromRequest(req),
          UUID.fromString(templateId),
          Report.Type.valueOf(type)
        ).toHTML());

    } catch (Exception e) {
      LOG.error("Failed to analysis", e);
      return responseProvider.failureText(e);
    }
  }
}
