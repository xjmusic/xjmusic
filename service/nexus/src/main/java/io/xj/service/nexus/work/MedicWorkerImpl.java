package io.xj.service.nexus.work;

import com.google.inject.Inject;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 Medic Worker implementation
 */
public class MedicWorkerImpl implements MedicWorker {
  private final Logger log = LoggerFactory.getLogger(MedicWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final ChainDAO chainDAO;

  @Inject
  public MedicWorkerImpl(
    ChainDAO chainDAO
  ) {
    this.chainDAO = chainDAO;
    log.info("Instantiated OK");
  }

  public void run() {
    final Thread currentThread = Thread.currentThread();
    final String _ogThreadName = currentThread.getName();
    currentThread.setName(_ogThreadName + "-Medic");
    try {
      long t = Instant.now().toEpochMilli();
      chainDAO.checkAndReviveAll(access);
      log.info("Did run in {}ms OK", Instant.now().toEpochMilli() - t);

    } catch (Throwable e) {
      log.error("Failed!", e);

    } finally {
      currentThread.setName(_ogThreadName);
    }
  }
}
