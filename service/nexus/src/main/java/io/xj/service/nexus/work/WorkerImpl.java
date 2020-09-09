package io.xj.service.nexus.work;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.inject.Inject;
import io.xj.lib.telemetry.TelemetryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public abstract class WorkerImpl implements Runnable {
  private static final String JOB_DURATION = "JOB_DURATION";
  private final Logger log = LoggerFactory.getLogger(WorkerImpl.class);
  protected final TelemetryProvider metrics;

  @Inject
  public WorkerImpl(TelemetryProvider telemetryProvider) {
    metrics = telemetryProvider;
  }

  /**
   This is a wrapper to perform common tasks around threaded work execution
   */
  @Override
  public void run() {
    final Thread currentThread = Thread.currentThread();
    final String _ogThreadName = currentThread.getName();
    currentThread.setName(String.format("%s-%s", _ogThreadName, getName()));
    try {
      long jobStartAtMillis = Instant.now().toEpochMilli();
      doWork();
      long totalMillis = Instant.now().toEpochMilli() - jobStartAtMillis;
      metrics.send(getMetricNamespace(), JOB_DURATION, StandardUnit.Milliseconds, (double) totalMillis);

    } catch (Throwable e) {
      log.error("Failed!", e);

    } finally {
      currentThread.setName(_ogThreadName);
    }
  }

  /**
   Get the namespace for metrics based on the worker name

   @return metric name
   */
  private String getMetricNamespace() {
    return String.format("NEXUS/WORKER/%s", getName().toUpperCase());
  }

  /**
   Get the name of this worker

   @return name of worker
   */
  protected abstract String getName();

  /**
   This is where the work gets done
   */
  protected abstract void doWork() throws Exception;

  /**
   Send a count-type datum in this worker namespace

   @param name  of count-type datum
   @param value of count-type datum
   */
  protected void observeCount(String name, double value) {
    metrics.send(getMetricNamespace(), name, StandardUnit.Count, value);
  }

  /**
   Send a milliseconds-type datum in this worker namespace

   @param name  of milliseconds-type datum
   @param value of milliseconds-type datum
   */
  protected void observeMillis(String name, double value) {
    metrics.send(getMetricNamespace(), name, StandardUnit.Milliseconds, value);
  }
}
