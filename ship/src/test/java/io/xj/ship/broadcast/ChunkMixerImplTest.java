// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.*;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.InternalResource;
import io.xj.nexus.persistence.ChainManager;
import io.xj.ship.source.SegmentAudio;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SourceFactory;
import io.xj.ship.work.ShipWorkModule;
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
  private ChainManager chainManager;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Before
  public void setUp() {
    Environment env = Environment.from(ImmutableMap.of("SHIP_KEY", "coolair"));
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

    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ChainManager.class).toInstance(chainManager);
        bind(Environment.class).toInstance(env);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
        bind(SegmentAudioManager.class).toInstance(segmentAudioManager);
      }
    }));
    source = injector.getInstance(SourceFactory.class);
    BroadcastFactory broadcast = injector.getInstance(BroadcastFactory.class);

    Chunk chunk = broadcast.chunk(SHIP_KEY, 151304042L, "mp3", null);

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
    segmentAudios.add(source.segmentAudio(SHIP_KEY, segment2, sourcePath));

    subject.mix();

    // FUTURE assertFileMatchesResourceFile("chunk_reference_outputs/test5-151304042.wav", subject.getWavFilePath());
  }

  @Test
  public void run_nothingFromNothing() throws Exception {
    subject.mix();

    // FUTURE verify(chunkManager, never()).put(any());
  }

}
