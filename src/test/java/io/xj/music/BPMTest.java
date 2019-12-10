// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import org.junit.Test;

import static org.junit.Assert.*;

public class BPMTest {
  @Test
  public void velocity() throws Exception {
    assertEquals(1, BPM.velocity(60),0);
    assertEquals(0.5, BPM.velocity(120),0);
    assertEquals(0.495, BPM.velocity(121),0.001);
  }

  @Test
  public void beatsNanos() throws Exception {
    assertEquals(100000000000L, BPM.beatsNanos(100, 60));
    assertEquals(31735537190L, BPM.beatsNanos(64, 121));
  }

}
