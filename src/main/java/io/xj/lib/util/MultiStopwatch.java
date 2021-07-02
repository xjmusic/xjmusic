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
  private final Map<String, Double> lapSectionSeconds = Maps.newHashMap();
  private final Map<String, Double> totalSectionSeconds = Maps.newHashMap();
  private final long started;
  private double lapTotalSeconds;
  private long lapStarted;
  private long sectionStarted;
  public static final long SECONDS_PER_MINUTE = 60;
  public static final long MINUTES_PER_HOUR = 60;
  public static final long HOURS_PER_DAY = 24;
  public static final long MILLIS_PER_SECOND = 1000;
  public static final long NANOS_PER_SECOND = 1000000000L;
  public static final long NANOS_PER_MILLI = 1000000L;
  public static final long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
  public static final long SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
  public static final String STANDBY = "Standby";

  @Nullable
  private Double totalSeconds = null;

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
    totalSeconds = (double) (System.nanoTime() - started) / NANOS_PER_SECOND;
  }

  /**
   Record a lap, including a time for each cycle, and go back to standby
   */
  public void lap() {
    section(STANDBY);
    lapTotalSeconds = (double) (System.nanoTime() - lapStarted) / NANOS_PER_SECOND;
    lapStarted = System.nanoTime();
  }

  /**
   Get the total # of seconds. After MultiStopwatch is stopped, this will stop advancing.

   @return total seconds
   */
  public Double getTotalSeconds() {
    return Objects.nonNull(totalSeconds) ? totalSeconds : (System.nanoTime() - started) / NANOS_PER_SECOND;
  }

  /**
   Get the total # of seconds of the last lap

   @return total seconds
   */
  public double getLapTotalSeconds() {
    return lapTotalSeconds;
  }

  /**
   Begin a section by name, and measure the last one if we are in a section

   @param name of next section
   */
  public void section(String name) {
    if (Objects.nonNull(section)) {
      var seconds = (System.nanoTime() - sectionStarted) / NANOS_PER_SECOND;
      lapSectionSeconds.put(section, lapSectionSeconds.containsKey(section) ? lapSectionSeconds.get(section) + seconds : seconds);
      totalSectionSeconds.put(section, totalSectionSeconds.containsKey(section) ? totalSectionSeconds.get(section) + seconds : seconds);
    }
    sectionStarted = System.nanoTime();
    section = name;
  }

  /**
   Get map of all measured section times, keyed by section name, values are seconds double

   @return map of measured section times
   */
  public Map<String, Double> getLapSectionSeconds() {
    return lapSectionSeconds;
  }

  /**
   Get the totals of all measured section times, keyed by section name, values are seconds double

   @return map of measured section times
   */
  public Map<String, Double> getTotalSectionSeconds() {
    return totalSectionSeconds;
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String toString(double total, Map<String, Double> sectionTotals) {
    return String.format("%ss (%s)",
      formatHoursMinutesFromSeconds(total),
      sectionTotals.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .map(entry -> String.format("%s: %d%%", entry.getKey(), (int) Math.floor(100 * entry.getValue() / total)))
        .collect(Collectors.joining(", ")));
  }

  /**
   Format a number of seconds like 4d 12h 43m 23.45s or 12h 43m 23.45s or 43m 23.45s or 23.45s

   @param total number of seconds
   @return formatted seconds
   */
  private String formatHoursMinutesFromSeconds(double total) {
    int days = (int) Math.floor(total / SECONDS_PER_DAY);
    int hours = (int) Math.floor((total - days * SECONDS_PER_DAY) / SECONDS_PER_HOUR);
    int minutes = (int) Math.floor((total - days * SECONDS_PER_DAY - hours * SECONDS_PER_HOUR) / SECONDS_PER_MINUTE);
    double seconds = Math.floor(100 * (total - days * SECONDS_PER_DAY - hours * SECONDS_PER_HOUR - minutes * SECONDS_PER_MINUTE)) / 100;
    if (0 < days) return String.format("%dd %dh %dm %fs", days, hours, minutes, seconds);
    if (0 < hours) return String.format("%dh %dm %fs", hours, minutes, seconds);
    if (0 < minutes) return String.format("%dm %fs", minutes, seconds);
    return String.format("%fs", seconds);
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String lapToString() {
    return toString(lapTotalSeconds, lapSectionSeconds);
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String totalsToString() {
    return toString(getTotalSeconds(), totalSectionSeconds);
  }

  /**
   Clear all sections that are not standby
   */
  public void clearLapSections() {
    lapSectionSeconds.clear();
  }
}
