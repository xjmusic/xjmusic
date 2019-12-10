// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import io.xj.core.app.AppResource;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ChainConfigType;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.ProgramPatternType;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.SegmentState;
import io.xj.core.payload.Payload;
import io.xj.core.payload.PayloadObject;
import io.xj.core.transport.ApiUrlProvider;

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
public class ConfigEndpoint extends AppResource {
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
  public Response getConfig(@Context ContainerRequestContext crc) throws CoreException {


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
          .put("patternDetailTypes", ProgramPatternType.stringValuesForDetailSequence())
          .put("patternTypes", ProgramPatternType.stringValues())
          .put("programStates", ProgramState.stringValues())
          .put("programTypes", ProgramType.stringValues())
          .put("segmentBaseUrl", apiUrlProvider.getSegmentBaseUrl())
          .put("segmentStates", SegmentState.stringValues())
          .put("voiceTypes", InstrumentType.stringValues())
          .build())));
  }
}
