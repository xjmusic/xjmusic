// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

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
import io.xj.service.hub.entity.InstrumentState;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.entity.ProgramSequencePatternType;
import io.xj.service.hub.entity.ProgramState;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.NexusEndpoint;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.SegmentState;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Current platform configuration
 */
@Path("nexus/config")
public class NexusConfigEndpoint extends NexusEndpoint {
  private final ApiUrlProvider apiUrlProvider;
  private final String defaultChainConfig;

  /**
   Constructor
   */
  @Inject
  public NexusConfigEndpoint(
    ApiUrlProvider apiUrlProvider,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    defaultChainConfig = Text.format(config.getConfig("chain"));
    this.apiUrlProvider = apiUrlProvider;
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
          .put("chainStates", ChainState.stringValues())
          .put("chainTypes", ChainType.stringValues())
          .put("choiceTypes", ProgramType.stringValues())
          .put("defaultChainConfig", defaultChainConfig)
          .put("instrumentStates", InstrumentState.stringValues())
          .put("instrumentTypes", InstrumentType.stringValues())
          .put("patternDetailTypes", ProgramSequencePatternType.stringValuesForDetailSequence())
          .put("patternTypes", ProgramSequencePatternType.stringValues())
          .put("playerBaseUrl", apiUrlProvider.getPlayerBaseUrl())
          .put("programStates", ProgramState.stringValues())
          .put("programTypes", ProgramType.stringValues())
          .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
          .put("segmentStates", SegmentState.stringValues())
          .put("voiceTypes", InstrumentType.stringValues())
          .build())));
  }
}
