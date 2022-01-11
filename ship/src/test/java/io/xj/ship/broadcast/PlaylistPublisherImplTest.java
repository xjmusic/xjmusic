// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.persistence.ChainManager;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.work.ShipWorkModule;
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
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistPublisherImplTest {
  // Under Test
  private PlaylistPublisher subject;

  @Mock
  private ChainManager chainManager;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Mock
  private FileStoreProvider fileStoreProvider;
  private BroadcastFactory broadcast;

  @Before
  public void setUp() {
    Environment env = Environment.from(ImmutableMap.of(
      "SHIP_CHUNK_TARGET_DURATION", "10",
      "SHIP_KEY", "coolair"
    ));
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ChainManager.class).toInstance(chainManager);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
        bind(Environment.class).toInstance(env);
        bind(SegmentAudioManager.class).toInstance(segmentAudioManager);
      }
    }));

    var chain = buildChain(buildTemplate(buildAccount("Testing"), "Testing"));
    chain.setTemplateConfig("metaSource = \"XJ Music Testing\"\nmetaTitle = \"Test Stream 5\"");

    broadcast = injector.getInstance(BroadcastFactory.class);

    subject = injector.getInstance(PlaylistPublisher.class);
  }

  @Test
  public void get() throws ShipException {
    var item = broadcast.chunk("coolair", 164030295L, "mp3", null);

    subject.putNext(item);
    assertSame(item, subject.get(164030295).orElseThrow());
  }

  /**
   Second attempt returns false (already seen this item)
   */
  @Test
  public void put() throws ShipException {
    var item = broadcast.chunk("coolair", 164030295L, "mp3", null);

    assertTrue(subject.putNext(item));
    assertFalse(subject.putNext(item));
  }

  @Test
  public void collectGarbage() throws ShipException {
    var item = broadcast.chunk("coolair", 164030295L, "mp3", null);

    subject.putNext(item);
    subject.collectGarbage(164030996);
    assertFalse(subject.get(164030295).isPresent());
  }

  @Test
  public void computeMediaSequence() {
    assertEquals(164030295, subject.computeMediaSeqNum(1640302958444L));
  }

  @Test
  public void loadItemsFromPlaylist_getPlaylistContent() throws IOException, ShipException {
    var reference_m3u8 = getResourceFileContent("coolair.m3u8");

    var added = subject.parseItems(reference_m3u8);
    for (var chunk : added) assertTrue(subject.putNext(chunk));
    assertEquals(20, added.size());

    var reAdded = subject.parseItems(reference_m3u8);
    for (var chunk : reAdded) assertFalse(subject.putNext(chunk));

    assertEquals(reference_m3u8, subject.getPlaylistContent(164029638));
  }

  @Test
  public void collectGarbage_recomputesMaxSequence_resetsOnEmpty() throws IOException, ShipException {
    var chunks = subject.parseItems(getResourceFileContent("coolair.m3u8"));
    for (var chunk : chunks) assertTrue(subject.putNext(chunk));
    assertEquals(164029657, subject.getMaxSequenceNumber());

    subject.collectGarbage(164029651);
    assertEquals(164029657, subject.getMaxSequenceNumber());

    subject.collectGarbage(164029959); // past end of playlist; will clear all
    assertEquals(0, subject.getMaxSequenceNumber());
  }

  @Test
  public void getMaxToSecondsUTC() throws IOException, ShipException {
    var reference_m3u8 = getResourceFileContent("coolair.m3u8");

    var added = subject.parseItems(reference_m3u8);
    for (var chunk : added) assertTrue(subject.putNext(chunk));

    assertEquals(1640296580, (int) subject.getMaxToSecondsUTC());
  }

}
