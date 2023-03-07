// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.common.collect.ImmutableMap;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.ship.ShipException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.lib.util.Files.getResourceFileContent;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistPublisherImplTest {
  // Under Test
  private PlaylistPublisher playlistPublisher;
  @Mock
  private FileStoreProvider fileStoreProvider;
  @Mock
  TelemetryProvider telemetryProvider;
  private ChunkFactory chunkFactory;

  @Before
  public void setUp() {
    AppEnvironment env = AppEnvironment.from(ImmutableMap.of(
      "SHIP_CHUNK_TARGET_DURATION", "10",
      "SHIP_KEY", "coolair"
    ));
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(env);
    chunkFactory = new ChunkFactoryImpl(env);
    MediaSeqNumProvider mediaSeqNumProvider = new MediaSeqNumProvider(env);
    playlistPublisher = new PlaylistPublisherImpl(env, chunkFactory, fileStoreProvider, httpClientProvider, mediaSeqNumProvider, telemetryProvider);

    var chain = buildChain(buildTemplate(buildAccount("Testing"), "Testing"));
    chain.setTemplateConfig("metaSource = \"XJ Music Testing\"\nmetaTitle = \"Test Stream 5\"");
  }

  @Test
  public void get() throws ShipException {
    var item = chunkFactory.build("coolair", 164030295L, "mp3", null);

    playlistPublisher.putNext(item);
    assertSame(item, playlistPublisher.get(164030295).orElseThrow());
  }

  /**
   * Second attempt returns false (already seen this item)
   */
  @Test
  public void put() throws ShipException {
    var item = chunkFactory.build("coolair", 164030295L, "mp3", null);

    assertTrue(playlistPublisher.putNext(item));
    assertFalse(playlistPublisher.putNext(item));
  }

  @Test
  public void collectGarbage() throws ShipException {
    var item = chunkFactory.build("coolair", 164030295L, "mp3", null);

    playlistPublisher.putNext(item);
    playlistPublisher.collectGarbage(164030996);
    assertFalse(playlistPublisher.get(164030295).isPresent());
  }

  @Test
  public void loadItemsFromPlaylist_getPlaylistContent() throws IOException, ShipException {
    var reference_m3u8 = getResourceFileContent("coolair.m3u8");

    var added = playlistPublisher.parseItems(reference_m3u8);
    for (var chunk : added) assertTrue(playlistPublisher.putNext(chunk));
    assertEquals(20, added.size());

    var reAdded = playlistPublisher.parseItems(reference_m3u8);
    for (var chunk : reAdded) assertFalse(playlistPublisher.putNext(chunk));

    assertEquals(reference_m3u8, playlistPublisher.getPlaylistContent(164029638));
  }

  @Test
  public void collectGarbage_recomputesMaxSequence_resetsOnEmpty() throws IOException, ShipException {
    var chunks = playlistPublisher.parseItems(getResourceFileContent("coolair.m3u8"));
    for (var chunk : chunks) assertTrue(playlistPublisher.putNext(chunk));
    assertEquals(164029657, playlistPublisher.getMaxSequenceNumber());

    playlistPublisher.collectGarbage(164029651);
    assertEquals(164029657, playlistPublisher.getMaxSequenceNumber());

    playlistPublisher.collectGarbage(164029959); // past end of playlist; will clear all
    assertEquals(0, playlistPublisher.getMaxSequenceNumber());
  }

  @Test
  public void getMaxToSecondsUTC() throws IOException, ShipException {
    var reference_m3u8 = getResourceFileContent("coolair.m3u8");

    var added = playlistPublisher.parseItems(reference_m3u8);
    for (var chunk : added) assertTrue(playlistPublisher.putNext(chunk));

    assertEquals(1640296580, (int) playlistPublisher.getMaxToSecondsUTC());
  }

}
