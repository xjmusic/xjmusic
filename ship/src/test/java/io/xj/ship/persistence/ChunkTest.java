package io.xj.ship.persistence;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.app.Environment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChunkTest {
  private static final String SHIP_KEY = "test63";
  private Chunk subject;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(Modules.override(new ShipPersistenceModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(Environment.getDefault());
      }
    }));
    var persistenceFactory = injector.getInstance(ShipPersistenceFactory.class);
    subject = persistenceFactory.chunk(SHIP_KEY, 1513040424);
  }

  @Test
  public void getFromSecondsUTC() {
    assertEquals(1513040424L, (long) subject.getFromSecondsUTC());
  }

  @Test
  public void getShipKey() {
    assertEquals(SHIP_KEY, subject.getShipKey());
  }

  @Test
  public void setState_getState() {
    subject.setState(ChunkState.Done);
    assertEquals(ChunkState.Done, subject.getState());
  }

  @Test
  public void getKey() {
    assertEquals("test63-1513040424", subject.getKey());
  }

  @Test
  public void addStreamOutputKey_getStreamOutputKeys() {
    assertEquals(ImmutableList.of("test63-1513040424.ts"),
      subject.addStreamOutputKey("test63-1513040424.ts").getStreamOutputKeys());
  }
}
