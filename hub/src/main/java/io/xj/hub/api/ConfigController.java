// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.analysis.Report;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.util.Text;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Current platform configuration
 */
@RestController
@RequestMapping("/config")
public class ConfigController extends HubJsonapiEndpoint {
    private final Map<String, Object> configMap;

  /**
   * Constructor
   */
  public ConfigController(
    AppEnvironment env,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    var c = ImmutableMap.<String, Object>builder();
    c.put("analysisTypes", Arrays.stream(Report.Type.values()).collect(Collectors.toMap(Report.Type::toString, Report.Type::getName)));
    c.put("apiBaseUrl", env.getAppBaseUrl());
    c.put("audioBaseUrl", env.getAudioBaseUrl());
    c.put("streamBaseUrl", env.getStreamBaseUrl());
    c.put("baseUrl", env.getAppBaseUrl());
    c.put("choiceTypes", ProgramType.values());
    c.put("defaultInstrumentConfig", Text.format(ConfigFactory.parseString(InstrumentConfig.DEFAULT)));
    c.put("defaultProgramConfig", Text.format(ConfigFactory.parseString(ProgramConfig.DEFAULT)));
    c.put("defaultTemplateConfig", Text.format(ConfigFactory.parseString(TemplateConfig.DEFAULT)));
    c.put("instrumentStates", InstrumentState.values());
    c.put("instrumentTypes", InstrumentType.values());
    c.put("instrumentModes", InstrumentMode.values());
    c.put("playerBaseUrl", env.getPlayerBaseUrl());
    c.put("programStates", ProgramState.values());
    c.put("programTypes", ProgramType.values());
    c.put("shipBaseUrl", env.getShipBaseUrl());
    c.put("templateTypes", TemplateType.values());
    configMap = c.build();
  }

  /**
   * Get current platform configuration (PUBLIC)
   *
   * @return application/json response.
   */
  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  @PermitAll
  public ResponseEntity<JsonapiPayload> getConfig() {
    var dataOne = new JsonapiPayloadObject().setAttributes(configMap);
    var payload = new JsonapiPayload().setDataOne(dataOne);

    return responseProvider.ok(payload);
  }
}
