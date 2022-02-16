// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.ConfigFactory;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.*;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
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
public class ConfigEndpoint extends HubJsonapiEndpoint<Object> {
  private final Map<String, Object> configMap;

  /**
   Constructor
   */
  @Inject
  public ConfigEndpoint(
    Environment env,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    configMap = ImmutableMap.<String, Object>builder()
      .put("apiBaseUrl", env.getAppBaseUrl())
      .put("audioBaseUrl", env.getAudioBaseUrl())
      .put("streamBaseUrl", env.getStreamBaseUrl())
      .put("baseUrl", env.getAppBaseUrl())
      .put("choiceTypes", ProgramType.values())
      .put("defaultInstrumentConfig", Text.format(ConfigFactory.parseString(InstrumentConfig.DEFAULT)))
      .put("defaultProgramConfig", Text.format(ConfigFactory.parseString(ProgramConfig.DEFAULT)))
      .put("defaultTemplateConfig", Text.format(ConfigFactory.parseString(TemplateConfig.DEFAULT)))
      .put("instrumentStates", InstrumentState.values())
      .put("instrumentTypes", InstrumentType.values())
      .put("instrumentModes", InstrumentMode.values())
      .put("playerBaseUrl", env.getPlayerBaseUrl())
      .put("programStates", ProgramState.values())
      .put("programTypes", ProgramType.values())
      .put("shipBaseUrl", env.getShipBaseUrl())
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
