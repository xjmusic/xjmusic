// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.*;
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
import java.util.List;
import java.util.Map;

/**
 Current platform configuration
 */
@Path("config")
public class ConfigEndpoint extends HubJsonapiEndpoint {
  private static final List<String> CHAIN_STATES = ImmutableList.of(
    "Draft",
    "Ready",
    "Fabricate",
    "Complete",
    "Failed"
  );
  private static final List<String> CHAIN_TYPES = ImmutableList.of(
    "Preview",
    "Production"
  );
  private static final List<String> SEGMENT_STATES = ImmutableList.of(
    "Planned",
    "Crafting",
    "Crafted",
    "Dubbing",
    "Dubbed",
    "Failed"
  );
  private static final List<String> SEGMENT_TYPES = ImmutableList.of(
    "Pending",
    "Initial",
    "Continue",
    "NextMain",
    "NextMacro"
  );
  private final Map<String, Object> configMap;

  /**
   Constructor
   */
  @Inject
  public ConfigEndpoint(
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    ApiUrlProvider apiUrlProvider
  ) {
    super(response, payloadFactory);
    configMap = ImmutableMap.<String, Object>builder()
      .put("apiBaseUrl", apiUrlProvider.getAppBaseUrl())
      .put("audioBaseUrl", apiUrlProvider.getAudioBaseUrl())
      .put("baseUrl", apiUrlProvider.getAppBaseUrl())
      .put("chainStates", CHAIN_STATES)
      .put("chainTypes", CHAIN_TYPES)
      .put("choiceTypes", ProgramType.values())
      .put("defaultInstrumentConfig", Text.format(InstrumentConfig.DEFAULT))
      .put("defaultProgramConfig", Text.format(ProgramConfig.DEFAULT))
      .put("defaultTemplateConfig", Text.format(TemplateConfig.DEFAULT))
      .put("instrumentStates", InstrumentState.values())
      .put("instrumentTypes", InstrumentType.values())
      .put("patternDetailTypes", ProgramSequencePatternType.values())
      .put("patternTypes", ProgramSequencePatternType.values())
      .put("playerBaseUrl", apiUrlProvider.getPlayerBaseUrl())
      .put("programStates", ProgramState.values())
      .put("programTypes", ProgramType.values())
      .put("segmentStates", SEGMENT_STATES)
      .put("segmentTypes", SEGMENT_TYPES)
      .put("shipBaseURL", apiUrlProvider.getShipBaseUrl())
      .put("templateTypes", TemplateType.values())
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
