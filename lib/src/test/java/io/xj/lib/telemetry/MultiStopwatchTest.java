// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 Measures a series of named sections of time
 */
public class MultiStopwatchTest {
  MultiStopwatch subject;

  @BeforeEach
  public void start() {
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
    assertTrue(0.01 <= subject.getSectionLapSeconds().get("One"));
    assertTrue(0.01 <= subject.getSectionLapSeconds().get("Two"));
    var str = subject.lapToString();
    assertTrue(str.contains("One"));
    assertTrue(str.contains("Two"));
  }

  @Test
  public void lap_section_getLapTimes_toString() throws InterruptedException {
    Thread.sleep(20); // wasted time
    subject.section("Work");
    Thread.sleep(10);
    subject.lap();

    assertTrue(0.03 <= subject.getLapTotalSeconds());

    Thread.sleep(20); // wasted time
    subject.section("Work");
    Thread.sleep(10);
    subject.stop();

    assertTrue(0.03 <= subject.getLapTotalSeconds());
    assertTrue(0.06 <= subject.getTotalSeconds());
    assertTrue(0.01 <= subject.getSectionLapSeconds().get("Work"));
    assertTrue(0.02 <= subject.getSectionLapSeconds().get("Standby"));
    assertTrue(0.02 <= subject.getSectionTotalSeconds().get("Work"));
    assertTrue(0.04 <= subject.getSectionTotalSeconds().get("Standby"));
    assertTrue(0.02 <= subject.getSectionLapSeconds().get(MultiStopwatch.STANDBY));
  }

  @Test
  public void lap_sectionTimesAddUpWithinLap() throws InterruptedException {
    Thread.sleep(10); // standby
    subject.section("Left");
    Thread.sleep(10);
    subject.section("Right");
    Thread.sleep(10);
    subject.section("Left");
    Thread.sleep(10);
    subject.section("Right");
    Thread.sleep(10);
    subject.lap();

    assertTrue(0.05 <= subject.getLapTotalSeconds());
    assertTrue(0.02 <= subject.getSectionLapSeconds().get("Left"));
    assertTrue(0.02 <= subject.getSectionLapSeconds().get("Right"));
    assertTrue(0.01 <= subject.getSectionLapSeconds().get(MultiStopwatch.STANDBY));
  }

}
