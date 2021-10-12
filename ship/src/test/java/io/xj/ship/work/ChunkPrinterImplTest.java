package io.xj.ship.work;

import com.google.api.client.util.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.ValueException;
import io.xj.ship.persistence.Chunk;
import io.xj.ship.persistence.ChunkManager;
import io.xj.ship.persistence.SegmentAudio;
import io.xj.ship.persistence.ShipPersistenceFactory;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChunkPrinterImplTest {

  // Under Test
  private ChunkPrinter subject;

  // Fixture
  private static final String SHIP_KEY = "test5";
  private Collection<SegmentAudio> segmentAudios;
  private Segment segment2;
  private ShipPersistenceFactory persistenceFactory;

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
    persistenceFactory = injector.getInstance(ShipPersistenceFactory.class);

    Chunk chunk = persistenceFactory.chunk(SHIP_KEY, 1513040424);
    segmentAudios = Lists.newArrayList();

    when(segmentAudioManager.getAllIntersecting(
      eq(chunk.getShipKey()),
      eq(chunk.getFromInstant()),
      eq(chunk.getToInstant())))
      .thenReturn(segmentAudios);

    subject = factory.printer(chunk);
  }

  @Test
  public void run() throws ValueException, IOException, FileStoreException {
    var loader = ChunkPrinterImplTest.class.getClassLoader();
    var input = loader.getResourceAsStream("ogg_decoding/coolair-1633586832900943.ogg");
    segmentAudios.add(persistenceFactory.segmentAudio(SHIP_KEY, segment2).loadOggVorbis(input));

    subject.print();

    verify(chunkManager, times(4)).put(any());
    assertFileMatchesResourceFile("/tmp/test5-1513040424.wav", "chunk_reference_outputs/test5-1513040424.wav");
    assertFileSizeToleranceFromResourceFile("/tmp/test5-1513040424.ts", "chunk_reference_outputs/test5-1513040424.ts");
    verify(fileStoreProvider, times(1))
      .putS3ObjectFromTempFile(eq("/tmp/test5-1513040424.ts"), eq("xj-dev-stream"), eq("test5-1513040424.ts"));
  }

  @Test
  public void run_nothingFromNothing() {
    subject.print();

    verify(chunkManager, never()).put(any());
    assertNull(subject.getOutputPcmData());
  }

  @Test
  public void run_nothingFromUnreadyAudio() {
    segmentAudios.add(persistenceFactory.segmentAudio(SHIP_KEY, segment2));

    subject.print();

    verify(chunkManager, never()).put(any());
    assertNull(subject.getOutputPcmData());
  }

  @Test
  public void getWavFilePath() {
    assertEquals("/tmp/test5-1513040424.wav", subject.getWavFilePath());
  }

  @Test
  public void getTsFilePath() {
    assertEquals("/tmp/test5-1513040424.ts", subject.getTsFilePath());
  }


}
