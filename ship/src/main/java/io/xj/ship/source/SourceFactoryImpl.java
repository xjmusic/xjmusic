package io.xj.ship.source;

import io.xj.lib.app.AppEnvironment;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.SegmentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SourceFactoryImpl implements SourceFactory {
  ChainManager chainManager;
  AppEnvironment env;
  HttpClientProvider httpClientProvider;
  JsonProvider jsonProvider;
  JsonapiPayloadFactory jsonapiPayloadFactory;
  SegmentAudioManager segmentAudioManager;
  SegmentManager segmentManager;
  TelemetryProvider telemetryProvider;

  @Autowired
  public SourceFactoryImpl(
    ChainManager chainManager,
    AppEnvironment env,
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    SegmentAudioManager segmentAudioManager,
    SegmentManager segmentManager,
    TelemetryProvider telemetryProvider
  ) {
    this.chainManager = chainManager;
    this.env = env;
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.segmentAudioManager = segmentAudioManager;
    this.segmentManager = segmentManager;
    this.telemetryProvider = telemetryProvider;
  }

  @Override
  public ChainLoader loadChain(String shipKey, Runnable onFailure) {
    return new ChainLoaderImpl(shipKey, onFailure, chainManager, env, httpClientProvider, jsonProvider, jsonapiPayloadFactory, segmentAudioManager, segmentManager, telemetryProvider);
  }

  @Override
  public SegmentAudio loadSegmentAudio(String shipKey, Segment segment, String absolutePath) {
    return new SegmentAudio(absolutePath, segment, shipKey, telemetryProvider, env);
  }
}
