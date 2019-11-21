// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExposureTest {

  @After
  public void cleanup() {
    System.clearProperty("audio.url.base");
  }

  @Before
  public void setup() {
    System.setProperty("audio.url.base", "http://mush/");
  }

  @Test
  public void audioUrl() throws Exception {
    assertEquals(("http://mush/bun.wav"), Config.getAudioUrl("bun.wav"));
  }

}
