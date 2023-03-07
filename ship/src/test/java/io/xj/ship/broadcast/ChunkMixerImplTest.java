// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableMap;


import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.model.*;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.InternalResource;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.ship.source.SegmentAudio;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SourceFactory;
import io.xj.ship.source.SourceFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sound.sampled.AudioFormat;
import java.time.Instant;
import java.util.Collection;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChunkMixerImplTest {
  // Fixture
  private static final String SHIP_KEY = "test5";
  // Under Test
  private Collection<SegmentAudio> segmentAudios;
  private Segment segment2;
  private SourceFactory source;
  private ChunkMixer subject;
  @Mock
  ChainManager chainManager;
  @Mock
  FileStoreProvider fileStoreProvider;
  @Mock
  SegmentAudioManager segmentAudioManager;
  @Mock
  TelemetryProvider telemetryProvider;
  @Mock
  NotificationProvider notificationProvider;

  @Before
  public void setUp() {
    AppEnvironment env = AppEnvironment.from(ImmutableMap.of("SHIP_KEY", "coolair"));
    Account account1 = buildAccount("Testing");
    Template template1 = buildTemplate(account1, "fonds", "ABC");
    Chain chain1 = buildChain(
      account1,
      template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z"));
    segment2 = buildSegment(
      chain1,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:32.000000Z"),
      "G major",
      32,
      0.6,
      120.0,
      "seg123",
      "ogg");

    AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
      48000,
      32,
      2,
      8,
      48000,
      false);

    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(env);
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, nexusEntityStore);
    source = new SourceFactoryImpl(
      chainManager,
      env,
      httpClientProvider,
      jsonProvider,
      jsonapiPayloadFactory,
      segmentAudioManager,
      segmentManager,
      telemetryProvider
    );
    ChunkFactory chunkFactory = new ChunkFactoryImpl(env);
    MediaSeqNumProvider mediaSeqNumProvider = new MediaSeqNumProvider(env);
    PlaylistPublisher playlistPublisher = new PlaylistPublisherImpl(env, chunkFactory, fileStoreProvider, httpClientProvider, mediaSeqNumProvider, telemetryProvider);
    BroadcastFactory broadcast = new BroadcastFactoryImpl(env, playlistPublisher, fileStoreProvider, notificationProvider, segmentAudioManager);

    Chunk chunk = chunkFactory.build(SHIP_KEY, 151304042L, "mp3", null);

    segmentAudios = Lists.newArrayList();

    when(segmentAudioManager.getAllIntersecting(
      eq(chunk.getShipKey()),
      eq(chunk.getFromInstant()),
      eq(chunk.getToInstant())))
      .thenReturn(segmentAudios);

    subject = broadcast.mixer(chunk, format);
  }

  @Test
  public void run() throws Exception {
    String sourcePath = new InternalResource("ogg_decoding/coolair-1633586832900943.wav").getFile().getAbsolutePath();
    segmentAudios.add(source.loadSegmentAudio(SHIP_KEY, segment2, sourcePath));

    subject.mix();

    // FUTURE assertFileMatchesResourceFile("chunk_reference_outputs/test5-151304042.wav", subject.getWavFilePath());
  }

  @Test
  public void run_nothingFromNothing() throws Exception {
    subject.mix();

    // FUTURE verify(chunkManager, never()).put(any());
  }

}
