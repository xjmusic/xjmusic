// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import com.google.api.client.util.Maps;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 Measures a series of named sections of time
 */
public class MultiStopwatch {
  private String section;
  private final Map<String, Float> sectionTotalSeconds = Maps.newHashMap();
  private final long started;

  private float lapTotalSeconds;

  private long lapStarted;
  private long sectionStarted;
  public static final String STANDBY = "Standby";
  private static final float MILLI = 1000;
  private static final float MILLIS_PER_SECOND = MILLI;
  private static final float NANOS_PER_SECOND = MILLIS_PER_SECOND * MILLI * MILLI;
  @Nullable
  private Float totalSeconds = null;

  /**
   Don't construct directly-- use MultiStopwatch.start()
   */
  private MultiStopwatch() {
    started = System.nanoTime();
    section(STANDBY);
    lap();
  }

  /**
   Start a MultiStopwatch

   @return new instance
   */
  public static MultiStopwatch start() {
    return new MultiStopwatch();
  }

  /**
   Stop the MultiStopwatch, measuring the last section and total seconds
   */
  public void stop() {
    lap();
    totalSeconds = (System.nanoTime() - started) / NANOS_PER_SECOND;
  }

  /**
   Record a lap, including a time for each cycle, and go back to standby
   */
  public void lap() {
    section(STANDBY);
      lapTotalSeconds =  (System.nanoTime() - lapStarted) / NANOS_PER_SECOND;
    lapStarted = System.nanoTime();
  }

  /**
   Get the total # of seconds. After MultiStopwatch is stopped, this will stop advancing.

   @return total seconds
   */
  public float getTotalSeconds() {
    return Objects.nonNull(totalSeconds) ? totalSeconds :
      (System.nanoTime() - started) / NANOS_PER_SECOND;
  }

  /**
   Get the total # of seconds of the last lap

   @return total seconds
   */
  public float getLapTotalSeconds() {
    return lapTotalSeconds;
  }

  /**
   Begin a section by name, and measure the last one if we are in a section

   @param name of next section
   */
  public void section(String name) {
    if (Objects.nonNull(section))
      sectionTotalSeconds.put(section, (System.nanoTime() - sectionStarted) / NANOS_PER_SECOND);
    sectionStarted = System.nanoTime();
    section = name;
  }

  /**
   Get map of all measured section times, keyed by section name, values are seconds floating point

   @return map of measured section times
   */
  public Map<String, Float> getSectionTotalSeconds() {
    return sectionTotalSeconds;
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String toString() {
    return sectionTotalSeconds.entrySet().stream()
      .map(entry -> String.format("%s: %f", entry.getKey(), entry.getValue()))
      .collect(Collectors.joining(", "));
  }

}
