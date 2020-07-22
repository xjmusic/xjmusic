package io.xj.service.nexus.work;

import com.google.inject.Inject;
import io.xj.lib.entity.Entity;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.entity.ChainState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Boss Worker implementation
 */
public class BossWorkerImpl implements BossWorker {
  private final Logger log = LoggerFactory.getLogger(BossWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final NexusWork work;
  private final ChainDAO chainDAO;

  @Inject
  public BossWorkerImpl(
    NexusWork work,
    ChainDAO chainDAO
  ) {
    this.work = work;
    this.chainDAO = chainDAO;
    log.info("Instantiated OK");
  }

  public void run() {
    final Thread currentThread = Thread.currentThread();
    final String _ogThreadName = currentThread.getName();
    currentThread.setName(_ogThreadName + "-Boss");
    try {
      long t = Instant.now().toEpochMilli();
      Collection<UUID> activeIds = chainDAO.readManyInState(access, ChainState.Fabricate)
        .stream()
        .map(Entity::getId)
        .collect(Collectors.toList());
      for (UUID id : activeIds)
        if (!work.isWorkingOnChain(id))
          work.beginChainWork(id);
      for (UUID id : work.getChainWorkingIds())
        if (!activeIds.contains(id))
          work.cancelChainWork(id);
      log.info("Did run in {}ms OK", Instant.now().toEpochMilli() - t);

    } catch (Throwable e) {
      log.error("Failed!", e);

    } finally {
      currentThread.setName(_ogThreadName);
    }
  }
}
