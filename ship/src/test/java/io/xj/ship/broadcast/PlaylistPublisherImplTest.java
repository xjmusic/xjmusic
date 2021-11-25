// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import io.xj.lib.util.ValueException;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.work.ShipWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.lib.util.Files.getResourceFileContent;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistPublisherImplTest {
  // Fixtures
  private static final String SHIP_KEY = "test5";
  // Under Test
  private PlaylistPublisher subject;
  private Chunk chunk0;

  @Mock
  private ChainManager chainManager;

  @Mock
  private ChunkManager chunkManager;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Before
  public void setUp() throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ChainManager.class).toInstance(chainManager);
        bind(ChunkManager.class).toInstance(chunkManager);
        bind(Environment.class).toInstance(env);
        bind(SegmentAudioManager.class).toInstance(segmentAudioManager);
      }
    }));

    var chain = buildChain(buildTemplate(buildAccount("Testing"), "Testing"));
    chain.setTemplateConfig("metaSource = \"XJ Music Testing\"\nmetaTitle = \"Test Stream 5\"");
    when(chainManager.readOneByShipKey(eq(SHIP_KEY))).thenReturn(chain);

    chunk0 = injector.getInstance(BroadcastFactory.class)
      .chunk(SHIP_KEY, 1513040420).setState(ChunkState.Done).addStreamOutputKey("test5-128000-151304042.m4a");

    subject = injector.getInstance(BroadcastFactory.class).publisher(SHIP_KEY);
  }

  @Test
  public void computeMPD() throws IOException, ShipException, ValueException, ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
    when(chunkManager.getAll(eq(SHIP_KEY), eq(1513040450000L))).thenReturn(List.of(chunk0));
    when(chunkManager.getContiguousDone(eq(SHIP_KEY), eq(1513040450000L))).thenReturn(List.of(chunk0));

    var result = subject.computeMPD(1513040450000L);

    assertMatchesResourceFile(result, "mpeg_dash_playlist/test5.mpd");
  }

  @Test
  public void computeM3U8() throws IOException, ShipException, ValueException, ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
    when(chunkManager.getAll(eq(SHIP_KEY), eq(1513040450000L))).thenReturn(List.of(chunk0));
    when(chunkManager.getContiguousDone(eq(SHIP_KEY), eq(1513040450000L))).thenReturn(List.of(chunk0));

    var result = subject.computeM3U8(1513040450000L);

    assertMatchesResourceFile(result, "mpeg_dash_playlist/test5.m3u8");
  }

  /**
   Assert this matches the reference file

   @param content           to test
   @param referenceFilePath to reference
   @throws IOException on failure
   */
  @SuppressWarnings("SameParameterValue")
  private void assertMatchesResourceFile(String content, String referenceFilePath) throws IOException {
    assertEquals("Demo output (" + content.length() + " bytes) does not match reference file " + referenceFilePath + "!",
      getResourceFileContent(referenceFilePath), content);
  }
}
