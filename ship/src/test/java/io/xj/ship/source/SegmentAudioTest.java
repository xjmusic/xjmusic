// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;


import io.xj.hub.enums.TemplateType;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.mixer.InternalResource;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SegmentAudioTest {
  @Mock
  ChainManager chainManager;
  @Mock
  SegmentAudioManager segmentAudioManager;
  @Mock
  TelemetryProvider telemetryProvider;
  private Segment segment1;
  private Chain chain1;
  private SegmentAudio subject;
  private SourceFactory factory;

  @Before
  public void setUp() {
    var account1 = buildAccount("Test");
    var template1 = buildTemplate(account1, TemplateType.Production, "Test 123", "test123");
    chain1 = buildChain(
      account1,
      template1,
      "Test Print #1",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2014-08-12T12:17:02.527142Z"));
    segment1 = buildSegment(
      chain1,
      1,
      SegmentState.CRAFTED,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg");
    segment1.setWaveformPreroll(1.7306228);
    segment1.setWaveformPostroll(1.205893);

    var env = AppEnvironment.getDefault();
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(env);
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, nexusEntityStore);
    factory = new SourceFactoryImpl(
      chainManager,
      env,
      httpClientProvider,
      jsonProvider,
      jsonapiPayloadFactory,
      segmentAudioManager,
      segmentManager,
      telemetryProvider
    );

    String sourcePath = new InternalResource("ogg_decoding/coolair-1633586832900943.wav").getFile().getAbsolutePath();
    subject = factory.loadSegmentAudio(chain1.getShipKey(), segment1, sourcePath);
  }

  @Test
  public void from() {
    String sourcePath = new InternalResource("ogg_decoding/coolair-1633586832900943.wav").getFile().getAbsolutePath();
    var result = factory.loadSegmentAudio(chain1.getShipKey(), segment1, sourcePath);

    assertEquals(result.getSegment(), subject.getSegment());
    assertEquals(SegmentAudioState.Ready, result.getState());
  }

  @Test
  public void getState() {
    assertEquals(SegmentAudioState.Ready, subject.getState());
  }

  @Test
  public void setState() {
    subject.setState(SegmentAudioState.Failed);

    assertEquals(SegmentAudioState.Failed, subject.getState());
  }

  @Test
  public void getId() {
    assertEquals(segment1.getId(), subject.getId());
  }

  @Test
  public void getSegment() {
    assertEquals(segment1, subject.getSegment());
  }

  @Test
  public void intersects() {
    assertTrue(subject.intersects(chain1.getShipKey(), Instant.parse("2017-12-12T01:00:09.000000Z"), Instant.parse("2017-12-12T01:00:15.000000Z")));
    assertTrue(subject.intersects(chain1.getShipKey(), Instant.parse("2017-12-12T01:00:00.000000Z"), Instant.parse("2017-12-12T01:00:12.000000Z")));
    assertTrue(subject.intersects(chain1.getShipKey(), Instant.parse("2017-12-12T01:00:10.000000Z"), Instant.parse("2017-12-12T01:00:30.000000Z")));
    assertFalse(subject.intersects(chain1.getShipKey(), Instant.parse("2017-12-12T01:00:04.000000Z"), Instant.parse("2017-12-12T01:00:05.000000Z")));
    assertFalse(subject.intersects(chain1.getShipKey(), Instant.parse("2017-12-12T01:00:18.000000Z"), Instant.parse("2017-12-12T01:00:21.000000Z")));
    // true because of waveform pre-roll
    assertTrue(subject.intersects(chain1.getShipKey(), Instant.parse("2017-12-12T01:00:01.000000Z"), Instant.parse("2017-12-12T01:00:07.000000Z")));
    // true because of waveform post-roll
    assertTrue(subject.intersects(chain1.getShipKey(), Instant.parse("2017-12-12T01:00:17.000000Z"), Instant.parse("2017-12-12T01:00:26.000000Z")));
  }
}
