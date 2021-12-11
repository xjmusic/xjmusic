// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamPublisherImplTest {
  // Fixtures
  private static final String SHIP_KEY = "test5";
  // Under Test
  private StreamPublisher subject;
  private Chunk chunk0;

  @Mock
  private ChainManager chainManager;

  @Mock
  private SegmentAudioManager segmentAudioManager;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
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
    when(chainManager.readOneByShipKey(eq(SHIP_KEY))).thenReturn(chain);

    chunk0 = injector.getInstance(BroadcastFactory.class).chunk(SHIP_KEY, 1513040420);

    subject = injector.getInstance(BroadcastFactory.class).publisher(SHIP_KEY);
  }

  @Test
  public void publish() {
    subject.publish(System.currentTimeMillis());
  }
}
