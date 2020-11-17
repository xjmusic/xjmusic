// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.jsonapi.ApiUrlProvider;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.PayloadObject;
import io.xj.lib.util.Text;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.dao.ProgramSequencePatternDAO;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Current platform configuration
 */
@Path("hub/config")
public class HubConfigEndpoint extends HubEndpoint {
  private final ApiUrlProvider apiUrlProvider;
  private final String defaultProgramConfig;
  private final String defaultInstrumentConfig;

  /**
   Constructor
   */
  @Inject
  public HubConfigEndpoint(
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory,
    ApiUrlProvider apiUrlProvider
  ) {
    super(response, config, payloadFactory);
    this.apiUrlProvider = apiUrlProvider;
    defaultProgramConfig = Text.format(config.getConfig("program"));
    defaultInstrumentConfig = Text.format(config.getConfig("instrument"));
  }

  /**
   Get current platform configuration (PUBLIC)

   @return application/json response.
   */
  @GET
  @PermitAll
  public Response getConfig(@Context ContainerRequestContext crc) throws JsonApiException {
    return response.ok(
      new Payload().setDataOne(
        new PayloadObject().setAttributes(ImmutableMap.<String, Object>builder()
          .put("apiBaseUrl", apiUrlProvider.getAppBaseUrl() + apiUrlProvider.getApiPath())
          .put("audioBaseUrl", apiUrlProvider.getAudioBaseUrl())
          .put("baseUrl", apiUrlProvider.getAppBaseUrl())
          .put("choiceTypes", ProgramDAO.programTypeStringValues())
          .put("defaultInstrumentConfig", defaultInstrumentConfig)
          .put("defaultProgramConfig", defaultProgramConfig)
          .put("instrumentStates", InstrumentDAO.instrumentStateStringValues())
          .put("instrumentTypes", InstrumentDAO.instrumentTypeStringValues())
          .put("patternDetailTypes", ProgramSequencePatternDAO.patternTypesForDetailSequenceStringValues())
          .put("patternTypes", ProgramSequencePatternDAO.programSequencePatternTypeStringValues())
          .put("playerBaseUrl", apiUrlProvider.getPlayerBaseUrl())
          .put("programStates", ProgramDAO.programStateStringValues())
          .put("programTypes", ProgramDAO.programTypeStringValues())
          .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
          .put("voiceTypes", InstrumentDAO.instrumentTypeStringValues())
          .build())));
  }
}
