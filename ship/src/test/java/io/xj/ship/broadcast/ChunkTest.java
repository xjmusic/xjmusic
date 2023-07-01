// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class ChunkTest {
  private static final String SHIP_KEY = "test63";
  private Chunk subject;

  @Before
  public void setUp() {
    ChunkFactory chunkFactory = new ChunkFactoryImpl("aac", 10);

    subject = chunkFactory.build(SHIP_KEY, 151304042L, "mp3", null);
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
  public void getKey() {
    assertEquals("test63-128k-151304042", subject.getKey(128000));
  }

  // Used in ffmpeg parameter for generating an HLS stream
  @Test
  public void getKeyTemplate() {
    assertEquals("test63-128k-%d", subject.getKeyTemplate(128000));
  }

}
