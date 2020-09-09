package io.xj.service.nexus.work;

import com.google.inject.Inject;
import io.xj.lib.entity.Entity;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.entity.ChainState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Boss Worker implementation
 <p>
 [#171919183] Prometheus metrics reported by Java apps and consumed by CloudWatch
 */
public class BossWorkerImpl extends WorkerImpl implements BossWorker {
  private static final String NAME = "Boss";
  private static final String CHAIN_STARTED = "ChainStarted";
  private static final String CHAIN_CANCELLED = "ChainCancelled";
  private final Logger log = LoggerFactory.getLogger(BossWorkerImpl.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final NexusWork work;
  private final ChainDAO chainDAO;

  @Inject
  public BossWorkerImpl(
    NexusWork work,
    ChainDAO chainDAO,
    TelemetryProvider telemetryProvider
  ) {
    super(telemetryProvider);
    this.work = work;
    this.chainDAO = chainDAO;

    log.info("Instantiated OK");
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook

   @throws DAOPrivilegeException on access failure
   @throws DAOFatalException     on internal failure
   */
  protected void doWork() throws DAOPrivilegeException, DAOFatalException {
    Collection<UUID> activeIds = getActiveChainIds();
    startActiveChains(activeIds);
    cancelInactiveChains(activeIds);
  }

  @Override
  protected String getName() {
    return NAME;
  }

  /**
   Get the IDs of all Chains in the store whose state is currently in Fabricate

   @return active Chain IDS
   @throws DAOPrivilegeException on access control failure
   @throws DAOFatalException     on internal failure
   */
  private Collection<UUID> getActiveChainIds() throws DAOPrivilegeException, DAOFatalException {
    return chainDAO.readManyInState(access, ChainState.Fabricate)
      .stream()
      .map(Entity::getId)
      .collect(Collectors.toList());
  }

  /**
   Cancel all Chains that are not in the list of active Chain IDs

   @param activeIds to avoid cancellation
   */
  private void cancelInactiveChains(Collection<UUID> activeIds) {
    long chainsCanceled = 0;
    for (UUID id : work.getChainWorkingIds())
      if (!activeIds.contains(id)) {
        work.cancelChainWork(id);
        log.info("Did cancel work on Chain[{}]", id);
        chainsCanceled++;
      }
    observeCount(CHAIN_CANCELLED, chainsCanceled);
  }

  /**
   Start work on all chains from this list of active Chain IDs

   @param activeIds to start if not already active
   */
  private void startActiveChains(Collection<UUID> activeIds) {
    long chainsStarted = 0;
    for (UUID id : activeIds)
      if (!work.isWorkingOnChain(id)) {
        work.beginChainWork(id);
        log.info("Did start work on Chain[{}]", id);
        chainsStarted++;
      }
    observeCount(CHAIN_STARTED, chainsStarted);
  }
}
