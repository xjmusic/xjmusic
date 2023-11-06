package io.xj.nexus.work;

import io.xj.lib.telemetry.MultiStopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class WorkTelemetryImpl implements WorkTelemetry {
  private final static Logger LOG = LoggerFactory.getLogger(WorkTelemetryImpl.class);
  private MultiStopwatch timer;

  public WorkTelemetryImpl() {
    startTimer();
  }

  @Override
  public void startTimer() {
    timer = MultiStopwatch.start();
  }

  @Override
  public void stopTimer() {
    timer.stop();
  }

  @Override
  public void markTimerSection(String name) {
    timer.section(name);
  }

  @Override
  public String markLap() {
    timer.lap();
    var text = timer.getLapText();
    timer.clearLapSections();
    return text;
  }

  @Override
  public void report() {
    LOG.info("Fabrication time: {}", timer.getTotalText());
  }
}
