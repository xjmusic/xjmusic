// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.persistence.ChainManager;
import io.xj.ship.source.SegmentAudioManager;
import io.xj.ship.work.ShipWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MediaSeqNumProviderTest {
  // Under Test
  private MediaSeqNumProvider subject;

  @Mock
  private ChainManager chainManager;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Mock
  private FileStoreProvider fileStoreProvider;

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

    subject = injector.getInstance(MediaSeqNumProvider.class);
  }


  @Test
  public void computeMediaSequence() {
    assertEquals(164030295, subject.computeMediaSeqNum(1640302958444L));
  }

}
