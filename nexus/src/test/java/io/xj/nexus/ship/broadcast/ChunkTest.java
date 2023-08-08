// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ChunkTest {
  static final String SHIP_KEY = "test63";
  Chunk subject;

  @BeforeEach
  public void setUp() {
    ChunkFactory chunkFactory = new ChunkFactoryImpl("aac", 10);

    subject = chunkFactory.build(SHIP_KEY, 3L, 90000000L, null, "mp3");
  }

  @Test
  public void getFromChainMicros() {
    assertEquals(90000000L, (long) subject.getFromChainMicros());
  }

  @Test
  public void getShipKey() {
    assertEquals(SHIP_KEY, subject.getShipKey());
  }

  @Test
  public void getKey() {
    assertEquals("test63-128k-3", subject.getKey(128000));
  }

  // Used in ffmpeg parameter for generating an HLS stream
  @Test
  public void getKeyTemplate() {
    assertEquals("test63-128k-%d", subject.getKeyTemplate(128000));
  }

}
