// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.*;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.InternalResource;
import io.xj.lib.util.ValueException;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.ship.source.SegmentAudio;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.source.SourceFactory;
import io.xj.ship.work.ShipWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.TrackRunBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.lib.util.Assertion.assertFileMatchesResourceFile;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mp4parser.tools.Path.getPath;

@RunWith(MockitoJUnitRunner.class)
public class ChunkPrinterImplTest {
  private static final Logger LOG = LoggerFactory.getLogger(ChunkPrinterImplTest.class);
  // Fixture
  private static final String SHIP_KEY = "test5";
  // Under Test
  private ChunkPrinter subject;
  private Collection<SegmentAudio> segmentAudios;
  private Segment segment2;
  private SourceFactory source;

  @Mock
  private ChainManager chainManager;

  @Mock
  private ChunkManager chunkManager;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Before
  public void setUp() throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
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
        bind(ChainManager.class).toInstance(chainManager);
        bind(ChunkManager.class).toInstance(chunkManager);
        bind(Environment.class).toInstance(env);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
        bind(SegmentAudioManager.class).toInstance(segmentAudioManager);
      }
    }));
    source = injector.getInstance(SourceFactory.class);
    BroadcastFactory broadcast = injector.getInstance(BroadcastFactory.class);

    when(chainManager.readOneByShipKey(eq(SHIP_KEY)))
      .thenReturn(buildChain(buildTemplate(buildAccount("Testing"), "Testing")));

    Chunk chunk = broadcast.chunk(SHIP_KEY, 1513040420);
    segmentAudios = Lists.newArrayList();

    when(segmentAudioManager.getAllIntersecting(
      eq(chunk.getShipKey()),
      eq(chunk.getFromInstant()),
      eq(chunk.getToInstant())))
      .thenReturn(segmentAudios);

    when(chunkManager.isInitialized(eq(SHIP_KEY))).thenReturn(false);

    subject = broadcast.printer(chunk);
  }

  @Test
  public void run() throws IOException, ValueException {
    var loader = ChunkPrinterImplTest.class.getClassLoader();
    var input = loader.getResourceAsStream("ogg_decoding/coolair-1633586832900943.ogg");
    segmentAudios.add(source.segmentAudio(SHIP_KEY, segment2).loadOggVorbis(input));

    subject.print();

    assertFileMatchesResourceFile("chunk_reference_outputs/test5-151304042.wav", subject.getWavFilePath());
    logAllMp4Boxes("EXPECTED M4s", new InternalResource("chunk_reference_outputs/test5-128k-151304042.m4s").getFile().getAbsolutePath());
    logAllMp4Boxes("EXPECTED M4s (mp4box)", new InternalResource("chunk_reference_outputs/test5-128k-151304042-mp4box.m4s").getFile().getAbsolutePath());
    logAllMp4Boxes("ACTUAL M4S", subject.getM4sFilePath());
    assertEquals("Samples in fragment", 470, ((TrackRunBox) getPath(getIsoFile(subject.getM4sFilePath()), "moof/traf/trun")).getEntries().size());
    //
    logAllMp4Boxes("EXPECTED INIT MP4 (ffmpeg)", new InternalResource("chunk_reference_outputs/test5-128k-IS-ffmpeg.mp4").getFile().getAbsolutePath());
    logAllMp4Boxes("EXPECTED INIT MP4 (mp4box)", new InternalResource("chunk_reference_outputs/test5-128k-IS-mp4box.mp4").getFile().getAbsolutePath());
    logAllMp4Boxes("ACTUAL INIT MP4", subject.getMp4InitFilePath());
  }

  /**
   Log all MP4 boxes of the given file path

   @param name of this log section
   @param path to file
   @throws IOException on failure
   */
  private void logAllMp4Boxes(String name, String path) throws IOException {
    LOG.info("----[ {} ]----", name);
    for (var box : getIsoFile(path).getBoxes()) LOG.info("{}", box.toString());
    LOG.info("--------------");
  }

  /**
   Get the boxes of an MP4 file

   @param path of file to get
   @return mp4 boxes
   @throws IOException on failure
   */
  private IsoFile getIsoFile(String path) throws IOException {
    var dataSource = Files.newByteChannel(Path.of(path));
    return new IsoFile(dataSource);
  }

  @Test
  public void run_nothingFromNothing() {
    subject.print();

    verify(chunkManager, never()).put(any());
    assertNull(subject.getOutputPcmData());
  }

  @Test
  public void run_nothingFromUnreadyAudio() {
    segmentAudios.add(source.segmentAudio(SHIP_KEY, segment2));

    subject.print();

    verify(chunkManager, never()).put(any());
    assertNull(subject.getOutputPcmData());
  }

  @Test
  public void getWavFilePath() {
    assertEquals("/tmp/test5-151304042.wav", subject.getWavFilePath());
  }

}
