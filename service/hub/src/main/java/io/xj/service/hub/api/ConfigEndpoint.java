// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import io.xj.lib.rest_api.ApiUrlProvider;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.PayloadObject;
import io.xj.lib.rest_api.RestApiException;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.model.ChainConfigType;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.model.InstrumentState;
import io.xj.service.hub.model.InstrumentType;
import io.xj.service.hub.model.ProgramSequencePatternType;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.SegmentState;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Current platform configuration
 */
@Path("config")
public class ConfigEndpoint extends HubEndpoint {
  private ApiUrlProvider apiUrlProvider;


  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ConfigEndpoint(
    Injector injector
  ) {
    super(injector);
    apiUrlProvider = injector.getInstance(ApiUrlProvider.class);
  }

  /**
   Get current platform configuration (PUBLIC)

   @return application/json response.
   */
  @GET
  @PermitAll
  public Response getConfig(@Context ContainerRequestContext crc) throws RestApiException {
    return response.ok(
      new Payload().setDataOne(
        new PayloadObject().setAttributes(ImmutableMap.<String, Object>builder()
          .put("apiBaseUrl", apiUrlProvider.getAppBaseUrl() + apiUrlProvider.getApiPath())
          .put("audioBaseUrl", apiUrlProvider.getAudioBaseUrl())
          .put("baseUrl", apiUrlProvider.getAppBaseUrl())
          .put("chainConfigTypes", ChainConfigType.stringValues())
          .put("chainStates", ChainState.stringValues())
          .put("chainTypes", ChainType.stringValues())
          .put("choiceTypes", ProgramType.stringValues())
          .put("instrumentStates", InstrumentState.stringValues())
          .put("instrumentTypes", InstrumentType.stringValues())
          .put("patternDetailTypes", ProgramSequencePatternType.stringValuesForDetailSequence())
          .put("patternTypes", ProgramSequencePatternType.stringValues())
          .put("programStates", ProgramState.stringValues())
          .put("programTypes", ProgramType.stringValues())
          .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
          .put("segmentStates", SegmentState.stringValues())
          .put("voiceTypes", InstrumentType.stringValues())
          .build())));
  }
}
