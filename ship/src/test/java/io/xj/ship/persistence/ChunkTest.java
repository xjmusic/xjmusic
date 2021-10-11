package io.xj.ship.persistence;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChunkTest {
  private static final String SHIP_KEY = "test63";
  private Chunk subject;

  @Before
  public void setUp() {
    subject = Chunk.from(SHIP_KEY, 1513040424, 6);
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
    assertEquals("test63-252173404", subject.getKey());
  }

  @Test
  public void addStreamOutputKey_getStreamOutputKeys() {
    assertEquals(ImmutableList.of("test63-252173404.ts"),
      subject.addStreamOutputKey("test63-252173404.ts").getStreamOutputKeys());
  }
}
