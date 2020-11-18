// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.Instrument;
import io.xj.Program;
import io.xj.ProgramSequencePattern;
import io.xj.lib.jsonapi.ApiUrlProvider;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.PayloadObject;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.service.hub.HubEndpoint;

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
@Path("hub/config")
public class HubConfigEndpoint extends HubEndpoint {
  private static final String UNRECOGNIZED_VALUE = "UNRECOGNIZED";
  private final ApiUrlProvider apiUrlProvider;
  private final String defaultProgramConfig;
  private final String defaultInstrumentConfig;
  private final Map<String, Object> configMap;

  /**
   Constructor
   */
  @Inject
  public HubConfigEndpoint(
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory,
    ApiUrlProvider apiUrlProvider
  ) throws JsonApiException {
    super(response, config, payloadFactory);
    this.apiUrlProvider = apiUrlProvider;
    defaultProgramConfig = Text.format(config.getConfig("program"));
    defaultInstrumentConfig = Text.format(config.getConfig("instrument"));

    configMap = ImmutableMap.<String, Object>builder()
      .put("apiBaseUrl", apiUrlProvider.getAppBaseUrl() + apiUrlProvider.getApiPath())
      .put("audioBaseUrl", apiUrlProvider.getAudioBaseUrl())
      .put("baseUrl", apiUrlProvider.getAppBaseUrl())
      .put("choiceTypes", Value.without(Program.Type.UNRECOGNIZED, Program.Type.values()))
      .put("defaultInstrumentConfig", defaultInstrumentConfig)
      .put("defaultProgramConfig", defaultProgramConfig)
      .put("instrumentStates", Value.without(Instrument.State.UNRECOGNIZED, Instrument.State.values()))
      .put("instrumentTypes", Value.without(Instrument.Type.UNRECOGNIZED, Instrument.Type.values()))
      .put("patternDetailTypes", Value.without(ProgramSequencePattern.Type.UNRECOGNIZED, ProgramSequencePattern.Type.values()))
      .put("patternTypes", Value.without(ProgramSequencePattern.Type.UNRECOGNIZED, ProgramSequencePattern.Type.values()))
      .put("playerBaseUrl", apiUrlProvider.getPlayerBaseUrl())
      .put("programStates", Value.without(Program.State.UNRECOGNIZED, Program.State.values()))
      .put("programTypes", Value.without(Program.Type.UNRECOGNIZED, Program.Type.values()))
      .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
      .put("voiceTypes", Value.without(Instrument.Type.UNRECOGNIZED, Instrument.Type.values()))
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
      new Payload().setDataOne(
        new PayloadObject().setAttributes(configMap)));
  }
}
