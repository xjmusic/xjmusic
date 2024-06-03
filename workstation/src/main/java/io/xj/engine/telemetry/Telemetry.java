package io.xj.engine.telemetry;

public interface Telemetry {

  /**
   Reset the multi-stopwatch
   */
  void startTimer();

  /**
   Record a timer section

   @param name of the section
   @param millis spent in the section
   */
  void record(String name, Long millis);

  /**
    Report telemetry
   */
  void report();
}
