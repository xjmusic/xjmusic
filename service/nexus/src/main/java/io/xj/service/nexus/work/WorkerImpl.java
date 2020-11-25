package io.xj.service.nexus.work;

import com.amazonaws.services.cloudwatch.model.AmazonCloudWatchException;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.inject.Inject;
import io.xj.lib.telemetry.TelemetryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

public abstract class WorkerImpl implements Runnable {
  private static final String JOB_DURATION = "JobDuration";
  private static final String METRIC_NAMESPACE_FORMAT = "XJ Nexus %s";
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
      double totalSeconds = (double) (Instant.now().toEpochMilli() - jobStartAtMillis) / MILLIS_PER_SECOND;
      observeSeconds(JOB_DURATION, totalSeconds);
      log.info("Completed {} in {}s", getName(), totalSeconds);

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
    return String.format(METRIC_NAMESPACE_FORMAT, getName());
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
    try {
      metrics.send(getMetricNamespace(), name, StandardUnit.Count, value);
    } catch (AmazonCloudWatchException e) {
      log.warn("Failed to send metrics to Amazon CloudWatch because {}", e.getMessage());
    }
  }

  /**
   Send a seconds-type datum in this worker namespace

   @param name  of seconds-type datum
   @param value of seconds-type datum
   */
  protected void observeSeconds(String name, double value) {
    try {
      metrics.send(getMetricNamespace(), name, StandardUnit.Seconds, value);
    } catch (AmazonCloudWatchException e) {
      log.warn("Failed to send metrics to Amazon CloudWatch because {}", e.getMessage());
    }
  }
}
