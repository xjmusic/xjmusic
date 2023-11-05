// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.telemetry;

import io.xj.hub.util.ValueUtils;
import jakarta.annotation.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 Measures a series of named sections of time
 */
public class MultiStopwatch {
  public static final String STANDBY = "Standby";
  final Map<String, Double> sectionLapSeconds = new HashMap<>();
  final Map<String, Double> sectionTotalSeconds = new HashMap<>();
  final long startedMillis;
  String section;
  double lapTotalSeconds;
  long lapStartedMillis;
  long sectionStartedMillis;
  @Nullable
  Double totalSeconds = null;

  /**
   Don't construct directly-- use MultiStopwatch.start()
   */
  MultiStopwatch() {
    startedMillis = System.currentTimeMillis();
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
    totalSeconds = (double) (System.currentTimeMillis() - startedMillis) / ValueUtils.MILLIS_PER_SECOND;
  }

  /**
   Record a lap, including a time for each cycle, and go back to standby
   */
  public void lap() {
    section(STANDBY);
    lapTotalSeconds = (double) (System.currentTimeMillis() - lapStartedMillis) / ValueUtils.MILLIS_PER_SECOND;
    lapStartedMillis = System.currentTimeMillis();
  }

  /**
   Get the total # of seconds. After MultiStopwatch is stopped, this will stop advancing.

   @return total seconds
   */
  public Double getTotalSeconds() {
    return Objects.nonNull(totalSeconds) ? totalSeconds : (double) (System.currentTimeMillis() - startedMillis) / ValueUtils.MILLIS_PER_SECOND;
  }

  /**
   Get the total # of seconds of the last lap

   @return total seconds
   */
  public Double getLapTotalSeconds() {
    return lapTotalSeconds;
  }

  /**
   Begin a section by name, and measure the last one if we are in a section

   @param name of next section
   */
  public void section(String name) {
    if (Objects.nonNull(section)) {
      var seconds = (double) (System.currentTimeMillis() - sectionStartedMillis) / ValueUtils.MILLIS_PER_SECOND;
      sectionLapSeconds.put(section, sectionLapSeconds.containsKey(section) ? sectionLapSeconds.get(section) + seconds : seconds);
      sectionTotalSeconds.put(section, sectionTotalSeconds.containsKey(section) ? sectionTotalSeconds.get(section) + seconds : seconds);
    }
    sectionStartedMillis = System.currentTimeMillis();
    section = name;
  }

  /**
   Get map of all measured section times, keyed by section name, values are seconds double

   @return map of measured section times
   */
  public Map<String, Double> getSectionLapSeconds() {
    return sectionLapSeconds;
  }

  /**
   Get the totals of all measured section times, keyed by section name, values are seconds double

   @return map of measured section times
   */
  public Map<String, Double> getSectionTotalSeconds() {
    return sectionTotalSeconds;
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String toString(double total, Map<String, Double> sectionTotals) {
    return String.format("%s (%s)",
      formatHoursMinutesFromSeconds(total),
      sectionTotals.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .map(entry -> String.format("%d%% %s", (int) Math.floor(100 * entry.getValue() / total), entry.getKey()))
        .collect(Collectors.joining(", ")));
  }

  /**
   Format a number of seconds like 4d 12h 43m 23.45s or 12h 43m 23.45s or 43m 23.45s or 23.45s

   @param total number of seconds
   @return formatted seconds
   */
  String formatHoursMinutesFromSeconds(double total) {
    int days = (int) Math.floor(total / ValueUtils.SECONDS_PER_DAY);
    int hours = (int) Math.floor((total - days * ValueUtils.SECONDS_PER_DAY) / ValueUtils.SECONDS_PER_HOUR);
    int minutes = (int) Math.floor((total - days * ValueUtils.SECONDS_PER_DAY - hours * ValueUtils.SECONDS_PER_HOUR) / ValueUtils.SECONDS_PER_MINUTE);
    double seconds = total - days * ValueUtils.SECONDS_PER_DAY - hours * ValueUtils.SECONDS_PER_HOUR - minutes * ValueUtils.SECONDS_PER_MINUTE;
    if (0 < days) return String.format("%dd %dh %dm %ds", days, hours, minutes, (int) seconds);
    if (0 < hours) return String.format("%dh %dm %ds", hours, minutes, (int) seconds);
    if (0 < minutes) return String.format("%dm %ds", minutes, (int) seconds);
    return String.format("%.2fs", seconds);
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String lapToString() {
    return toString(lapTotalSeconds, sectionLapSeconds);
  }

  /**
   Represent the whole stopwatch as a comma-separated list of sections and their time

   @return stopwatch as string
   */
  public String getTotalText() {
    return toString(getTotalSeconds(), sectionTotalSeconds);
  }

  /**
   Clear all sections that are not standby
   */
  public void clearLapSections() {
    sectionLapSeconds.clear();
  }
}
