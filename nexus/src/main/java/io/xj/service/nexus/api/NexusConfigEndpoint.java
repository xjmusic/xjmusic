// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.Instrument;
import io.xj.Program;
import io.xj.ProgramSequencePattern;
import io.xj.Segment;
import io.xj.lib.jsonapi.ApiUrlProvider;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.service.nexus.NexusEndpoint;

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
@Path("nexus/config")
public class NexusConfigEndpoint extends NexusEndpoint {
  private final Map<String, Object> configMap;

  /**
   Constructor
   */
  @Inject
  public NexusConfigEndpoint(
    ApiUrlProvider apiUrlProvider,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) throws JsonApiException {
    super(response, config, payloadFactory);
    String defaultChainConfig = Text.format(config.getConfig("chain"));

    configMap = ImmutableMap.<String, Object>builder()
      .put("apiBaseUrl", apiUrlProvider.getAppBaseUrl() + apiUrlProvider.getApiPath())
      .put("audioBaseUrl", apiUrlProvider.getAudioBaseUrl())
      .put("baseUrl", apiUrlProvider.getAppBaseUrl())
      .put("chainStates", Value.without(Chain.State.UNRECOGNIZED, Chain.State.values()))
      .put("chainTypes", Value.without(Chain.Type.UNRECOGNIZED, Chain.Type.values()))
      .put("choiceTypes", Value.without(Program.Type.UNRECOGNIZED, Program.Type.values()))
      .put("defaultChainConfig", defaultChainConfig)
      .put("instrumentStates", Value.without(Instrument.State.UNRECOGNIZED, Instrument.State.values()))
      .put("instrumentTypes", Value.without(Instrument.Type.UNRECOGNIZED, Instrument.Type.values()))
      .put("patternDetailTypes", Value.without(ProgramSequencePattern.Type.UNRECOGNIZED, ProgramSequencePattern.Type.values()))
      .put("patternTypes", Value.without(ProgramSequencePattern.Type.UNRECOGNIZED, ProgramSequencePattern.Type.values()))
      .put("playerBaseUrl", apiUrlProvider.getPlayerBaseUrl())
      .put("programStates", Value.without(Program.State.UNRECOGNIZED, Program.State.values()))
      .put("programTypes", Value.without(Program.Type.UNRECOGNIZED, Program.Type.values()))
      .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
      .put("segmentStates", Value.without(Segment.State.UNRECOGNIZED, Segment.State.values()))
      .put("segmentTypes", Value.without(Segment.Type.UNRECOGNIZED, Segment.Type.values()))
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
      new JsonapiPayload().setDataOne(
        new JsonapiPayloadObject().setAttributes(configMap)));
  }
}
