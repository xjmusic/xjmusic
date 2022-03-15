// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.analysis.HubAnalysisFactory;
import io.xj.hub.analysis.Report;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.CSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

/**
 Template content Analysis #161199945
 */
@Path("api/1/analysis")
public class AnalysisEndpoint extends HubJsonapiEndpoint<Object> {
  private static final Logger LOG = LoggerFactory.getLogger(AnalysisEndpoint.class);
  private final HubAnalysisFactory analysisFactory;

  /**
   Constructor
   */
  @Inject
  public AnalysisEndpoint(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    HubAnalysisFactory analysisFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.analysisFactory = analysisFactory;
  }

  /**
   Perform any type of analysis

   @return text/html response.
   */
  @GET
  @Path("{templateId}")
  @RolesAllowed(ARTIST)
  public Response analysis(
    @Context ContainerRequestContext crc,
    @PathParam("templateId") String templateId,
    @QueryParam("analyze") String analysisTypes
  ) {
    try {
      return Response
        .ok(analysisFactory.analysis(
          HubAccess.fromContext(crc),
          UUID.fromString(templateId),
          Report.Type.fromValues(CSV.split(analysisTypes))
        ).toHTML())
        .type(MediaType.TEXT_HTML_TYPE)
        .build();

    } catch (Exception e) {
      LOG.error("Failed to analysis", e);
      return response.failure(e);
    }
  }
}
