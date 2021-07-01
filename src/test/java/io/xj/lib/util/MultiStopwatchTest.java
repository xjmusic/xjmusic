// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 Measures a series of named sections of time
 */
public class MultiStopwatchTest {
  MultiStopwatch subject;

  @Before
  public void start() throws Exception {
    subject = MultiStopwatch.start();
  }

  @Test
  public void stop_getTotalSeconds() throws InterruptedException {
    Thread.sleep(10);
    subject.stop();

    assertTrue(0.01 <= subject.getTotalSeconds());
  }

  @Test
  public void section_getSectionTotalSeconds_toString() throws InterruptedException {
    subject.section("One");
    Thread.sleep(10);
    subject.section("Two");
    Thread.sleep(10);
    subject.stop();

    assertTrue(0.02 <= subject.getTotalSeconds());
    assertTrue(0.01 <= subject.getSectionTotalSeconds().get("One"));
    assertTrue(0.01 <= subject.getSectionTotalSeconds().get("Two"));
    var str = subject.toString();
    assertTrue(str.contains("One"));
    assertTrue(str.contains("Two"));
  }
}
