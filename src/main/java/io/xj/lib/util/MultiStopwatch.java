// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import com.google.api.client.util.Maps;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 Measures a series of named sections of time
 */
public class MultiStopwatch {
  private String section;
  private final Map<String, Float> lapSectionSeconds = Maps.newHashMap();
  private final Map<String, Float> totalSectionSeconds = Maps.newHashMap();
  private final long started;
  private float lapTotalSeconds;
  private long lapStarted;
  private long sectionStarted;
  private static final float MILLI = 1000;
  private static final float MILLIS_PER_SECOND = MILLI;
  private static final float NANOS_PER_SECOND = MILLIS_PER_SECOND * MILLI * MILLI;
  public static final String STANDBY = "Standby";

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
    lapTotalSeconds = (System.nanoTime() - lapStarted) / NANOS_PER_SECOND;
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
    if (Objects.nonNull(section)) {
      var seconds = (System.nanoTime() - sectionStarted) / NANOS_PER_SECOND;
      lapSectionSeconds.put(section, seconds);
      totalSectionSeconds.put(section,
        totalSectionSeconds.containsKey(section) ? totalSectionSeconds.get(section) + seconds : seconds);
    }
    sectionStarted = System.nanoTime();
    section = name;
  }

  /**
   Get map of all measured section times, keyed by section name, values are seconds floating point

   @return map of measured section times
   */
  public Map<String, Float> getLapSectionSeconds() {
    return lapSectionSeconds;
  }

  /**
   Get the totals of all measured section times, keyed by section name, values are seconds floating point

   @return map of measured section times
   */
  public Map<String, Float> getTotalSectionSeconds() {
    return totalSectionSeconds;
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String lapToString() {
    return String.format("%s (%s)",
      lapTotalSeconds,
      lapSectionSeconds.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .map(entry -> String.format("%s: %d%%", entry.getKey(), (int) Math.floor(100 * entry.getValue() / lapTotalSeconds)))
        .collect(Collectors.joining(", ")));
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String totalsToString() {
    var total = getTotalSeconds();
    return String.format("%s (%s)",
      total,
      totalSectionSeconds.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .map(entry -> String.format("%s: %d%%", entry.getKey(), (int) Math.floor(100 * entry.getValue() / total)))
        .collect(Collectors.joining(", ")));
  }

  /**
   Clear all sections that are not standby
   */
  public void clearLapSections() {
    lapSectionSeconds.clear();
  }
}
