// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import io.outright.xj.music.BPM;

import org.junit.Test;

import static org.junit.Assert.*;

public class BPMTest {

  @Test
  public void beatsNanos() throws Exception {
    assertEquals(100000000000L, BPM.beatsNanos(100, 60));
    assertEquals(31735537190L, BPM.beatsNanos(64, 121));
  }

}
