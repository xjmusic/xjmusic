// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

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

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;

@RunWith(MockitoJUnitRunner.class)
public class PlaylistPublisherImplTest {
  // Fixtures
  private static final String SHIP_KEY = "test5";
  // Under Test
  private PlaylistPublisher subject;

  @Mock
  private ChainManager chainManager;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() {
    var env = Environment.getDefault();
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

    subject = injector.getInstance(BroadcastFactory.class).publisher(SHIP_KEY);
  }

  @Test
  public void publish() throws ShipException {
    subject.publish(System.currentTimeMillis());
  }
}
