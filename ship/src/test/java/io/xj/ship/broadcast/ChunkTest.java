package io.xj.ship.broadcast;

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
    var injector = Guice.createInjector(Modules.override(new ShipBroadcastModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(Environment.getDefault());
      }
    }));
    var broadcast = injector.getInstance(ShipBroadcastFactory.class);
    subject = broadcast.chunk(SHIP_KEY, 1513040420);
  }

  @Test
  public void getFromSecondsUTC() {
    assertEquals(1513040420L, (long) subject.getFromSecondsUTC());
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
    assertEquals("test63-128kbps-151304042", subject.getKey(128000));
  }

  @Test
  public void addStreamOutputKey_getStreamOutputKeys() {
    assertEquals(ImmutableList.of("test63-128kbps-151304042.m4a"),
      subject.addStreamOutputKey("test63-128kbps-151304042.m4a").getStreamOutputKeys());
  }
}
