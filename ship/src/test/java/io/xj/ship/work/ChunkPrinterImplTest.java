package io.xj.ship.work;

import com.google.api.client.util.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.*;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.ValueException;
import io.xj.ship.persistence.Chunk;
import io.xj.ship.persistence.ChunkManager;
import io.xj.ship.persistence.SegmentAudio;
import io.xj.ship.persistence.SegmentAudioManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.lib.util.Assertion.assertFileMatchesResourceFile;
import static io.xj.lib.util.Assertion.assertFileSizeToleranceFromResourceFile;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChunkPrinterImplTest {

  // Under Test
  private ChunkPrinter subject;

  // Fixture
  private static final String SHIP_KEY = "test5";
  private Segment segment2;
  private Collection<SegmentAudio> segmentAudios;

  @Mock
  private ChunkManager chunkManager;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Before
  public void setUp() {
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
      "seg123.ogg",
      "wav");

    Chunk chunk = Chunk.from(SHIP_KEY, 1513040422, 6);
    segmentAudios = Lists.newArrayList();

    when(segmentAudioManager.getAllIntersecting(
      eq(chunk.getShipKey()),
      eq(chunk.getFromInstant()),
      eq(chunk.getToInstant())))
      .thenReturn(segmentAudios);

    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ChunkManager.class).toInstance(chunkManager);
        bind(Environment.class).toInstance(env);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
        bind(SegmentAudioManager.class).toInstance(segmentAudioManager);
      }
    }));
    var factory = injector.getInstance(WorkFactory.class);
    subject = factory.print(chunk);
  }

  @Test
  public void compute() throws ValueException, IOException, FileStoreException {
    var loader = ChunkPrinterImplTest.class.getClassLoader();
    var input = loader.getResourceAsStream("ogg_decoding/coolair-1633586832900943.ogg");
    segmentAudios.add(SegmentAudio.from(segment2, SHIP_KEY).loadOggVorbis(input));

    subject.compute();

    verify(chunkManager, times(4)).put(any());
    assertFileMatchesResourceFile("/tmp/test5-1513040422.wav", "chunk_reference_outputs/test5-1513040422.wav");
    assertFileSizeToleranceFromResourceFile("/tmp/test5-1513040422-128k.ts", "chunk_reference_outputs/test5-1513040422-128k.ts");
    verify(fileStoreProvider, times(1))
      .putS3ObjectFromTempFile(eq("/tmp/test5-1513040422-128k.ts"), eq("xj-dev-stream"), eq("test5-1513040422-128k.ts"));
  }

  @Test
  public void compute_nothingFromNothing() {
    subject.compute();

    verify(chunkManager, never()).put(any());
    assertNull(subject.getOutputPcmData());
  }

  @Test
  public void compute_nothingFromUnreadyAudio() {
    segmentAudios.add(SegmentAudio.from(segment2, SHIP_KEY));

    subject.compute();

    verify(chunkManager, never()).put(any());
    assertNull(subject.getOutputPcmData());
  }

  @Test
  public void getWavFilePath() {
    assertEquals("/tmp/test5-1513040422.wav", subject.getWavFilePath());
  }

  @Test
  public void getTsFilePath() {
    assertEquals("/tmp/test5-1513040422-128k.ts", subject.getTsFilePath());
  }


}
