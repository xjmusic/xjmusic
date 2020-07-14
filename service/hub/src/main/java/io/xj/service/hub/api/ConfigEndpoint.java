// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import io.xj.lib.jsonapi.ApiUrlProvider;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadObject;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.entity.*;

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
  public Response getConfig(@Context ContainerRequestContext crc) throws JsonApiException {
    return response.ok(
      new Payload().setDataOne(
        new PayloadObject().setAttributes(ImmutableMap.<String, Object>builder()
          .put("apiBaseUrl", apiUrlProvider.getAppBaseUrl() + apiUrlProvider.getApiPath())
          .put("audioBaseUrl", apiUrlProvider.getAudioBaseUrl())
          .put("baseUrl", apiUrlProvider.getAppBaseUrl())
          .put("choiceTypes", ProgramType.stringValues())
          .put("instrumentStates", InstrumentState.stringValues())
          .put("instrumentTypes", InstrumentType.stringValues())
          .put("patternDetailTypes", ProgramSequencePatternType.stringValuesForDetailSequence())
          .put("patternTypes", ProgramSequencePatternType.stringValues())
          .put("programStates", ProgramState.stringValues())
          .put("programTypes", ProgramType.stringValues())
          .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
          .put("voiceTypes", InstrumentType.stringValues())
          .build())));
  }
}
