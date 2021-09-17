// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.api.ChainState;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramSequencePatternType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.hub.HubEndpoint;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.util.Text;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 Current platform configuration
 */
@Path("config")
public class ConfigEndpoint extends HubEndpoint {
  private final Map<String, Object> configMap;

  /**
   Constructor
   */
  @Inject
  public ConfigEndpoint(
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory payloadFactory,
    ApiUrlProvider apiUrlProvider
  ) {
    super(response, config, payloadFactory);
    var defaultInstrumentConfig = Text.format(config.getConfig("instrument"));
    var defaultProgramConfig = Text.format(config.getConfig("program"));

    configMap = ImmutableMap.<String, Object>builder()
      .put("apiBaseUrl", apiUrlProvider.getAppBaseUrl())
      .put("audioBaseUrl", apiUrlProvider.getAudioBaseUrl())
      .put("baseUrl", apiUrlProvider.getAppBaseUrl())
      .put("chainStates", ChainState.values())
      .put("templateTypes", TemplateType.values())
      .put("choiceTypes", ProgramType.values())
      .put("defaultInstrumentConfig", defaultInstrumentConfig)
      .put("defaultProgramConfig", defaultProgramConfig)
      .put("instrumentStates", InstrumentState.values())
      .put("instrumentTypes", InstrumentType.values())
      .put("patternDetailTypes", ProgramSequencePatternType.values())
      .put("patternTypes", ProgramSequencePatternType.values())
      .put("playerBaseUrl", apiUrlProvider.getPlayerBaseUrl())
      .put("programStates", ProgramState.values())
      .put("programTypes", ProgramType.values())
      .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
      .put("segmentStates", SegmentState.values())
      .put("segmentTypes", SegmentType.values())
      .put("voiceTypes", InstrumentType.values())
      .build();
  }

  /**
   Get current platform configuration (PUBLIC)

   @return application/json response.
   */
  @GET
  @PermitAll
  public Response getConfig(@Context ContainerRequestContext crc) {
    return response.ok(
      new JsonapiPayload().setDataOne(
        new JsonapiPayloadObject().setAttributes(configMap)));
  }
}
