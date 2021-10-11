package io.xj.ship.persistence;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ChunkManagerImplTest {

  // Under Test
  private ChunkManager subject;

  // Fixtures
  private static final String SHIP_KEY = "test5";
  private Chunk chunk1;

  @Mock
  private FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() {
    chunk1 = Chunk.from(SHIP_KEY, 1513040424, 6);

    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new ShipWorkModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
      }
    }));
    subject = injector.getInstance(ChunkManager.class);
    subject.clear();
  }

  @Test
  public void testIsAssembledFarEnoughAhead() {
    subject.put(chunk1.setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040430, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040436, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040442, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040448, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040454, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040460, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040466, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040472, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040478, 6).setState(ChunkState.Done));

    assertTrue(subject.isAssembledFarEnoughAhead(SHIP_KEY, 1513040424000L));
  }

  @Test
  public void testIsAssembledFarEnoughAhead_false() {
    assertFalse(subject.isAssembledFarEnoughAhead(SHIP_KEY, 1513040424000L));
  }

  @Test
  public void testComputeAssembledToMillis() {
    subject.put(chunk1.setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040430, 6).setState(ChunkState.Done));
    subject.put(Chunk.from(SHIP_KEY, 1513040436, 6).setState(ChunkState.Done));

    assertEquals(1513040442000L, subject.computeAssembledToMillis(SHIP_KEY, 1513040424000L));
  }

  /**
   This computes all the expected chunks, given the ship chunks ahead value
   */
  @Test
  public void testGetAll() {
    assertEquals(10, subject.getAll(SHIP_KEY, 1513040428000L).size());
  }

  @Test
  public void testGetContiguousDone() {
    subject.put(chunk1.setState(ChunkState.Done));

    assertEquals(ImmutableList.of(chunk1), subject.getContiguousDone(SHIP_KEY, 1513040428000L));
  }

  @Test
  public void testPut() {
    subject.put(chunk1.setState(ChunkState.Done));
    var chunk2 = subject.put(Chunk.from(SHIP_KEY, 1513040430, 6).setState(ChunkState.Done));

    assertEquals(ImmutableList.of(chunk1, chunk2), subject.getContiguousDone(SHIP_KEY, 1513040428000L));
  }
}
