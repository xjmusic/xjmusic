package io.xj.ship.broadcast;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.ship.work.ShipWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ChunkManagerImplTest {

  // Under Test
  private ChunkManager subject;
  private ShipBroadcastFactory broadcastFactory;

  // Fixtures
  private static final String SHIP_KEY = "test5";
  private Chunk chunk1;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
      }
    }));
    subject = injector.getInstance(ChunkManager.class);
    broadcastFactory = injector.getInstance(ShipBroadcastFactory.class);
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
   * This computes all the expected chunks, given the ship chunks ahead value
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
