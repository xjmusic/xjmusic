package io.xj.engine.telemetry;

import io.xj.model.util.ValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.xj.model.util.ValueUtils.MILLIS_PER_SECOND;

public class TelemetryImpl implements Telemetry {
  private final static Logger LOG = LoggerFactory.getLogger(TelemetryImpl.class);
  long startedAtMillis;
  Map<String, Long> sectionTotalMillis = new HashMap<>();

  public TelemetryImpl() {
    startTimer();
  }

  @Override
  public void startTimer() {
    startedAtMillis = System.currentTimeMillis();
    sectionTotalMillis.clear();
  }

  @Override
  public void record(String name, Long millis) {
    sectionTotalMillis.put(name, sectionTotalMillis.getOrDefault(name, 0L) + millis);
  }

  @Override
  public void report() {
    long totalMillis = System.currentTimeMillis() - startedAtMillis;
    LOG.info("({}) {}",
      formatHoursMinutesFromMillis(totalMillis),
      sectionTotalMillis.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .map(entry -> String.format("%s %d%%", (int) Math.floor((double) (100 * entry.getValue()) / totalMillis), entry.getKey()))
        .collect(Collectors.joining(" | ")));
  }

  /**
   Format a number of seconds like 4d 12h 43m 23.45s or 12h 43m 23.45s or 43m 23.45s or 23.45s

   @param millis number of milliseconds
   @return formatted seconds
   */
  String formatHoursMinutesFromMillis(long millis) {
    double totalSeconds = (double) millis / MILLIS_PER_SECOND;
    int days = (int) Math.floor(totalSeconds / ValueUtils.SECONDS_PER_DAY);
    int hours = (int) Math.floor((totalSeconds - days * ValueUtils.SECONDS_PER_DAY) / ValueUtils.SECONDS_PER_HOUR);
    int minutes = (int) Math.floor((totalSeconds - days * ValueUtils.SECONDS_PER_DAY - hours * ValueUtils.SECONDS_PER_HOUR) / ValueUtils.SECONDS_PER_MINUTE);
    double seconds = totalSeconds - days * ValueUtils.SECONDS_PER_DAY - hours * ValueUtils.SECONDS_PER_HOUR - minutes * ValueUtils.SECONDS_PER_MINUTE;
    if (0 < days) return String.format("%dd %dh %dm %ds", days, hours, minutes, (int) seconds);
    if (0 < hours) return String.format("%dh %dm %ds", hours, minutes, (int) seconds);
    if (0 < minutes) return String.format("%dm %ds", minutes, (int) seconds);
    return String.format("%.2fs", seconds);
  }

}
