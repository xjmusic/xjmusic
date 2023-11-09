package io.xj.nexus.work;

public interface WorkTelemetry {
  String TIMER_SECTION_STANDBY = "Standby";

  /**
   Reset the multi-stopwatch
   */
  void startTimer();

  /**
   Stop the multi-stopwatch
   */
  void stopTimer();

  /**
   Mark a timer section

   @param name of the section
   */
  void markTimerSection(String name);

  /**
    Mark a lap
   * @return text describing the lap times
   */
  String markLap();

  /**
    Report telemetry
   */
  void report();
}
