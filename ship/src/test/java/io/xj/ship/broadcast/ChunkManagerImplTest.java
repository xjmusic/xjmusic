// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.ship.work.ShipWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChunkManagerImplTest {

  // Fixtures
  private static final String SHIP_KEY = "test5";
  // Under Test
  private ChunkManager subject;
  private BroadcastFactory broadcastFactory;
  private Chunk chunk1;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Mock
  private ChainManager chainManager;

  @Before
  public void setUp() throws ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(ChainManager.class).toInstance(chainManager);
        bind(Environment.class).toInstance(env);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
      }
    }));

    when(chainManager.readOneByShipKey(eq(SHIP_KEY)))
      .thenReturn(buildChain(buildTemplate(buildAccount("Testing"), "Testing")));
    when(chainManager.existsForShipKey(eq(SHIP_KEY)))
      .thenReturn(true);

    subject = injector.getInstance(ChunkManager.class);
    broadcastFactory = injector.getInstance(BroadcastFactory.class);
    chunk1 = broadcastFactory.chunk(SHIP_KEY, 1513040420);
    subject.clear();
  }

  @Test
  public void testIsAssembledFarEnoughAhead() {
    subject.put(chunk1.setState(ChunkState.Done));
    subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040430).setState(ChunkState.Done));
    subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040440).setState(ChunkState.Done));
    subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040450).setState(ChunkState.Done));
    subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040460).setState(ChunkState.Done));
    subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040470).setState(ChunkState.Done));

    assertTrue(subject.isAssembledFarEnoughAhead(SHIP_KEY, 1513040420000L));
  }

  @Test
  public void testIsAssembledFarEnoughAhead_false() {
    assertFalse(subject.isAssembledFarEnoughAhead(SHIP_KEY, 1513040420000L));
  }

  @Test
  public void testComputeAssembledToMillis() {
    subject.put(chunk1.setState(ChunkState.Done));
    subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040430).setState(ChunkState.Done));
    subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040440).setState(ChunkState.Done));

    assertEquals(1513040450000L, subject.computeAssembledToMillis(SHIP_KEY, 1513040420000L));
  }

  /**
   This computes all the expected chunks, given the ship chunks ahead value
   */
  @Test
  public void testGetAll() {
    assertEquals(6, subject.getAll(SHIP_KEY, 1513040430000L).size());
  }

  @Test
  public void testGetContiguousDone() {
    subject.put(chunk1.setState(ChunkState.Done));

    assertEquals(ImmutableList.of(chunk1), subject.getContiguousDone(SHIP_KEY, 1513040420000L));
  }

  @Test
  public void testPut() {
    subject.put(chunk1.setState(ChunkState.Done));
    var chunk2 = subject.put(broadcastFactory.chunk(SHIP_KEY, 1513040430).setState(ChunkState.Done));

    assertEquals(ImmutableList.of(chunk1, chunk2), subject.getContiguousDone(SHIP_KEY, 1513040420000L));
  }

  @Test
  public void isInitialized_didInitialize() {
    assertFalse(subject.isInitialized("test123"));
    subject.didInitialize("test123");
    assertTrue(subject.isInitialized("test123"));
  }

}
