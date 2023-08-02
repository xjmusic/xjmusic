// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

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
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadObject;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Current platform configuration
 */
@RestController
@RequestMapping("/config")
public class ConfigController extends HubJsonapiEndpoint {
  final Map<String, Object> configMap;

  /**
   * Constructor
   */
  @Autowired
  public ConfigController(
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory,
    @Value("${app.base.url}")
    String appBaseUrl,
    @Value("${audio.base.url}")
    String audioBaseUrl,
    @Value("${stream.base.url}")
    String streamBaseUrl,
    @Value("${player.base.url}")
    String playerBaseUrl,
    @Value("${ship.base.url}")
    String shipBaseUrl
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    configMap = new HashMap<>();
    configMap.put("analysisTypes", Arrays.stream(Report.Type.values()).collect(Collectors.toMap(Report.Type::toString, Report.Type::getName)));
    configMap.put("apiBaseUrl", appBaseUrl);
    configMap.put("audioBaseUrl", audioBaseUrl);
    configMap.put("streamBaseUrl", streamBaseUrl);
    configMap.put("baseUrl", appBaseUrl);
    configMap.put("choiceTypes", ProgramType.values());
    configMap.put("defaultInstrumentConfig", StringUtils.format(ConfigFactory.parseString(InstrumentConfig.DEFAULT)));
    configMap.put("defaultProgramConfig", StringUtils.format(ConfigFactory.parseString(ProgramConfig.DEFAULT)));
    configMap.put("defaultTemplateConfig", StringUtils.format(ConfigFactory.parseString(TemplateConfig.DEFAULT)));
    configMap.put("instrumentStates", InstrumentState.values());
    configMap.put("instrumentTypes", InstrumentType.values());
    configMap.put("instrumentModes", InstrumentMode.values());
    configMap.put("playerBaseUrl", playerBaseUrl);
    configMap.put("programStates", ProgramState.values());
    configMap.put("programTypes", ProgramType.values());
    configMap.put("shipBaseUrl", shipBaseUrl);
    configMap.put("templateTypes", TemplateType.values());
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
