package io.xj.service.nexus.work;

import com.google.inject.Inject;
import io.xj.lib.notification.NotificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WorkerImpl implements Runnable {
  private final Logger log = LoggerFactory.getLogger(WorkerImpl.class);
  protected NotificationProvider notification;

  @Inject
  public WorkerImpl(
    NotificationProvider notification
  ) {
    this.notification = notification;
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
      doWork();
      log.debug("Completed {}", getName());

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
