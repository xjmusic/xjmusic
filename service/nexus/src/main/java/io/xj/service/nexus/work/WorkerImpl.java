package io.xj.service.nexus.work;

import com.google.inject.Inject;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkerImpl implements Runnable {
  private static final String JOB_DURATION = "JobDuration";
  private static final String TELEMETRY_WORKER_NAME = "Worker";
  private final Logger log = LoggerFactory.getLogger(WorkerImpl.class);

  @Inject
  public WorkerImpl() {
  }

  /**
   This is a wrapper to perform common tasks around threaded work execution
   */
  @Override
  @Trace(metricName = "work", nameTransaction = true, dispatcher = true)
  public void run() {
    final Thread currentThread = Thread.currentThread();
    final String _ogThreadName = currentThread.getName();
    currentThread.setName(String.format("%s-%s", _ogThreadName, getName()));
    try {
      var t = NewRelic.getAgent().getTransaction().startSegment(TELEMETRY_WORKER_NAME);
      doWork();
      t.end();
      log.info("Completed {}", getName());

    } catch (Throwable e) {
      log.error("Failed!", e);

    } finally {
      currentThread.setName(_ogThreadName);
    }
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
}
