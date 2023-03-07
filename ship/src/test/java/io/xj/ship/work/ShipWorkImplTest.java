// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.collect.ImmutableMap;
import io.xj.hub.HubTopology;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.ship.broadcast.BroadcastFactory;
import io.xj.ship.broadcast.BroadcastFactoryImpl;
import io.xj.ship.broadcast.ChunkFactory;
import io.xj.ship.broadcast.ChunkFactoryImpl;
import io.xj.ship.broadcast.MediaSeqNumProvider;
import io.xj.ship.broadcast.PlaylistPublisher;
import io.xj.ship.broadcast.PlaylistPublisherImpl;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SourceFactory;
import io.xj.ship.source.SourceFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class ShipWorkImplTest {
  @Mock
  BroadcastFactory broadcastFactory;
  @Mock
  ChainManager chainManager;
  @Mock
  Janitor janitor;
  @Mock
  NotificationProvider notificationProvider;
  @Mock
  SourceFactory sourceFactory;
  @Mock
  FileStoreProvider fileStoreProvider;
  @Mock
  SegmentAudioManager segmentAudioManager;
  @Mock
  TelemetryProvider telemetryProvider;

  private ShipWork subject;

  @Before
  public void setUp() throws Exception {
    AppEnvironment env = AppEnvironment.from(ImmutableMap.of("SHIP_KEY", "coolair"));
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    NexusEntityStore test = new NexusEntityStoreImpl(entityFactory);
    test.deleteAll();

    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(env);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, nexusEntityStore);
    sourceFactory = new SourceFactoryImpl(
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

    // Instantiate the test subject
    subject = new ShipWorkImpl(
      broadcastFactory,
      chunkFactory,
      chainManager,
      env,
      janitor,
      mediaSeqNumProvider,
      notificationProvider,
      playlistPublisher,
      segmentAudioManager,
      sourceFactory
    );
  }

/*
FUTURE: bring this back, but it's now necessary to get the stream process running before the health check is OK, by actually running the work!

  @Test
  public void isHealthy() {
    assertTrue(subject.isHealthy());
  }
*/

  @Test
  public void isHealthy_neverWithoutShipKey() {
    assertFalse(subject.isHealthy());
  }

}
